package net.rizecookey.racc0on.ir.util;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.BuiltinCallNode;
import net.rizecookey.racc0on.ir.node.CallNode;
import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ParameterNode;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.ProjNode.SimpleProjectionInfo;
import net.rizecookey.racc0on.ir.node.ReturnNode;
import net.rizecookey.racc0on.ir.node.StartNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.memory.AllocArrayNode;
import net.rizecookey.racc0on.ir.node.operation.memory.AllocNode;
import net.rizecookey.racc0on.ir.node.operation.memory.ArrayMemberOffset;
import net.rizecookey.racc0on.ir.node.operation.memory.LoadNode;
import net.rizecookey.racc0on.ir.node.operation.memory.StoreNode;
import net.rizecookey.racc0on.ir.node.operation.memory.StructMemberOffset;
import net.rizecookey.racc0on.ir.node.operation.unary.UnaryOperationNode;
import net.rizecookey.racc0on.ir.schedule.SsaSchedule;
import net.rizecookey.racc0on.ir.schedule.SsaScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YCompPrinter {

    private final Map<Block, Set<Node>> clusters = new HashMap<>();
    private final Map<Node, Integer> ids = new HashMap<>();
    private final IrGraph graph;
    private int nodeCounter = 0;
    private int blockCounter = 0;
    private final SsaSchedule schedule;

    public YCompPrinter(IrGraph graph) {
        this.graph = graph;
        this.schedule = SsaScheduler.schedule(graph);
    }

    private void prepare(Node node, Set<Node> seen) {
        if (!seen.add(node)) {
            return;
        }

        if (!(node instanceof Block)) {
            this.clusters.computeIfAbsent(
                    node.block(),
                    _ -> Collections.newSetFromMap(new IdentityHashMap<>())
                )
                .add(node);
            prepare(node.block(), seen);
        }
        for (Node predecessor : node.predecessors()) {
            prepare(predecessor, seen);
        }
        if (node == this.graph.endBlock()) {
            this.clusters.put(this.graph.endBlock(), Set.of());
        }
    }

    public static String print(IrGraph graph) {
        YCompPrinter printer = new YCompPrinter(graph);
        printer.prepare(graph.endBlock(), new HashSet<>());
        return printer.dumpGraphAsString();
    }

    private String dumpGraphAsString() {
        StringBuilder result = new StringBuilder();

        result.append("graph: {");
        String graphName = this.graph.name();
        result.append("\n  title: ").append('"').append(graphName).append('"').append("\n");

        result.append("""
            display_edge_labels: yes
            layoutalgorithm: mindepth //$ "Compilergraph"
            manhattan_edges: yes
            port_sharing: no
            orientation: top_to_bottom
            """.indent(2));

        for (VcgColor color : VcgColor.values()) {
            result.append("\n  colorentry ").append(color.id()).append(": ").append(color.getRgb());
        }

        result.append("\n");

        result.append(formatMethod(graphName).indent(2));

        result.append("}");

        return result.toString();
    }

    private String formatMethod(String name) {
        StringBuilder result = new StringBuilder();

        result.append("graph: {");
        result.append("\n  title: ").append('"').append("method").append('"');
        result.append("\n  label: ").append('"').append(name).append('"');
        result.append("\n  color: ").append(VcgColor.ROOT_BLOCK.id());

        for (Entry<Block, Set<Node>> entry : this.clusters.entrySet()) {
            result.append("\n").append(formatBlock(entry.getKey(), entry.getValue()).indent(2));
        }

        result.append("}");

        return result.toString();
    }

    private String formatBlock(Block block, Set<Node> nodes) {
        StringBuilder result = new StringBuilder("graph: {");
        result.append("\n  title: " + '"').append(nodeTitle(block)).append('"');
        result.append("\n  label: " + '"').append(nodeLabel(block)).append('"');
        result.append("\n  status: clustered");
        result.append("\n  color: ").append(VcgColor.BLOCK.id());
        result.append("\n");

        for (Node node : nodes) {
            result.append(formatNode(node).indent(2));
            result.append(formatInputEdges(node).indent(2));
        }
        result.append(formatControlflowEdges(block));

        result.append(formatSchedule(block));

        result.append("\n}");

        return result.toString();
    }

    private String formatNode(Node node) {
        String infoText = "I am an info text for " + node;

        String result = "node: {";
        result += "\n  title: " + '"' + nodeTitle(node) + '"' + "\n";
        result += "\n  label: " + '"' + nodeLabel(node) + '"' + "\n";
        result += "\n  color: " + nodeColor(node).id();
        result += "\n  info1: " + '"' + infoText + '"';
        result += "\n}";

        return result;
    }

    private String formatInputEdges(Node node) {
        var edges = IntStream.range(0, node.predecessors().size())
            .mapToObj(
                idx -> new Edge(
                    node.predecessor(idx), node, idx, edgeColor(node.predecessor(idx), node)
                )
            )
            .toList();
        return formatEdges(edges, "\n  priority: 50");
    }

    private Optional<VcgColor> edgeColor(Node src, Node dst) {
        if (nodeColor(src) != VcgColor.NORMAL) {
            return Optional.of(nodeColor(src));
        }
        if (nodeColor(dst) != VcgColor.NORMAL) {
            return Optional.of(nodeColor(dst));
        }
        return Optional.empty();
    }

    private String formatControlflowEdges(Block block) {
        StringJoiner result = new StringJoiner("\n");
        List<? extends Node> parents = block.predecessors();
        for (Node parent : parents) {
            if (parent instanceof ReturnNode || parent instanceof ProjNode || parent instanceof JumpNode) {
                // Return needs no label
                result.add(formatControlflowEdge(parent, block, ""));
            } else {
                throw new RuntimeException("Unknown paren type: " + parent);
            }
        }

        return result.toString();
    }

    private String formatControlflowEdge(Node source, Block dst, String label) {
        String result = "edge: {";
        result += "\n  sourcename: " + '"' + nodeTitle(source) + '"';
        result += "\n  targetname: " + '"' + nodeTitle(dst) + '"';
        result += "\n  label: " + '"' + label + '"';
        result += "\n  color: " + VcgColor.CONTROL_FLOW.id();
        result += "\n}";
        return result;
    }

    private String formatEdges(Collection<Edge> edges, String additionalProps) {
        StringJoiner result = new StringJoiner("\n");
        for (Edge edge : edges) {
            StringBuilder inner = new StringBuilder();
            // edge: {sourcename: "n74" targetname: "n71" label: "0" class:14 priority:50 color:blue}
            inner.append("edge: {");
            inner.append("\n  sourcename: ").append('"').append(nodeTitle(edge.src())).append('"');
            inner.append("\n  targetname: ").append('"').append(nodeTitle(edge.dst())).append('"');
            inner.append("\n  label: ").append('"').append(edge.index()).append('"');
            edge.color.ifPresent(color -> inner.append("\n  color: ").append(color.id()));
            inner.append(additionalProps);
            inner.append("\n}");
            result.add(inner);
        }

        return result.toString();
    }

    private String formatSchedule(Block block) {
        // Once you have a schedule, you might want to also emit it :)
        List<Node> blockSchedule = schedule.nodeSchedules().get(block).stream()
                .filter(node -> !block.getExits().contains(node))
                .toList();
        List<Edge> edges = List.of();
        if (!blockSchedule.isEmpty()) {
            Node last = blockSchedule.getLast();
            edges = IntStream.range(0, blockSchedule.size() - 1)
                    .mapToObj(i -> {
                        Node first = blockSchedule.get(i);
                        Node second = blockSchedule.get(i + 1);
                        return new Edge(first, second, i + first.predecessors().size(), Optional.of(VcgColor.SCHEDULE));
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            int idx = 1;
            for (Node exit : block.getExits()) {
                edges.add(new Edge(last, exit, last.predecessors().size() + idx++, Optional.of(VcgColor.SCHEDULE)));
            }

        }
        return formatEdges(edges, "\n  color: " + VcgColor.SCHEDULE.id());
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private VcgColor nodeColor(Node node) {
        return switch (node) {
            case BinaryOperationNode _ -> VcgColor.NORMAL;
            case UnaryOperationNode _ -> VcgColor.NORMAL;
            case Block _ -> VcgColor.NORMAL;
            case ConstIntNode _ -> VcgColor.NORMAL;
            case ConstBoolNode _ -> VcgColor.NORMAL;
            case Phi _ -> VcgColor.PHI;
            case ProjNode proj -> {
                switch (proj.projectionInfo()) {
                    case SimpleProjectionInfo.SIDE_EFFECT -> {
                        yield VcgColor.MEMORY;
                    }
                    case SimpleProjectionInfo.RESULT -> {
                        yield VcgColor.NORMAL;
                    }
                    case SimpleProjectionInfo.IF_FALSE, SimpleProjectionInfo.IF_TRUE -> {
                        yield VcgColor.CONTROL_FLOW;
                    }
                }
            }
            case ReturnNode _ -> VcgColor.CONTROL_FLOW;
            case StartNode _ -> VcgColor.CONTROL_FLOW;
            case IfNode _ -> VcgColor.CONTROL_FLOW;
            case JumpNode _ -> VcgColor.CONTROL_FLOW;
            case CallNode _, BuiltinCallNode _ -> VcgColor.NORMAL;
            case ParameterNode _ -> VcgColor.NORMAL;
            case ArrayMemberOffset _ -> VcgColor.NORMAL;
            case LoadNode _ -> VcgColor.MEMORY;
            case StoreNode _ -> VcgColor.MEMORY;
            case StructMemberOffset _ -> VcgColor.NORMAL;
            case AllocArrayNode _ -> VcgColor.MEMORY;
            case AllocNode _ -> VcgColor.MEMORY;
        };
    }

    private String nodeTitle(Node node) {
        if (node instanceof Block block) {
            if (block == this.graph.startBlock()) {
                return "start-block";
            } else if (block == this.graph.endBlock()) {
                return "end-block";
            }
            return "block-" + idFor(block);
        }
        return "node-" + idFor(node);
    }

    private String nodeLabel(Node node) {
        if (node == this.graph.startBlock()) {
            return "start-block";
        } else if (node == this.graph.endBlock()) {
            return "end-block";
        }
        return node.toString();
    }

    private int idFor(Node node) {
        if (node instanceof Block block) {
            return this.ids.computeIfAbsent(block, _ -> this.blockCounter++);
        }
        return this.ids.computeIfAbsent(node, _ -> this.nodeCounter++);
    }

    private record Edge(Node src, Node dst, int index, Optional<VcgColor> color) {

    }

    private enum VcgColor {
        // colorentry 100: 204 204 204  gray
        // colorentry 101: 222 239 234  faint green
        // colorentry 103: 242 242 242  white-ish
        // colorentry 104: 153 255 153  light green
        // colorentry 105: 153 153 255  blue
        // colorentry 106: 255 153 153  red
        // colorentry 107: 255 255 153  yellow
        // colorentry 108: 255 153 255  pink
        // colorentry 110: 127 127 127  dark gray
        // colorentry 111: 153 255 153  light green
        // colorentry 114: 153 153 255  blue
        CONTROL_FLOW("255 153 153"),
        MEMORY("153 153 255"),
        NORMAL("242 242 242"),
        SPECIAL("255 153 255"),
        CONST("255 255 153"),
        PHI("153 255 153"),
        ROOT_BLOCK("204 204 204"),
        BLOCK("222 239 234"),
        SCHEDULE("255 153 255");

        private final String rgb;

        VcgColor(String rgb) {
            this.rgb = rgb;
        }

        public String getRgb() {
            return rgb;
        }

        public int id() {
            return 100 + ordinal();
        }
    }
}
