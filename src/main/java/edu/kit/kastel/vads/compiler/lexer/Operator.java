package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Span;

public record Operator(OperatorType type, Span span) implements Token {

    @Override
    public boolean isOperator(OperatorType operatorType) {
        return type() == operatorType;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public enum OperatorType {
        ASSIGN_MINUS("-=", true),
        MINUS("-", 2),
        ASSIGN_PLUS("+=", true),
        PLUS("+", 2),
        MUL("*", 2),
        ASSIGN_MUL("*=", true),
        ASSIGN_DIV("/=", true),
        DIV("/", 2),
        ASSIGN_MOD("%=", true),
        MOD("%", 2),
        ASSIGN("=", true),

        BITWISE_AND("&", 2),
        ASSIGN_BITWISE_AND("&=", true),
        BITWISE_XOR("^", 2),
        ASSIGN_BITWISE_XOR("^=", true),
        BITWISE_OR("|", 2),
        ASSIGN_BITWISE_OR("|=", true),
        SHIFT_LEFT("<<", 2),
        ASSIGN_SHIFT_LEFT("<<=", true),
        SHIFT_RIGHT(">>", 2),
        ASSIGN_SHIFT_RIGHT(">>=", true),

        LESS_THAN("<", 2),
        LESS_OR_EQUAL("<=", 2),
        GREATER_THAN(">", 2),
        GREATER_OR_EQUAL(">=", 2),
        EQUAL("==", 2),
        NOT_EQUAL("!=", 2),
        AND("&&", 2),
        OR("||", 2),

        NOT("!", 1),
        BITWISE_NOT("~", 1),

        TERNARY_IF_BRANCH("?", 3),
        TERNARY_ELSE_BRANCH(":", 3),
        ;

        private final String value;
        private final boolean isAssignment;
        private final int operandCount;

        OperatorType(String value, boolean isAssignment) {
            this.value = value;
            this.isAssignment = isAssignment;
            this.operandCount = 1;
        }

        OperatorType(String value, int operandCount) {
            this.value = value;
            this.isAssignment = false;
            this.operandCount = operandCount;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public boolean isAssignment() {
            return isAssignment;
        }

        public int getOperandCount() {
            return operandCount;
        }
    }
}
