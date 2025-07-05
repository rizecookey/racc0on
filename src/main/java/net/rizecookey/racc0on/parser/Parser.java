package net.rizecookey.racc0on.parser;

import net.rizecookey.racc0on.lexer.AmbiguousSymbol;
import net.rizecookey.racc0on.lexer.keyword.AllocKeywordType;
import net.rizecookey.racc0on.lexer.keyword.BuiltinFunctionsKeywordType;
import net.rizecookey.racc0on.lexer.keyword.ComposedTypeKeywordType;
import net.rizecookey.racc0on.lexer.keyword.PointerLiteralKeywordType;
import net.rizecookey.racc0on.parser.ast.FieldDeclarationTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.call.CallTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.PointerLiteralTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueDereferenceTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueFieldAccessTree;
import net.rizecookey.racc0on.parser.type.ArrayType;
import net.rizecookey.racc0on.parser.type.PointerType;
import net.rizecookey.racc0on.parser.type.StructType;
import net.rizecookey.racc0on.semantic.SemanticException;
import net.rizecookey.racc0on.utils.Pair;
import net.rizecookey.racc0on.utils.Position;
import net.rizecookey.racc0on.lexer.BooleanLiteral;
import net.rizecookey.racc0on.lexer.Identifier;
import net.rizecookey.racc0on.lexer.Keyword;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.lexer.keyword.ControlKeywordType;
import net.rizecookey.racc0on.lexer.NumberLiteral;
import net.rizecookey.racc0on.lexer.Operator;
import net.rizecookey.racc0on.lexer.Separator;
import net.rizecookey.racc0on.lexer.Separator.SeparatorType;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.lexer.Token;
import net.rizecookey.racc0on.lexer.keyword.BasicTypeKeywordType;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.control.ControlTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.symbol.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private final TokenSource tokenSource;

    public Parser(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public ProgramTree parseProgram() {
        List<StructDeclarationTree> structs = new ArrayList<>();
        List<FunctionTree> functions = new ArrayList<>();
        while (this.tokenSource.hasMore()) {
            TypeTree type = parseType();
            if (!(type.type() instanceof StructType) || !this.tokenSource.peek().isSeparator(SeparatorType.BRACE_OPEN)) {
                functions.add(parseFunctionPastType(type));
                continue;
            }

            structs.add(parseStructDeclarationPastType(type));
        }
        return new ProgramTree(List.copyOf(structs), List.copyOf(functions));
    }

    private TypeTree parseType() {
        TypeTree currentTree;
        if (this.tokenSource.peek().isKeyword(ComposedTypeKeywordType.STRUCT)) {
            Token token = this.tokenSource.consume();
            Identifier identifier = this.tokenSource.expectIdentifier();
            currentTree = new TypeTree(
                    new StructType(Name.forIdentifier(identifier)),
                    new TypeTree.Struct(name(identifier)),
                    token.span().merge(identifier.span()));
        } else {
            Pair<Keyword, BasicTypeKeywordType> typePair = this.tokenSource.expectBasicType();
            currentTree = new TypeTree(typePair.second().type(), TypeTree.BASE, typePair.first().span());
        }

        for (Token token = this.tokenSource.peek();
             token.isAmbiguous(AmbiguousSymbol.SymbolType.STAR) || token.isSeparator(SeparatorType.BRACKET_OPEN);
             token = this.tokenSource.peek()) {
            switch (token) {
                case AmbiguousSymbol(var type, var span) when type.equals(AmbiguousSymbol.SymbolType.STAR) -> {
                    currentTree = new TypeTree(
                            new PointerType<>(currentTree.type()),
                            new TypeTree.Pointer(currentTree),
                            currentTree.span().merge(span)
                    );
                    this.tokenSource.consume();
                }
                case Separator(var type, _) when type.equals(SeparatorType.BRACKET_OPEN) -> {
                    this.tokenSource.consume();
                    Separator sep = this.tokenSource.expectSeparator(SeparatorType.BRACKET_CLOSE);
                    currentTree = new TypeTree(
                            new ArrayType<>(currentTree.type()),
                            new TypeTree.Array(currentTree),
                            currentTree.span().merge(sep.span())
                    );
                }
                default -> throw new IllegalStateException("Invalid type token");
            }
        }

        return currentTree;
    }

    private StructDeclarationTree parseStructDeclarationPastType(TypeTree type) {
        TypeTree.Kind kind = type.kind();
        if (!(kind instanceof TypeTree.Struct(NameTree name))) {
            throw new SemanticException(type.span(), "expected struct type but got " + type.type().asString());
        }
        List<FieldDeclarationTree> fields = parseFieldList();
        Separator sep = this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        return new StructDeclarationTree(name, fields, type.span().merge(sep.span()));
    }

    private List<FieldDeclarationTree> parseFieldList() {
        this.tokenSource.expectSeparator(SeparatorType.BRACE_OPEN);
        List<FieldDeclarationTree> fields = new ArrayList<>();
        while (!this.tokenSource.peek().isSeparator(SeparatorType.BRACE_CLOSE)) {
            fields.add(parseField());
            this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        }
        this.tokenSource.expectSeparator(SeparatorType.BRACE_CLOSE);
        return List.copyOf(fields);
    }

    private FieldDeclarationTree parseField() {
        TypeTree type = parseType();
        NameTree name = name(this.tokenSource.expectIdentifier());

        return new FieldDeclarationTree(type, name);
    }

    private FunctionTree parseFunctionPastType(TypeTree type) {
        Identifier identifier = this.tokenSource.expectIdentifier();

        List<ParameterTree> parameters = parseParameterList();
        BlockTree body = parseBlock();
        return new FunctionTree(
                type,
                name(identifier),
                parameters,
                body
        );
    }

    private List<ParameterTree> parseParameterList() {
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_CLOSE)) {
            this.tokenSource.consume();
            return List.of();
        }
        List<ParameterTree> parameters = new ArrayList<>();
        parameters.add(parseParameter(0));
        while (this.tokenSource.peek().isSeparator(SeparatorType.COMMA)) {
            this.tokenSource.consume();
            parameters.add(parseParameter(parameters.size()));
        }
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);

        return List.copyOf(parameters);
    }

    private ParameterTree parseParameter(int index) {
        TypeTree type = parseType();
        Identifier identifier = this.tokenSource.expectIdentifier();

        return new ParameterTree(index, type, name(identifier));
    }

    private BlockTree parseBlock() {
        Separator bodyOpen = this.tokenSource.expectSeparator(SeparatorType.BRACE_OPEN);
        List<StatementTree> statements = new ArrayList<>();
        while (!(this.tokenSource.peek() instanceof Separator sep && sep.type() == SeparatorType.BRACE_CLOSE)) {
            statements.add(parseStatement());
        }
        Separator bodyClose = this.tokenSource.expectSeparator(SeparatorType.BRACE_CLOSE);
        return new BlockTree(statements, bodyOpen.span().merge(bodyClose.span()));
    }

    private StatementTree parseStatement() {
        StatementTree statement;
        if (this.tokenSource.peek().isControlKeyword()) {
            statement = parseControl();
        } else if (this.tokenSource.peek().isSeparator(SeparatorType.BRACE_OPEN)) {
            statement = parseBlock();
        } else {
            statement = parseSimple();
            this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        }
        return statement;
    }

    private SimpleStatementTree parseDeclaration() {
        TypeTree type = parseType();
        Identifier ident = this.tokenSource.expectIdentifier();
        ExpressionTree expr = null;
        if (this.tokenSource.peek().isOperator(OperatorType.Assignment.DEFAULT)) {
            this.tokenSource.expectOperator(OperatorType.Assignment.DEFAULT);
            expr = parseExpression();
        }
        return new DeclarationTree(type, name(ident), expr);
    }

    private SimpleStatementTree parseSimple() {
        Token next = this.tokenSource.peek();
        if (next.isTypeKeyword()) {
            return parseDeclaration();
        }

        if (next instanceof Keyword(BuiltinFunctionsKeywordType type, _)) {
            this.tokenSource.consume();
            Pair<List<ExpressionTree>, Span> argsPair = parseArgumentList();
            return new BuiltinCallTree(type, List.copyOf(argsPair.first()), next.span().merge(argsPair.second()));
        }

        if (next instanceof Keyword(AllocKeywordType _, _)) {
            return parseAllocTypeCall();
        }

        LValueTree lValue = parseLValue();
        if (next instanceof Identifier ident && this.tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
            Pair<List<ExpressionTree>, Span> argsPair = parseArgumentList();
            return new FunctionCallTree(name(ident), List.copyOf(argsPair.first()), next.span().merge(argsPair.second()));
        }

        Operator assignmentOperator = parseAssignmentOperator();
        OperatorType.Assignment assignment = assignmentOperator.type().as(OperatorType.Assignment.class).orElseThrow();
        ExpressionTree expression = parseExpression();
        return new AssignmentTree(lValue, expression, assignment);
    }

    private CallTree parseAllocTypeCall() {
        Token token = this.tokenSource.consume();
        if (!(token instanceof Keyword(AllocKeywordType allocType, Span span))) {
            throw new ParseException(token.span(), "expected allocation type call but got '" + token.asString() + "'");
        }

        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        TypeTree type = parseType();
        return switch (allocType) {
            case ALLOC -> {
                Separator end = this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield new AllocCallTree(type, token.span().merge(end.span()));
            }
            case ALLOC_ARRAY -> {
                this.tokenSource.expectSeparator(SeparatorType.COMMA);
                ExpressionTree count = parseExpression();
                Separator end = this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield new AllocArrayCallTree(type, count, token.span().merge(end.span()));
            }
        };
    }

    private Operator parseAssignmentOperator() {
        if (this.tokenSource.peek() instanceof Operator op && op.type().isAssignment()) {
            this.tokenSource.consume();
            return op;
        }
        Token token = this.tokenSource.peek();
        throw new ParseException(token.span(), "expected assignment but got '" + token.asString() + "'");
    }

    private LValueTree parseLValue() {
        LValueTree baseLValue = parseBaseLValue();

        return switch (this.tokenSource.peek()) {
            case Separator(SeparatorType type, _) when type.equals(SeparatorType.BRACKET_OPEN) -> {
                this.tokenSource.consume();
                ExpressionTree index = parseExpression();
                Span end = this.tokenSource.expectSeparator(SeparatorType.BRACKET_CLOSE).span();
                yield new LValueArrayAccessTree(baseLValue, index, baseLValue.span().merge(end));
            }
            case Operator(OperatorType.Pointer type, _) when !type.equals(OperatorType.Pointer.DEREFERENCE) -> {
                Token token = this.tokenSource.consume();
                NameTree field = name(this.tokenSource.expectIdentifier());

                yield switch (type) {
                    case ARROW -> new LValueFieldAccessTree(
                            new LValueDereferenceTree(baseLValue, baseLValue.span().merge(token.span())),
                            field
                    );
                    case FIELD_ACCESS -> new LValueFieldAccessTree(baseLValue, field);
                    case DEREFERENCE -> throw new IllegalStateException("unreachable");
                };
            }
            default -> baseLValue;
        };
    }

    private LValueTree parseBaseLValue() {
        Token token = peekUnary();
        return switch (token) {
            case Separator(SeparatorType type, _) when type.equals(SeparatorType.PAREN_OPEN) -> {
                this.tokenSource.consume();
                LValueTree inner = parseLValue();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield inner;
            }
            case Operator(OperatorType.Pointer type, _) when type.equals(OperatorType.Pointer.DEREFERENCE) -> {
                this.tokenSource.consume();
                LValueTree inner = parseBaseLValue(); // highest precedence!
                yield new LValueDereferenceTree(inner, token.span().merge(inner.span()));
            }
            default -> {
                Identifier identifier = this.tokenSource.expectIdentifier();
                yield new LValueIdentTree(name(identifier));
            }
        };
    }

    private ControlTree parseControl() {
        var result = this.tokenSource.expectControl();
        Keyword keyword = result.first();
        Position start = keyword.span().start();
        ControlKeywordType controlType = result.second();
        return switch (controlType) {
            case IF -> {
                this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
                ExpressionTree condition = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                StatementTree then = parseStatement();
                StatementTree elsey = null;
                if (this.tokenSource.peek().isKeyword(ControlKeywordType.ELSE)) {
                    this.tokenSource.expectKeyword(ControlKeywordType.ELSE);
                    elsey = parseStatement();
                }

                yield new IfElseTree(condition, then, elsey, start);
            }
            case ELSE -> throw new ParseException(keyword.span(), "found else without preceding if statement");
            case WHILE -> {
                this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
                ExpressionTree condition = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                StatementTree body = parseStatement();
                yield new WhileTree(condition, body, start);
            }
            case FOR -> {
                this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
                SimpleStatementTree initializer = null;
                if (!this.tokenSource.peek().isSeparator(SeparatorType.SEMICOLON)) {
                    initializer = parseSimple();
                }
                this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
                ExpressionTree condition = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
                SimpleStatementTree step = null;
                if (!this.tokenSource.peek().isSeparator(SeparatorType.PAREN_CLOSE)) {
                    step = parseSimple();
                }
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                StatementTree body = parseStatement();
                yield new ForTree(initializer, condition, body, step, start);
            }
            case BREAK -> {
                this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
                yield new LoopControlTree(LoopControlTree.Type.BREAK, keyword.span());
            }
            case CONTINUE -> {
                this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
                yield new LoopControlTree(LoopControlTree.Type.CONTINUE, keyword.span());
            }
            case RETURN -> {
                ExpressionTree expression = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
                yield new ReturnTree(expression, keyword.span().start());
            }
        };
    }

    private ExpressionTree parseExpression() {
        ExpressionTree current = parseExpression(0);
        if (this.tokenSource.peek() instanceof Operator(OperatorType type, _) && type.equals(OperatorType.Ternary.IF_BRANCH)) {
            this.tokenSource.expectOperator(OperatorType.Ternary.IF_BRANCH);
            ExpressionTree ifBranch = parseExpression();
            this.tokenSource.expectOperator(OperatorType.Ternary.ELSE_BRANCH);
            ExpressionTree elseBranch = parseExpression();
            return new TernaryExpressionTree(current, ifBranch, elseBranch);
        } else {
            return current;
        }
    }

    private ExpressionTree parseExpression(int level) {
        if (level >= OperatorType.Binary.PRECEDENCE.size()) {
            return parseFactor();
        }

        ExpressionTree lhs = parseExpression(level + 1);
        while (true) {
            Token token = peekBinary();
            Optional<OperatorType.Binary> binary;
            if (token instanceof Operator(OperatorType type, _) && (binary = type.as(OperatorType.Binary.class)).isPresent()
                    && OperatorType.Binary.PRECEDENCE.get(level).contains(binary.get())) {
                this.tokenSource.consume();
                lhs = new BinaryOperationTree(lhs, parseExpression(level + 1), binary.get());
            } else {
                return lhs;
            }
        }
    }

    private ExpressionTree parseFactor() {
        ExpressionTree baseExp = parseBaseFactor();

        return switch (this.tokenSource.peek()) {
            case Separator(var type, _) when type.equals(SeparatorType.BRACKET_OPEN) -> {
                this.tokenSource.consume();
                ExpressionTree index = parseExpression();
                Span end = this.tokenSource.expectSeparator(SeparatorType.BRACKET_CLOSE).span();
                yield new ExpArrayAccessTree(baseExp, index, baseExp.span().merge(end));
            }
            case Operator(OperatorType.Pointer type, _) when !type.equals(OperatorType.Pointer.DEREFERENCE) -> parseBinaryPointerOp(baseExp);
            default -> baseExp;
        };
    }

    private ExpressionTree parseBinaryPointerOp(ExpressionTree base) {
        Token token = this.tokenSource.peek();
        if (!(token instanceof Operator(OperatorType.Pointer type, _))) {
            throw new ParseException(token.span(), "expected binary pointer operation but got '" + token.asString() + "'");
        }
        this.tokenSource.consume();

        return switch (type) {
            case ARROW -> new ExpFieldAccessTree(new ExpDereferenceTree(base, base.span().merge(token.span())), name(this.tokenSource.expectIdentifier()));
            case FIELD_ACCESS -> {
                NameTree field = name(this.tokenSource.expectIdentifier());
                yield new ExpFieldAccessTree(base, field);
            }
            case DEREFERENCE -> throw new ParseException(token.span(), "expected binary pointer operation but got '" + token.asString() + "'");
        };
    }

    private ExpressionTree parseBaseFactor() {
        return switch (peekUnary()) {
            case Separator(var type, _) when type == SeparatorType.PAREN_OPEN -> {
                this.tokenSource.consume();
                ExpressionTree expression = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield expression;
            }
            case Operator(OperatorType.Unary type, _) -> {
                Span span = this.tokenSource.consume().span();
                yield new UnaryOperationTree(type, parseFactor(), span);
            }
            case Operator(OperatorType.Pointer type, _) when type.equals(OperatorType.Pointer.DEREFERENCE) -> {
                Span start = this.tokenSource.consume().span();
                ExpressionTree baseFactor = parseBaseFactor(); // highest precedence!
                yield new ExpDereferenceTree(baseFactor, start.merge(baseFactor.span()));
            }
            case Keyword(BuiltinFunctionsKeywordType type, _) -> {
                Keyword kw = (Keyword) this.tokenSource.consume();
                Pair<List<ExpressionTree>, Span> argsPair = parseArgumentList();

                yield new BuiltinCallTree(type, List.copyOf(argsPair.first()), kw.span().merge(argsPair.second()));
            }
            case Keyword(AllocKeywordType _, _) -> parseAllocTypeCall();
            case Identifier ident -> {
                this.tokenSource.consume();
                if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
                    Pair<List<ExpressionTree>, Span> argsPair = parseArgumentList();
                    yield new FunctionCallTree(name(ident), List.copyOf(argsPair.first()), ident.span().merge(argsPair.second()));
                }

                yield new IdentExpressionTree(name(ident));
            }
            case Keyword(PointerLiteralKeywordType type, Span span) -> {
                this.tokenSource.consume();
                yield new PointerLiteralTree(type, span);
            }
            case NumberLiteral(String value, int base, Span span) -> {
                this.tokenSource.consume();
                yield new IntLiteralTree(value, base, span);
            }
            case BooleanLiteral(boolean value, Span span) -> {
                this.tokenSource.consume();
                yield new BoolLiteralTree(value, span);
            }
            case Token t -> throw new ParseException(t.span(), "invalid factor " + t.asString());
        };
    }

    private Pair<List<ExpressionTree>, Span> parseArgumentList() {
        Separator start = this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_CLOSE)) {
            Span span = start.span().merge(this.tokenSource.consume().span());
            return new Pair<>(List.of(), span);
        }

        List<ExpressionTree> args = new ArrayList<>();
        args.add(parseExpression());

        while (this.tokenSource.peek().isSeparator(SeparatorType.COMMA)) {
            this.tokenSource.consume();
            args.add(parseExpression());
        }

        Token end = this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        return new Pair<>(List.copyOf(args), start.span().merge(end.span()));
    }

    /** Disambiguate any ambiguous tokens to their binary counterpart. **/
    private Token peekBinary() {
        Token token = this.tokenSource.peek();
        if (token instanceof AmbiguousSymbol(var type, var span)) {
            return switch (type) {
                case STAR -> new Operator(OperatorType.Binary.MUL, span);
                case MINUS -> new Operator(OperatorType.Binary.MINUS, span);
            };
        }

        return token;
    }

    /** Disambiguate any ambiguous tokens to their unary counterpart. **/
    private Token peekUnary() {
        Token token = this.tokenSource.peek();
        if (token instanceof AmbiguousSymbol(var type, var span)) {
            return switch (type) {
                case STAR -> new Operator(OperatorType.Pointer.DEREFERENCE, span);
                case MINUS -> new Operator(OperatorType.Unary.NEGATION, span);
            };
        }

        return token;
    }

    private static NameTree name(Identifier ident) {
        return new NameTree(Name.forIdentifier(ident), ident.span());
    }
}
