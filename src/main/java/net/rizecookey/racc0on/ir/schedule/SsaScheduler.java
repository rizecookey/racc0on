package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.util.NodeSupport;
import net.rizecookey.racc0on.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

public class SsaScheduler extends IrGraphTraverser {
    private final IrGraph program;
    private final Map<Block, SequencedSet<Node>> blockNodes;

    private final List<Block> blockOrder = new ArrayList<>();
    private final Map<Block, List<Node>> schedules = new HashMap<>();

    private final Set<Phi> phisVisited = new HashSet<>();
    private final Map<Block, List<Pair<Phi, Node>>> phiMoves = new HashMap<>();

    public SsaScheduler(IrGraph program) {
        this.program = program;

        NodeCollector collector = new NodeCollector();
        collector.traverse(program.endBlock());
        this.blockNodes = collector.getBlockNodes();
    }

    @Override
    protected boolean visit(Node node) {
        if (!addSeen(node)) {
            return false;
        }

        node.predecessors().stream().map(Node::block).forEach(this::push);
        return true;
    }

    @Override
    protected void consume(Node node) {
        if (!(node instanceof Block block)) {
            return;
        }

        if (block.predecessors().size() > 1 && block.predecessors().stream()
                .anyMatch(pred -> pred.block().getExits().size() > 1)) {
            throw new IllegalStateException(block + " has a critical edge with one of its predecessors");
        }

        blockOrder.add(block);

        SsaBlockLocalScheduler scheduler = new SsaBlockLocalScheduler();
        for (Node inner : blockNodes.get(block)) {
            scheduler.traverse(inner);
        }
        List<Node> schedule = scheduler.getSchedule();
        schedules.put(block, schedule);

        for (Node inner : schedule.reversed()) {
            if (!(inner instanceof Phi phi) || !phisVisited.add(phi) || NodeSupport.isSideEffect(phi)) {
                continue;
            }

            schedulePhiMoves(phi);
        }
    }

    private void schedulePhiMoves(Phi phi) {
        for (int i = 0; i < phi.predecessors().size(); i++) {
            Node pred = NodeSupport.predecessorSkipProj(phi, i);
            Block block = phi.block().predecessor(i).block();

            phiMoves.computeIfAbsent(block, _ -> new ArrayList<>()).add(new Pair<>(phi, pred));
        }
    }

    public SsaSchedule getSchedule() {
        return new SsaSchedule(blockOrder, schedules, phiMoves, program);
    }

    public static SsaSchedule schedule(IrGraph program) {
        SsaScheduler scheduler = new SsaScheduler(program);
        scheduler.traverse(program.endBlock());

        return scheduler.getSchedule();
    }
}
