package net.rizecookey.racc0on.parser;

import net.rizecookey.racc0on.parser.ast.ParameterTree;
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
import net.rizecookey.racc0on.lexer.keyword.TypeKeywordType;
import net.rizecookey.racc0on.parser.ast.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.ControlTree;
import net.rizecookey.racc0on.parser.ast.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.LValueTree;
import net.rizecookey.racc0on.parser.ast.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.SimpleStatementTree;
import net.rizecookey.racc0on.parser.ast.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.UnaryOperationTree;
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
        List<FunctionTree> functions = new ArrayList<>();
        while (this.tokenSource.hasMore()) {
            functions.add(parseFunction());
        }
        return new ProgramTree(List.copyOf(functions));
    }

    private FunctionTree parseFunction() {
        TypeTree type = parseType();
        Identifier identifier = this.tokenSource.expectIdentifier();

        List<ParameterTree> parameters = List.of();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        if (!this.tokenSource.peek().isSeparator(SeparatorType.PAREN_CLOSE)) {
            parameters = parseParameterList();
        }
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree body = parseBlock();
        return new FunctionTree(
            type,
            name(identifier),
            parameters,
            body
        );
    }

    private TypeTree parseType() {
        Pair<Keyword, TypeKeywordType> typePair = this.tokenSource.expectType();
        return new TypeTree(typePair.second().type(), typePair.first().span());
    }

    private List<ParameterTree> parseParameterList() {
        List<ParameterTree> parameters = new ArrayList<>();
        parameters.add(parseParameter());
        while (this.tokenSource.peek().isSeparator(SeparatorType.COMMA)) {
            this.tokenSource.expectSeparator(SeparatorType.COMMA);
            parameters.add(parseParameter());
        }

        return List.copyOf(parameters);
    }

    private ParameterTree parseParameter() {
        TypeTree type = parseType();
        Identifier identifier = this.tokenSource.expectIdentifier();

        return new ParameterTree(type, name(identifier));
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
        if (this.tokenSource.peek().isTypeKeyword()) {
            return parseDeclaration();
        }

        LValueTree lValue = parseLValue();
        Operator assignmentOperator = parseAssignmentOperator();
        OperatorType.Assignment assignment = assignmentOperator.type().as(OperatorType.Assignment.class).orElseThrow();
        ExpressionTree expression = parseExpression();
        return new AssignmentTree(lValue, expression, assignment);
    }

    private Operator parseAssignmentOperator() {
        if (this.tokenSource.peek() instanceof Operator op && op.type().isAssignment()) {
            this.tokenSource.consume();
            return op;
        }
        Token token = this.tokenSource.peek();
        throw new ParseException(token.span(), "expected assignment but got " + token.asString());
    }

    private LValueTree parseLValue() {
        if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
            this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
            LValueTree inner = parseLValue();
            this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
            return inner;
        }
        Identifier identifier = this.tokenSource.expectIdentifier();
        return new LValueIdentTree(name(identifier));
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
            Optional<OperatorType.Binary> binary;
            if (this.tokenSource.peek() instanceof Operator(OperatorType type, _) && (binary = type.as(OperatorType.Binary.class)).isPresent()
                    && OperatorType.Binary.PRECEDENCE.get(level).contains(binary.get())) {
                this.tokenSource.consume();
                lhs = new BinaryOperationTree(lhs, parseExpression(level + 1), binary.get());
            } else {
                return lhs;
            }
        }
    }

    private ExpressionTree parseFactor() {
        return switch (this.tokenSource.peek()) {
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
            case Operator(OperatorType.Ambiguous ambiguous, _) when ambiguous == OperatorType.Ambiguous.MINUS -> {
                Span span = this.tokenSource.consume().span();
                yield new UnaryOperationTree(OperatorType.Unary.NEGATION, parseFactor(), span);
            }
            case Identifier ident -> {
                this.tokenSource.consume();
                yield new IdentExpressionTree(name(ident));
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

    private static NameTree name(Identifier ident) {
        return new NameTree(Name.forIdentifier(ident), ident.span());
    }
}
