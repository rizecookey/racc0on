package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.lexer.Identifier;
import edu.kit.kastel.vads.compiler.lexer.Keyword;
import edu.kit.kastel.vads.compiler.lexer.keyword.ControlKeywordType;
import edu.kit.kastel.vads.compiler.lexer.keyword.KeywordType;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.lexer.Operator;
import edu.kit.kastel.vads.compiler.lexer.OperatorType;
import edu.kit.kastel.vads.compiler.lexer.Separator;
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.lexer.keyword.TypeKeywordType;
import net.rizecookey.racc0on.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TokenSource {
    private final List<Token> tokens;
    private int idx;

    public TokenSource(Lexer lexer) {
        this.tokens = Stream.generate(lexer::nextToken)
            .takeWhile(Optional::isPresent)
            .map(Optional::orElseThrow)
            .toList();
    }

    TokenSource(List<Token> tokens) {
        this.tokens = List.copyOf(tokens);
    }

    public Token peek() {
        expectHasMore();
        return this.tokens.get(this.idx);
    }

    public Pair<Keyword, TypeKeywordType> expectType() {
        Token token = peek();
        if (!(token instanceof Keyword kw) || !kw.isTypeKeyword()) {
            throw new ParseException(token.span(), "expected one of " + Arrays.toString(TypeKeywordType.values()) + " but got " + token.asString());
        }

        this.idx++;
        return new Pair<>(kw, (TypeKeywordType) kw.type());
    }

    public Pair<Keyword, ControlKeywordType> expectControl() {
        Token token = peek();
        if (!(token instanceof Keyword kw) || !kw.isControlKeyword()) {
            throw new ParseException(token.span(), "expected one of " + Arrays.toString(ControlKeywordType.values()) + " but got " + token.asString());
        }

        this.idx++;
        return new Pair<>(kw, (ControlKeywordType) kw.type());
    }

    public Keyword expectKeyword(KeywordType type) {
        Token token = peek();
        if (!(token instanceof Keyword kw) || !kw.type().equals(type)) {
            throw new ParseException(token.span(), "expected keyword '" + type + "' but got " + token.asString());
        }
        this.idx++;
        return kw;
    }

    public Separator expectSeparator(SeparatorType type) {
        Token token = peek();
        if (!(token instanceof Separator sep) || sep.type() != type) {
            throw new ParseException(token.span(), "expected separator '" + type + "' but got " + token.asString());
        }
        this.idx++;
        return sep;
    }

    public Operator expectOperator(OperatorType type) {
        Token token = peek();
        if (!(token instanceof Operator op) || op.type() != type) {
            throw new ParseException(token.span(), "expected operator '" + type + "' but got " + token.asString());
        }
        this.idx++;
        return op;
    }

    public Identifier expectIdentifier() {
        Token token = peek();
        if (!(token instanceof Identifier ident)) {
            throw new ParseException(token.span(), "expected identifier but got " + token.asString());
        }
        this.idx++;
        return ident;
    }

    public Token consume() {
        Token token = peek();
        this.idx++;
        return token;
    }

    public boolean hasMore() {
        return this.idx < this.tokens.size();
    }

    private void expectHasMore() {
        if (this.idx >= this.tokens.size()) {
            throw new ParseException(this.tokens.getLast().span(), "reached end of file but expected more tokens");
        }
    }
}
