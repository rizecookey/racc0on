package net.rizecookey.racc0on.backend;

import net.rizecookey.racc0on.ir.IrGraph;

import java.util.List;

public interface CodeGenerator {
    String generateCode(List<IrGraph> program);
}
