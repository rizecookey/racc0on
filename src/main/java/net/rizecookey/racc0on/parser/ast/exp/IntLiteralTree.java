package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

import java.util.OptionalLong;

public record IntLiteralTree(String value, int base, Span span) implements ExpressionTree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    public OptionalLong parseValue() {
        int end = value.length();
        return switch (base) {
            case 16 -> parseHex(end);
            case 10 -> parseDec(end);
            default -> throw new IllegalArgumentException("unexpected base " + base);
        };
    }

    private OptionalLong parseDec(int end) {
        long l;
        try {
            l = Long.parseLong(value, 0, end, base);
        } catch (NumberFormatException _) {
            return OptionalLong.empty();
        }
        if (l < 0 || l > Integer.toUnsignedLong(Integer.MIN_VALUE)) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(l);
    }

    private OptionalLong parseHex(int end) {
        try {
            return OptionalLong.of(Integer.parseUnsignedInt(value, 2, end, 16));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

}
