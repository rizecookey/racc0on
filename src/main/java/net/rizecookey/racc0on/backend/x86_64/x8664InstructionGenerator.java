package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.node.operation.binary.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.SubNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.unary.UnaryOperationNode;
import net.rizecookey.racc0on.backend.NodeUtils;
import net.rizecookey.racc0on.backend.instruction.InstructionGenerator;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.LivenessMap;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequests;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstructionStream;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664AddOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664DivPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664EmptyOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664EnterOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664IMulOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664LoadConstPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664ModPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664MovOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664RetOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664SubOp;
import net.rizecookey.racc0on.backend.x86_64.optimization.x8664AsmOptimization;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.stream.Collectors;

public class x8664InstructionGenerator implements InstructionGenerator<x8664Instr> {
    private final List<Node> statements;
    private final List<x8664Instr> instructions;
    private final x8664CodeGenerator codeGenerator;
    private final Map<StoreReference<x8664Store>, x8664Store> locations;
    private int stackSize;
    private LivenessMap<x8664Op, x8664Store> livenessMap;

    public x8664InstructionGenerator(x8664CodeGenerator codeGenerator, List<Node> statements) {
        this.statements = statements;
        this.codeGenerator = codeGenerator;
        this.locations = new HashMap<>();
        this.stackSize = 0;

        this.instructions = new ArrayList<>();
        this.livenessMap = new LivenessMap<>();
    }

    public List<x8664Instr> generateInstructions() {
        List<x8664Op> selectedOperations = new ArrayList<>();
        StoreRequests<x8664Op, x8664Store> storeRequests = new StoreRequests<>();
        selectedOperations.add(new x8664EnterOp());
        for (Node node : statements) {
            x8664Op operation = selectOperation(node);
            operation.makeStoreRequests(storeRequests);
            selectedOperations.add(operation);
        }

        x8664StoreAllocator allocator = new x8664StoreAllocator();
        x8664StoreAllocator.Allocation allocation = allocator.allocate(selectedOperations, storeRequests);
        locations.putAll(allocation.allocations());
        stackSize = allocation.stackSize();
        livenessMap = allocation.livenessMap();
        for (var op : selectedOperations) {
            op.write(this, ref -> Optional.ofNullable(locations.get(ref)));
        }

        return performOptimizations();
    }

    public SequencedSet<StoreReference<x8664Store>> getReferencesLiveAt(x8664Op at) {
        return livenessMap.getLiveAt(at);
    }

    public SequencedSet<x8664Store> getLiveAt(x8664Op at) {
        return getReferencesLiveAt(at).stream().map(locations::get).collect(Collectors.toCollection(LinkedHashSet::new));
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
        write(x8664InstrType.PUSH, x8664Operand.Size.QUAD_WORD, x8664Register.RBP);
        write(x8664InstrType.MOV, x8664Operand.Size.QUAD_WORD, x8664Register.RBP, x8664Register.RSP);
        if (stackSize > 0) {
            write(x8664InstrType.SUB, x8664Operand.Size.QUAD_WORD, x8664Register.RSP, new x8664Immediate(stackSize));
        }
    }

    public void tearDownStack() {
        write(x8664InstrType.LEAVE);
    }

    public x8664Op selectOperation(Node node) {
        return switch (node) {
            case AddNode addNode -> new x8664AddOp(extractOperands(addNode));
            case SubNode subNode -> new x8664SubOp(extractOperands(subNode));
            case MulNode mulNode -> new x8664IMulOp(extractOperands(mulNode));
            case DivNode divNode -> new x8664DivPhantomOp(extractOperands(divNode));
            case ModNode modNode -> new x8664ModPhantomOp(extractOperands(modNode));
            case BinaryOperationNode _, UnaryOperationNode _
                    -> throw new IllegalStateException("Operation not supported by x86 backend"); // TODO
            case ConstIntNode constIntNode -> new x8664LoadConstPhantomOp(constIntNode);
            case ReturnNode returnNode -> new x8664RetOp(NodeUtils.shortcutPredecessors(returnNode).get(ReturnNode.RESULT));
            case Phi _ -> throw new IllegalStateException("Phi node not supported");
            case Block _, ProjNode _, StartNode _ -> x8664EmptyOp.INSTANCE;
            default -> throw new IllegalStateException(node + " not implemented by x86 backend"); //TODO
        };
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

    public void move(x8664Store to, x8664Operand from) {
        new x8664MovOp(to, from).write(this, ref -> Optional.ofNullable(locations.get(ref)));
    }

    public void push(x8664Operand operand) {
        write(x8664InstrType.PUSH, x8664Operand.Size.QUAD_WORD, operand);
    }

    public void pop(x8664Operand operand) {
        write(x8664InstrType.POP, x8664Operand.Size.QUAD_WORD, operand);
    }
}
