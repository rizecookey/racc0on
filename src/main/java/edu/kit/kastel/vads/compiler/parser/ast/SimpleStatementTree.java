package edu.kit.kastel.vads.compiler.parser.ast;

public sealed interface SimpleStatementTree extends StatementTree permits AssignmentTree, DeclarationTree {
}
