package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public sealed interface x8664StorageLocation extends Register permits x8664Register, x8664StackLocation {
    LocationId getId();

    record LocationId(String qwordName, String dwordName, String wordName) {
        LocationId(String commonName) {
            this(commonName, commonName, commonName);
        }
    }

    static LocationId reg16(String coreId) {
        return new LocationId("r" + coreId, "e" + coreId, coreId);
    }

    static LocationId reg64(String coreName) {
        return new LocationId(coreName, coreName + "d", coreName + "w");
    }
}
