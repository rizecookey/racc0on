package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import net.rizecookey.racc0on.backend.NodeUtils;
import net.rizecookey.racc0on.backend.instruction.InstructionGenerator;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstructionStream;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664AddOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664DivPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664EmptyOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664IMulOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664ModPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664MovOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664RetOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664SubOp;
import net.rizecookey.racc0on.backend.x86_64.optimization.x8664AsmOptimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class x8664InstructionGenerator implements InstructionGenerator<x8664Instr> {
    private final List<Node> statements;
    private final List<x8664Instr> instructions;
    private final x8664CodeGenerator codeGenerator;
    private final Map<Node, x8664StoreLocation> locations;
    private final int stackSize;

    public x8664InstructionGenerator(x8664CodeGenerator codeGenerator, List<Node> statements, x8664StoreAllocator.Allocation allocation) {
        this.statements = statements;
        this.codeGenerator = codeGenerator;
        this.locations = allocation.allocations();
        this.stackSize = allocation.stackSize();

        this.instructions = new ArrayList<>();
    }

    public List<x8664Instr> generateInstructions() {
        prepareStack();
        for (Node node : statements) {
            selectInstructions(node);
        }

        return performOptimizations();
    }

    public static String printAssembly(List<x8664Instr> instructions) {
        return instructions.stream()
                .map(x8664Instr::toAssembly)
                .collect(Collectors.joining("\n"));
    }

    private List<x8664Instr> performOptimizations() {
        x8664InstructionStream stream = new x8664InstructionStream(instructions);
        List<x8664AsmOptimization> optimizers = List.of();
        int index = 0;

        outerLoop:
        while (index < stream.size()) {
            for (x8664AsmOptimization optimizer : optimizers) {
                optimizer.performOptimization(index);
                if (stream.isMarkedDirty()) {
                    index = stream.getLowestModifiedIndex();
                    stream.resetDirtyMark();
                    continue outerLoop;
                }
            }
            index++;
        }

        return stream.toInstructionList();
    }

    public void prepareStack() {
        write(x8664InstrType.ENTER, new x8664Immediate(stackSize), new x8664Immediate(0));
    }

    public void tearDownStack() {
        write(x8664InstrType.LEAVE);
    }

    public void selectInstructions(Node node) {
        x8664Op operation = switch (node) {
            case AddNode addNode -> new x8664AddOp(extractOperands(addNode));
            case SubNode subNode -> new x8664SubOp(extractOperands(subNode));
            case MulNode mulNode -> new x8664IMulOp(extractOperands(mulNode));
            case DivNode divNode -> new x8664DivPhantomOp(extractOperands(divNode));
            case ModNode modNode -> new x8664ModPhantomOp(extractOperands(modNode));
            case ConstIntNode constIntNode -> new x8664MovOp(locations.get(constIntNode), new x8664Immediate(String.valueOf(constIntNode.value())));
            case ReturnNode returnNode -> new x8664RetOp(NodeUtils.shortcutPredecessors(returnNode).get(ReturnNode.RESULT));
            case Phi _ -> throw new IllegalStateException("Phi node not supported");
            case Block _, ProjNode _, StartNode _ -> x8664EmptyOp.INSTANCE;
        };

        operation.write(this, locations::get);
    }

    private Operands.Binary<Node> extractOperands(BinaryOperationNode node) {
        List<Node> realPreds = NodeUtils.shortcutPredecessors(node);
        return new Operands.Binary<>(node, realPreds.get(BinaryOperationNode.LEFT), realPreds.get(BinaryOperationNode.RIGHT));
    }

    public void write(x8664InstrType type) {
        instructions.add(new x8664Instr.NoOperand(type));
    }

    public void write(x8664InstrType type, x8664Operand operand) {
        write(type, x8664Operand.Size.DOUBLE_WORD, operand);
    }

    public void write(x8664InstrType type, x8664Operand.Size size, x8664Operand operand) {
        instructions.add(new x8664Instr.Unary(type, operand, size));
    }

    public void write(x8664InstrType type, x8664Operand first, x8664Operand second) {
        write(type, x8664Operand.Size.DOUBLE_WORD, first, second);
    }

    public void write(x8664InstrType type, x8664Operand.Size size, x8664Operand first, x8664Operand second) {
        instructions.add(new x8664Instr.Binary(type, first, second, size));
    }

    public void move(x8664StoreLocation to, x8664Operand from) {
        new x8664MovOp(to, from).write(this, locations::get);
    }

    public void push(x8664Operand operand) {
        write(x8664InstrType.PUSH, x8664Operand.Size.QUAD_WORD, operand);
    }

    public void pop(x8664Operand operand) {
        write(x8664InstrType.POP, x8664Operand.Size.QUAD_WORD, operand);
    }
}
