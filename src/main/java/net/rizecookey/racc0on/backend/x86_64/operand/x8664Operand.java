package net.rizecookey.racc0on.backend.x86_64.operand;

import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;

public interface x8664Operand {
    Id getId();

    record Id(String qwordName, String dwordName, String wordName, String byteName) {
        Id(String commonName) {
            this(commonName, commonName, commonName, commonName);
        }

        public String getName(Size size) {
            return switch (size) {
                case BYTE -> byteName;
                case WORD -> wordName;
                case DOUBLE_WORD -> dwordName;
                case QUAD_WORD -> qwordName;
            };
        }
    }

    enum Size {
        BYTE("byte ptr"), WORD("word ptr"), DOUBLE_WORD("dword ptr"), QUAD_WORD("qword ptr");

        private final String prefix;

        Size(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
