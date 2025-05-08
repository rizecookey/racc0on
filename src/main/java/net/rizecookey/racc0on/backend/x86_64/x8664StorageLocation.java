package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public sealed interface x8664StorageLocation extends Register permits x8664Register, x8664StackLocation {
}
