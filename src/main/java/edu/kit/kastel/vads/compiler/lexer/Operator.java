package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Span;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record Operator(OperatorType type, Span span) implements Token {

    @Override
    public boolean isOperator(OperatorType operatorType) {
        return type() == operatorType;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public sealed interface OperatorType permits AmbiguousOperatorType, UnaryOperatorType, BinaryOperatorType {
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
    }

    public enum AmbiguousOperatorType implements OperatorType {
        MINUS("-", UnaryOperatorType.NEGATION, BinaryOperatorType.MINUS),
        ;

        private final String value;
        private final List<OperatorType> potentialInstances;
        AmbiguousOperatorType(String value, OperatorType... potentialInstances) {
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

    public enum UnaryOperatorType implements OperatorType {
        NEGATION("-"),
        NOT("!"),
        BITWISE_NOT("~"),
        ;

        private final String value;

        UnaryOperatorType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum BinaryOperatorType implements OperatorType {
        ASSIGN_MINUS("-=", true),
        MINUS("-"),
        ASSIGN_PLUS("+=", true),
        PLUS("+"),
        MUL("*"),
        ASSIGN_MUL("*=", true),
        ASSIGN_DIV("/=", true),
        DIV("/"),
        ASSIGN_MOD("%=", true),
        MOD("%"),
        ASSIGN("=", true),

        BITWISE_AND("&"),
        ASSIGN_BITWISE_AND("&=", true),
        BITWISE_XOR("^"),
        ASSIGN_BITWISE_XOR("^=", true),
        BITWISE_OR("|"),
        ASSIGN_BITWISE_OR("|=", true),
        SHIFT_LEFT("<<"),
        ASSIGN_SHIFT_LEFT("<<=", true),
        SHIFT_RIGHT(">>"),
        ASSIGN_SHIFT_RIGHT(">>=", true),

        LESS_THAN("<"),
        LESS_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_OR_EQUAL(">="),
        EQUAL("=="),
        NOT_EQUAL("!="),
        AND("&&"),
        OR("||"),

        TERNARY_IF_BRANCH("?"),
        TERNARY_ELSE_BRANCH(":"),
        ;

        public static final List<Set<BinaryOperatorType>> PRECEDENCE = List.of(
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
        private final boolean isAssignment;

        BinaryOperatorType(String value, boolean isAssignment) {
            this.value = value;
            this.isAssignment = isAssignment;
        }

        BinaryOperatorType(String value) {
            this(value, false);
        }

        @Override
        public String toString() {
            return this.value;
        }

        @Override
        public boolean isAssignment() {
            return isAssignment;
        }
    }
}
