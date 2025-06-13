package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.StartNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SsaBlockLocalScheduler extends IrGraphTraverser {
    private final Set<Node> visited = new HashSet<>();
    private final List<Node> schedule = new ArrayList<>();
    private final List<Node> exitJumps = new ArrayList<>();

    @Override
    protected boolean visit(Node node) {
        if (addSeen(node)) {
            node.predecessors().stream()
                    .filter(pred -> !visited.contains(pred))
                    .filter(pred -> pred.block().equals(node.block()))
                    .forEach(this::push);
            return true;
        }
        return false;
    }

    @Override
    protected void consume(Node node) {
        if (!visited.add(node)) {
            return;
        }

        if (node instanceof StartNode) {
            schedule.addFirst(node);
        } else if (isExitNode(node)) {
            exitJumps.add(node);
        } else {
            schedule.add(node);
        }
    }

    public List<Node> getSchedule() {
        return Stream.concat(schedule.stream(), exitJumps.stream()).toList();
    }

    private static boolean isExitNode(Node node) {
        return switch (node) {
            case JumpNode _, IfNode _ -> true;
            case ProjNode proj when proj.projectionInfo() == ProjNode.SimpleProjectionInfo.IF_FALSE
                    || proj.projectionInfo() == ProjNode.SimpleProjectionInfo.IF_TRUE -> true;
            default -> false;
        };
    }
}
