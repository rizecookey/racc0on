package net.rizecookey.racc0on.ir.xir;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import net.rizecookey.racc0on.ir.SsaSchedule;
import net.rizecookey.racc0on.ir.xir.node.PhiSupportXNode;
import net.rizecookey.racc0on.ir.xir.node.SsaXNode;
import net.rizecookey.racc0on.ir.xir.node.XNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record XIrSchedule(List<Block> blockOrder, Map<Block, List<XNode>> blockSchedules) {
    public static XIrSchedule extend(SsaSchedule ssaSchedule) {
        Map<Block, List<XNode>> xBlockSchedules = new HashMap<>();
        for (Block block : ssaSchedule.blockOrder()) {
            List<Node> blockSchedule = ssaSchedule.blockSchedules().get(block);
            List<XNode> xBlockSchedule = new ArrayList<>();
            for (Node node : blockSchedule) {
                xBlockSchedule.add(new SsaXNode(node));

                for (Node successor : node.graph().successors(node)) {
                    if (!(successor instanceof Phi phi)) {
                        continue;
                    }
                    xBlockSchedule.add(new PhiSupportXNode(phi, node));
                }
            }
            xBlockSchedules.put(block, xBlockSchedule);
        }

        return new XIrSchedule(ssaSchedule.blockOrder(), Map.copyOf(xBlockSchedules));
    }
}
