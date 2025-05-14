package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.IrGraph;

import java.util.List;

public interface CodeGenerator {
    String generateCode(List<IrGraph> program);
}
