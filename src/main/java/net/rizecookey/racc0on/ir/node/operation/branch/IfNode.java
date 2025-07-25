package net.rizecookey.racc0on.ir.node.operation.branch;

import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ProjNode;

import java.util.Map;
import java.util.stream.Collectors;

public final class IfNode extends AbstractNode {
    public static final int CONDITION = 0;

    public IfNode(Block block, Node condition) {
        super(block, condition);
    }

    public Map<Boolean, Block> targets() {
        return graph().successors(this).stream()
                .map(succ -> {
                    if (!(succ instanceof ProjNode proj) || proj.projectionInfo() != ProjNode.SimpleProjectionInfo.IF_FALSE
                            && proj.projectionInfo() != ProjNode.SimpleProjectionInfo.IF_TRUE) {
                        throw new IllegalStateException("Invalid successors");
                    }
                    return Map.entry(proj.projectionInfo() == ProjNode.SimpleProjectionInfo.IF_TRUE,
                            graph().successors(proj).stream()
                                    .filter(succSucc -> succSucc instanceof Block)
                                    .map(succSucc -> (Block) succSucc)
                                    .findFirst().orElseThrow());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
