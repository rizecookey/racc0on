package net.rizecookey.racc0on.ir.xir.node;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;

public record PhiSupportXNode(Phi phi, Node value) implements XNode {
}
