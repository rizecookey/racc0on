package net.rizecookey.racc0on.utils;

public sealed interface Span {
    Position start();
    Position end();

    Span merge(Span later);

    record SimpleSpan(Position start, Position end) implements Span {
        @Override
        public Span merge(Span later) {
            return new SimpleSpan(start(), later.end());
        }

        @Override
        public String toString() {
            return "[" + start() + "|" + end() + "]";
        }
    }

    static Position min(Position first, Position second) {
        int lineDiff = first.line() - second.line();
        if (lineDiff != 0) {
            return lineDiff < 0 ? first : second;
        }

        return first.column() <= second.column() ? first : second;
    }

    static Position max(Position first, Position second) {
        int lineDiff = first.line() - second.line();
        if (lineDiff != 0) {
            return lineDiff > 0 ? first : second;
        }

        return first.column() >= second.column() ? first : second;
    }
}
