package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.ir.node.Node;

import java.util.List;

public class x8664IfElseOpLike implements x8664OpLike {
    private final x8664ConditionalJumpOp thenJump;
    private final x8664JumpOp elseJump;

    public x8664IfElseOpLike(Node condition, boolean negate, String thenTarget, String elseTarget) {
        this.thenJump = new x8664ConditionalJumpOp(condition, negate, thenTarget);
        this.elseJump = new x8664JumpOp(elseTarget);
    }

    @Override
    public List<x8664Op> asOperations() {
        return List.of(thenJump, elseJump);
    }
}
