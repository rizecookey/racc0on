package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.utils.Pair;

import java.util.List;
import java.util.Map;

public record SsaSchedule(List<Block> blockSchedule, Map<Block, List<Node>> nodeSchedules, Map<Block, List<Pair<Phi, Node>>> phiMoves, IrGraph programGraph) {

    public static SsaSchedule generate(IrGraph program) {
        SsaBlockScheduler blockSchedule = new SsaBlockScheduler();
        blockSchedule.traverse(program);
        List<Block> blockOrder = blockSchedule.getSchedule();

        SsaNodeScheduler nodeScheduler = new SsaNodeScheduler();
        nodeScheduler.traverse(program);
        Map<Block, List<Node>> nodeSchedules = nodeScheduler.getSchedules();

        PhiMoveScheduler phiMoveScheduler = new PhiMoveScheduler();
        phiMoveScheduler.traverse(program);
        Map<Block, List<Pair<Phi, Node>>> phiMoves = phiMoveScheduler.getPhiMoves();

        return new SsaSchedule(blockOrder, nodeSchedules, phiMoves, program);
    }
}
