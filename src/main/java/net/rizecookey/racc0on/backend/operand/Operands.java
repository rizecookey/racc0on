package net.rizecookey.racc0on.backend.operand;

import java.util.List;

public interface Operands<T> {
    List<T> inputs();
    List<T> outputs();

    record Binary<T>(T out, T inLeft, T inRight) implements Operands<T> {
        @Override
        public List<T> inputs() {
            return List.of(inLeft(), inRight());
        }

        @Override
        public List<T> outputs() {
            return List.of(out());
        }
    }
}
