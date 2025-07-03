package net.rizecookey.racc0on.lexer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public sealed interface OperatorType permits OperatorType.Ambiguous, OperatorType.Assignment, OperatorType.Binary, OperatorType.Pointer, OperatorType.Ternary, OperatorType.Unary {
    default boolean isAssignment() {
        return false;
    }

    default <T extends OperatorType> boolean matchesType(Class<T> type) {
        return type.isAssignableFrom(getClass());
    }

    default <T extends OperatorType> Optional<T> as(Class<T> type) {
        if (matchesType(type)) {
            return Optional.of(type.cast(this));
        }

        return Optional.empty();
    }

    String toString();

    enum Ambiguous implements OperatorType {
        MINUS("-", Unary.NEGATION, Binary.MINUS),
        ;

        private final String value;
        private final List<OperatorType> potentialInstances;
        Ambiguous(String value, OperatorType... potentialInstances) {
            this.value = value;
            this.potentialInstances = List.of(potentialInstances);
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public <T extends OperatorType> Optional<T> as(Class<T> type) {
            return potentialInstances().stream()
                    .filter(inst -> type.isAssignableFrom(inst.getClass()))
                    .map(type::cast)
                    .findFirst();
        }

        public List<OperatorType> potentialInstances() {
            return potentialInstances;
        }
    }

    enum Unary implements OperatorType {
        NEGATION("-"),
        NOT("!"),
        BITWISE_NOT("~"),
        ;

        private final String value;

        Unary(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    enum Binary implements OperatorType {
        MINUS("-"),
        PLUS("+"),
        MUL("*"),
        DIV("/"),
        MOD("%"),

        BITWISE_AND("&"),
        BITWISE_XOR("^"),
        BITWISE_OR("|"),

        SHIFT_LEFT("<<"),
        SHIFT_RIGHT(">>"),

        LESS_THAN("<"),
        LESS_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_OR_EQUAL(">="),
        EQUAL("=="),
        NOT_EQUAL("!="),
        AND("&&"),
        OR("||"),
        ;

        public static final List<Set<Binary>> PRECEDENCE = List.of(
                Set.of(OR),
                Set.of(AND),
                Set.of(BITWISE_OR),
                Set.of(BITWISE_XOR),
                Set.of(BITWISE_AND),
                Set.of(EQUAL, NOT_EQUAL),
                Set.of(LESS_THAN, LESS_OR_EQUAL, GREATER_THAN, GREATER_OR_EQUAL),
                Set.of(SHIFT_LEFT, SHIFT_RIGHT),
                Set.of(PLUS, MINUS),
                Set.of(MUL, DIV, MOD)
        );

        private final String value;
        Binary(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    enum Assignment implements OperatorType {
        MINUS("-="),
        PLUS("+="),
        MUL("*="),
        DIV("/="),
        MOD("%="),
        DEFAULT("="),
        BITWISE_AND("&="),
        BITWISE_XOR("^="),
        BITWISE_OR("|="),
        SHIFT_LEFT("<<="),
        SHIFT_RIGHT(">>="),
        ;

        private final String value;
        Assignment(String value) {
            this.value = value;
        }

        @Override
        public boolean isAssignment() {
            return true;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    enum Ternary implements OperatorType {
        IF_BRANCH("?"),
        ELSE_BRANCH(":"),
        ;

        private final String value;
        Ternary(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    enum Pointer implements OperatorType {
        ARROW("->"),
        FIELD_ACCESS("."),
        DEREFERENCE("*"),
        ;

        private final String value;
        Pointer(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
