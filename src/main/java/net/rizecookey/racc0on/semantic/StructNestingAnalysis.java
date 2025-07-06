package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.ast.FieldDeclarationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.type.StructType;
import net.rizecookey.racc0on.parser.visitor.NoOpVisitor;
import net.rizecookey.racc0on.parser.visitor.Unit;

class StructNestingAnalysis implements NoOpVisitor<Namespace<Unit>> {
    private final Namespace<StructDeclarationTree> structs = new Namespace<>();

    @Override
    public Unit visit(ProgramTree programTree, Namespace<Unit> data) {
        programTree.structs().forEach(t -> structs.put(t.name().name(), t));
        programTree.structs().forEach(t -> t.accept(this, data));

        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(StructDeclarationTree structDeclarationTree, Namespace<Unit> data) {
        data.put(structDeclarationTree.name().name(), Unit.INSTANCE);
        structDeclarationTree.fields().forEach(t -> {
            data.openScope();
            t.accept(this, data);
            data.closeScope();
        });
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(FieldDeclarationTree fieldDeclarationTree, Namespace<Unit> data) {
        if (!(fieldDeclarationTree.type().type() instanceof StructType(Name name))) {
            return Unit.INSTANCE;
        }

        if (data.get(name) != null) {
            throw new SemanticException(fieldDeclarationTree.type().span(), fieldDeclarationTree.type().type().asString()
                    + " is declared within itself");
        }

        StructDeclarationTree struct = structs.get(name);
        if (struct == null) {
            throw new SemanticException(fieldDeclarationTree.type().span(),
                    "unknown struct " + fieldDeclarationTree.type().type().asString());
        }
        struct.accept(this, data);

        return Unit.INSTANCE;
    }
}
