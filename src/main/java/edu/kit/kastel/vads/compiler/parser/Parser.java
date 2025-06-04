package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.lexer.BooleanLiteral;
import edu.kit.kastel.vads.compiler.lexer.Identifier;
import edu.kit.kastel.vads.compiler.lexer.Keyword;
import edu.kit.kastel.vads.compiler.lexer.OperatorType;
import edu.kit.kastel.vads.compiler.lexer.keyword.ControlKeywordType;
import edu.kit.kastel.vads.compiler.lexer.NumberLiteral;
import edu.kit.kastel.vads.compiler.lexer.Operator;
import edu.kit.kastel.vads.compiler.lexer.Separator;
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.lexer.keyword.TypeKeywordType;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BoolLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.ControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueTree;
import edu.kit.kastel.vads.compiler.parser.ast.IntLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.SimpleStatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TernaryExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.IfElseTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.LoopControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.UnaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.type.BasicType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private final TokenSource tokenSource;

    public Parser(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public ProgramTree parseProgram() {
        ProgramTree programTree = new ProgramTree(List.of(parseFunction()));
        if (this.tokenSource.hasMore()) {
            Token token = this.tokenSource.peek();
            throw new ParseException(token.span(), "expected end of input but got " + token.asString());
        }
        return programTree;
    }

    private FunctionTree parseFunction() {
        Keyword returnType = this.tokenSource.expectKeyword(TypeKeywordType.INT);
        Identifier identifier = this.tokenSource.expectIdentifier();
        if (!identifier.value().equals("main")) {
            throw new ParseException(identifier.span(), "expected main function but got " + identifier.value());
        }
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree body = parseBlock();
        return new FunctionTree(
            new TypeTree(BasicType.INT, returnType.span()),
            name(identifier),
            body
        );
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
        var result = this.tokenSource.expectType();
        Keyword keyword = result.first();
        TypeKeywordType type = result.second();
        Identifier ident = this.tokenSource.expectIdentifier();
        ExpressionTree expr = null;
        if (this.tokenSource.peek().isOperator(OperatorType.Assignment.DEFAULT)) {
            this.tokenSource.expectOperator(OperatorType.Assignment.DEFAULT);
            expr = parseExpression();
        }
        return new DeclarationTree(new TypeTree(type.type(), keyword.span()), name(ident), expr);
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
