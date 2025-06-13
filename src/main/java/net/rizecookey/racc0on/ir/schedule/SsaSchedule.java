package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.utils.Pair;

import java.util.List;
import java.util.Map;

public record SsaSchedule(List<Block> blockSchedule, Map<Block, List<Node>> nodeSchedules, Map<Block, List<Pair<Phi, Node>>> phiMoves, IrGraph programGraph) {
}
