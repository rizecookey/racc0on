package net.rizecookey.racc0on.backend.x86_64.storage;

public interface x8664Operand {
    OperandId getId();

    record OperandId(String qwordName, String dwordName, String wordName) {
        OperandId(String commonName) {
            this(commonName, commonName, commonName);
        }

        public String getName(Size size) {
            return switch (size) {
                case WORD -> wordName;
                case DOUBLE_WORD -> dwordName;
                case QUAD_WORD -> qwordName;
            };
        }

        public static OperandId stack(int offset) {
            String common = "[" + x8664Register.RSP.getId().qwordName() + (offset != 0 ? "+" + offset : "") + "]";
            return new OperandId(common);
        }
    }

    enum Size {
        WORD("word ptr"), DOUBLE_WORD("dword ptr"), QUAD_WORD("qword ptr");

        private final String prefix;

        Size(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
