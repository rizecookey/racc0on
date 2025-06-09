package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;

import java.util.List;
import java.util.Map;

public record SsaSchedule(List<Block> blockSchedule, Map<Block, List<Node>> nodeSchedules, Map<Block, Map<Phi, Node>> phiValues, IrGraph programGraph) {

    public static SsaSchedule generate(IrGraph program) {
        SsaBlockScheduler blockSchedule = new SsaBlockScheduler();
        blockSchedule.traverse(program);
        List<Block> blockOrder = blockSchedule.getSchedule();

        SsaNodeScheduler nodeScheduler = new SsaNodeScheduler();
        nodeScheduler.traverse(program);
        Map<Block, List<Node>> nodeSchedules = nodeScheduler.getSchedules();

        PhiMoveScheduler phiMoveScheduler = new PhiMoveScheduler();
        phiMoveScheduler.traverse(program);
        Map<Block, Map<Phi, Node>> phiValues = phiMoveScheduler.getPhiValues();

        return new SsaSchedule(blockOrder, nodeSchedules, phiValues, program);
    }
}
