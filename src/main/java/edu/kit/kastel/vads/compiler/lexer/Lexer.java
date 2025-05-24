package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.Operator.OperatorType;
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Lexer {
    private final String source;
    private int pos;
    private int lineStart;
    private int line;

    private Lexer(String source) {
        this.source = source;
    }

    public static Lexer forString(String source) {
        return new Lexer(source);
    }

    public Optional<Token> nextToken() {
        ErrorToken error = skipWhitespace();
        if (error != null) {
            return Optional.of(error);
        }
        if (this.pos >= this.source.length()) {
            return Optional.empty();
        }
        Token t = switch (peek()) {
            case '(' -> separator(SeparatorType.PAREN_OPEN);
            case ')' -> separator(SeparatorType.PAREN_CLOSE);
            case '{' -> separator(SeparatorType.BRACE_OPEN);
            case '}' -> separator(SeparatorType.BRACE_CLOSE);
            case ';' -> separator(SeparatorType.SEMICOLON);
            case '-' -> singleOrFollowedByEq(OperatorType.MINUS, OperatorType.ASSIGN_MINUS);
            case '+' -> singleOrFollowedByEq(OperatorType.PLUS, OperatorType.ASSIGN_PLUS);
            case '*' -> singleOrFollowedByEq(OperatorType.MUL, OperatorType.ASSIGN_MUL);
            case '/' -> singleOrFollowedByEq(OperatorType.DIV, OperatorType.ASSIGN_DIV);
            case '%' -> singleOrFollowedByEq(OperatorType.MOD, OperatorType.ASSIGN_MOD);
            case '^' -> singleOrFollowedByEq(OperatorType.BITWISE_XOR, OperatorType.ASSIGN_BITWISE_XOR);

            case '&' -> singleOrAssignOrDouble(OperatorType.BITWISE_AND, OperatorType.ASSIGN_BITWISE_AND, OperatorType.AND);
            case '|' -> singleOrAssignOrDouble(OperatorType.BITWISE_OR, OperatorType.ASSIGN_BITWISE_OR, OperatorType.OR);

            case '<' -> compareOrDoubleOrDoubleAssign(OperatorType.LESS_THAN, OperatorType.LESS_OR_EQUAL,
                    OperatorType.SHIFT_LEFT, OperatorType.ASSIGN_SHIFT_LEFT);
            case '>' -> compareOrDoubleOrDoubleAssign(OperatorType.GREATER_THAN, OperatorType.GREATER_OR_EQUAL,
                    OperatorType.SHIFT_RIGHT, OperatorType.ASSIGN_SHIFT_RIGHT);

            case '~' -> new Operator(OperatorType.BITWISE_NOT, buildSpan(1));
            case '!' -> singleOrFollowedByEq(OperatorType.NOT, OperatorType.NOT_EQUAL);
            case '=' -> singleOrFollowedByEq(OperatorType.ASSIGN, OperatorType.EQUAL);

            case '?' -> new Operator(OperatorType.TERNARY_IF_BRANCH, buildSpan(1));
            case ':' -> new Operator(OperatorType.TERNARY_ELSE_BRANCH, buildSpan(1));

            default -> {
                if (isIdentifierChar(peek())) {
                    if (isNumeric(peek())) {
                        yield lexNumber();
                    }
                    Optional<String> booleanMatch = tryLexingBoolean();
                    if (booleanMatch.isPresent()) {
                        String booleanString = booleanMatch.get();
                        yield new BooleanLiteral(Boolean.parseBoolean(booleanString), buildSpan(booleanString.length()));
                    }
                    yield lexIdentifierOrKeyword();
                }
                yield new ErrorToken(String.valueOf(peek()), buildSpan(1));
            }
        };

        return Optional.of(t);
    }

    private @Nullable ErrorToken skipWhitespace() {
        enum CommentType {
            SINGLE_LINE,
            MULTI_LINE
        }
        CommentType currentCommentType = null;
        int multiLineCommentDepth = 0;
        int commentStart = -1;
        while (hasMore(0)) {
            switch (peek()) {
                case ' ', '\t' -> this.pos++;
                case '\n', '\r' -> {
                    this.pos++;
                    this.lineStart = this.pos;
                    this.line++;
                    if (currentCommentType == CommentType.SINGLE_LINE) {
                        currentCommentType = null;
                    }
                }
                case '/' -> {
                    if (currentCommentType == CommentType.SINGLE_LINE) {
                        this.pos++;
                        continue;
                    }
                    if (hasMore(1)) {
                        if (peek(1) == '/' && currentCommentType == null) {
                            currentCommentType = CommentType.SINGLE_LINE;
                        } else if (peek(1) == '*') {
                            currentCommentType = CommentType.MULTI_LINE;
                            multiLineCommentDepth++;
                        } else if (currentCommentType == CommentType.MULTI_LINE) {
                            this.pos++;
                            continue;
                        } else {
                            return null;
                        }
                        commentStart = this.pos;
                        this.pos += 2;
                        continue;
                    }
                    // are we in a multi line comment of any depth?
                    if (multiLineCommentDepth > 0) {
                        this.pos++;
                        continue;
                    }
                    return null;
                }
                default -> {
                    if (currentCommentType == CommentType.MULTI_LINE) {
                        if (peek() == '*' && hasMore(1) && peek(1) == '/') {
                            this.pos += 2;
                            multiLineCommentDepth--;
                            currentCommentType = multiLineCommentDepth == 0 ? null : CommentType.MULTI_LINE;
                        } else {
                            this.pos++;
                        }
                        continue;
                    } else if (currentCommentType == CommentType.SINGLE_LINE) {
                        this.pos++;
                        continue;
                    }
                    return null;
                }
            }
        }
        if (!hasMore(0) && currentCommentType == CommentType.MULTI_LINE) {
            return new ErrorToken(this.source.substring(commentStart), buildSpan(0));
        }
        return null;
    }

    private Separator separator(SeparatorType parenOpen) {
        return new Separator(parenOpen, buildSpan(1));
    }

    private Token lexIdentifierOrKeyword() {
        int off = 1;
        while (hasMore(off) && isIdentifierChar(peek(off))) {
            off++;
        }
        String id = this.source.substring(this.pos, this.pos + off);
        // This is a naive solution. Using a better data structure (hashmap, trie) likely performs better.
        for (KeywordType value : KeywordType.values()) {
            if (value.keyword().equals(id)) {
                return new Keyword(value, buildSpan(off));
            }
        }
        return new Identifier(id, buildSpan(off));
    }

    private Token lexNumber() {
        if (isHexPrefix()) {
            int off = 2;
            while (hasMore(off) && isHex(peek(off))) {
                off++;
            }
            if (off == 2) {
                // 0x without any further hex digits
                return new ErrorToken(this.source.substring(this.pos, this.pos + off), buildSpan(2));
            }
            return new NumberLiteral(this.source.substring(this.pos, this.pos + off), 16, buildSpan(off));
        }
        int off = 1;
        while (hasMore(off) && isNumeric(peek(off))) {
            off++;
        }
        if (peek() == '0' && off > 1) {
            // leading zero is not allowed
            return new ErrorToken(this.source.substring(this.pos, this.pos + off), buildSpan(off));
        }
        return new NumberLiteral(this.source.substring(this.pos, this.pos + off), 10, buildSpan(off));
    }

    private boolean isHexPrefix() {
        return peek() == '0' && hasMore(1) && (peek(1) == 'x' || peek(1) == 'X');
    }

    private boolean isIdentifierChar(char c) {
        return c == '_'
            || c >= 'a' && c <= 'z'
            || c >= 'A' && c <= 'Z'
            || c >= '0' && c <= '9';
    }

    private boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHex(char c) {
        return isNumeric(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private Optional<String> tryLexingBoolean() {
        Optional<String> match = peekAndSearch("true");
        if (match.isPresent()) {
            return match;
        }

        return peekAndSearch("false");
    }

    private Optional<String> peekAndSearch(String value) {
        if (!hasMore(value.length() - 1)) {
            return Optional.empty();
        }

        String peeked = IntStream.range(0, value.length())
                .mapToObj(this::peek)
                .map(String::valueOf)
                .collect(Collectors.joining());
        return peeked.equals(value) ? Optional.of(value) : Optional.empty();
    }

    private Token singleOrFollowedByEq(OperatorType single, OperatorType assign) {
        if (hasMore(1) && peek(1) == '=') {
            return new Operator(assign, buildSpan(2));
        }
        return new Operator(single, buildSpan(1));
    }

    private Token compareOrDoubleOrDoubleAssign(OperatorType compare, OperatorType compareEquals, OperatorType doubly, OperatorType doubleAssign) {
        if (hasMore(1)) {
            char second = peek(1);
            if (second == peek()) {
                return doubleOrAssign(doubly, doubleAssign);
            }
            if (second == '=') {
                return new Operator(compareEquals, buildSpan(2));
            }
        }

        return new Operator(compare, buildSpan(1));
    }

    private Token doubleOrAssign(OperatorType doubly, OperatorType assign) {
        if (hasMore(2) && peek(2) == '=') {
            return new Operator(assign, buildSpan(3));
        }

        return new Operator(doubly, buildSpan(2));
    }

    private Token singleOrAssignOrDouble(OperatorType single, OperatorType assign, OperatorType doubly) {
        if (hasMore(1)) {
            char second = peek(1);
            if (second == peek()) {
                return new Operator(doubly, buildSpan(2));
            }
            if (second == '=') {
                return new Operator(assign, buildSpan(2));
            }
        }

        return new Operator(single, buildSpan(1));
    }

    private Span buildSpan(int proceed) {
        int start = this.pos;
        this.pos += proceed;
        Position.SimplePosition s = new Position.SimplePosition(this.line, start - this.lineStart);
        Position.SimplePosition e = new Position.SimplePosition(this.line, start - this.lineStart + proceed);
        return new Span.SimpleSpan(s, e);
    }

    private char peek() {
        return this.source.charAt(this.pos);
    }

    private boolean hasMore(int offset) {
        return this.pos + offset < this.source.length();
    }

    private char peek(int offset) {
        return this.source.charAt(this.pos + offset);
    }

}
