package net.rizecookey.racc0on.backend.x86_64.operand;

import net.rizecookey.racc0on.backend.operand.Operand;

public interface x8664Operand extends Operand {
    Id getId();

    record Id(String qwordName, String dwordName, String wordName, String byteName) {
        public Id(String commonName) {
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
        BYTE(1, "byte ptr"), WORD(2, "word ptr"), DOUBLE_WORD(4, "dword ptr"), QUAD_WORD(8, "qword ptr");

        private final String prefix;
        private final int bytes;
        Size(int bytes, String prefix) {
            this.bytes = bytes;
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getByteSize() {
            return bytes;
        }
    }
}
