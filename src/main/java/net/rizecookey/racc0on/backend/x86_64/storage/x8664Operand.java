package net.rizecookey.racc0on.backend.x86_64.storage;

public interface x8664Operand {
    OperandId getId();

    record OperandId(String qwordName, String dwordName, String wordName) {
        OperandId(String commonName) {
            this(commonName, commonName, commonName);
        }
    }
}
