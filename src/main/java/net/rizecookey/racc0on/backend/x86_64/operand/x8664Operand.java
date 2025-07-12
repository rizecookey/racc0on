package net.rizecookey.racc0on.backend.x86_64.operand;

import net.rizecookey.racc0on.backend.operand.Operand;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664Store;
import net.rizecookey.racc0on.ir.node.ValueType;

public sealed interface x8664Operand extends Operand permits x8664Store, x8664Immediate, x8664Label {
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

    enum Size implements Comparable<Size> {
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

        public static Size fromValueType(ValueType valueType) {
            return switch (valueType) {
                case NONE -> throw new IllegalArgumentException("NONE has no size");
                case INT, BOOL -> Size.DOUBLE_WORD;
                case POINTER, ARRAY -> Size.QUAD_WORD;
            };
        }
    }
}
