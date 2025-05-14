package net.rizecookey.racc0on.backend.x86_64.operand;

import java.util.List;

public interface x8664Operands<T extends x8664Operand> {
    List<T> inputs();
    List<T> outputs();

    record Binary<T extends x8664Operand>(T out, T inLeft, T inRight) implements x8664Operands<T> {
        @Override
        public List<T> inputs() {
            return List.of(out);
        }

        @Override
        public List<T> outputs() {
            return List.of(inLeft, inRight);
        }
    }
}
