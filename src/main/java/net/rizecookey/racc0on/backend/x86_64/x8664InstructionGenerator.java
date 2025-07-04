package net.rizecookey.racc0on.backend.x86_64;

import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664ShiftOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664CallOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664EmptyOpLike;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664IfElseOpLike;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664LoadParamPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OpLike;
import net.rizecookey.racc0on.backend.x86_64.optimization.x8664InstrOptimization;
import net.rizecookey.racc0on.backend.x86_64.optimization.x8664JumpOptimization;
import net.rizecookey.racc0on.ir.node.BuiltinCallNode;
import net.rizecookey.racc0on.ir.node.CallNode;
import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.ParameterNode;
import net.rizecookey.racc0on.ir.node.operation.binary.AddNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BitwiseAndNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BitwiseOrNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BitwiseXorNode;
import net.rizecookey.racc0on.ir.node.operation.binary.DivNode;
import net.rizecookey.racc0on.ir.node.operation.binary.EqNode;
import net.rizecookey.racc0on.ir.node.operation.binary.GreaterNode;
import net.rizecookey.racc0on.ir.node.operation.binary.GreaterOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.binary.LessNode;
import net.rizecookey.racc0on.ir.node.operation.binary.LessOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ModNode;
import net.rizecookey.racc0on.ir.node.operation.binary.MulNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.ReturnNode;
import net.rizecookey.racc0on.ir.node.StartNode;
import net.rizecookey.racc0on.ir.node.operation.binary.NotEqNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ShiftLeftNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ShiftRightNode;
import net.rizecookey.racc0on.ir.node.operation.binary.SubNode;
import net.rizecookey.racc0on.ir.node.operation.unary.NotNode;
import net.rizecookey.racc0on.backend.instruction.InstructionBlock;
import net.rizecookey.racc0on.backend.instruction.InstructionGenerator;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.operation.OperationBlock;
import net.rizecookey.racc0on.backend.operation.OperationSchedule;
import net.rizecookey.racc0on.backend.store.LivenessMap;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequests;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664AddOp;
import net.rizecookey.racc0on.backend.x86_64.operation.compare.x8664EqOp;
import net.rizecookey.racc0on.backend.x86_64.operation.compare.x8664GreaterEqOp;
import net.rizecookey.racc0on.backend.x86_64.operation.compare.x8664GreaterOp;
import net.rizecookey.racc0on.backend.x86_64.operation.compare.x8664LessEqOp;
import net.rizecookey.racc0on.backend.x86_64.operation.compare.x8664LessOp;
import net.rizecookey.racc0on.backend.x86_64.operation.compare.x8664NotEqOp;
import net.rizecookey.racc0on.backend.x86_64.operation.logic.x8664AndOp;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664DivPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664EnterOp;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664IMulOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664JumpOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664LoadConstPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664ModPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.logic.x8664NotOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.logic.x8664OrOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664PhiMoveOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664RetOp;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664SubOp;
import net.rizecookey.racc0on.backend.x86_64.operation.logic.x8664XorOp;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreAllocator;
import net.rizecookey.racc0on.ir.schedule.SsaSchedule;
import net.rizecookey.racc0on.ir.util.NodeSupport;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

public class x8664InstructionGenerator implements InstructionGenerator<x8664Instr> {
    private final SsaSchedule schedule;
    private final List<x8664Instr> instructions;
    private final x8664CodeGenerator codeGenerator;
    private final Map<StoreReference<x8664Store>, x8664Store> locations;
    private final SequencedSet<x8664Store> writtenTo;
    private final Map<Block, String> blockLabels;
    private int stackSize;
    /// rsp % 16
    private int stackMisalignment;
    private @Nullable x8664Op currentOp;
    private LivenessMap<x8664Op, x8664Store> livenessMap;

    public x8664InstructionGenerator(x8664CodeGenerator codeGenerator, SsaSchedule schedule) {
        this.schedule = schedule;
        this.codeGenerator = codeGenerator;
        this.locations = new HashMap<>();
        this.writtenTo = new LinkedHashSet<>();
        this.stackSize = 0;

        this.instructions = new ArrayList<>();
        this.blockLabels = new HashMap<>();
        this.livenessMap = new LivenessMap<>();
    }

    public x8664CodeGenerator codeGenerator() {
        return codeGenerator;
    }

    public List<InstructionBlock<x8664Instr>> generateInstructions() {
        labelBlocks();

        StoreRequests<x8664Op, x8664Store> requestService = new StoreRequests<>();
        Map<String, List<x8664Op>> operations = generateOperations(requestService);
        OperationSchedule<x8664Op> opSchedule = createOperationSchedule(operations);

        x8664StoreAllocator allocator = new x8664StoreAllocator();
        x8664StoreAllocator.Allocation allocation = allocator.allocate(opSchedule, requestService);
        locations.putAll(allocation.allocations());
        writtenTo.addAll(locations.values());
        stackSize = allocation.stackSize();
        livenessMap = allocation.livenessMap();

        List<InstructionBlock<x8664Instr>> blocks = new ArrayList<>();
        for (OperationBlock<x8664Op> opBlock : opSchedule.blocks().values()) {
            opBlock.operations().forEach(op -> {
                currentOp = op;
                op.write(this, ref -> Optional.ofNullable(locations.get(ref)));
            });
            blocks.add(new InstructionBlock<>(opBlock.label(), new ArrayList<>(instructions)));
            instructions.clear();
        }

        applyOptimizations(blocks);

        return blocks;
    }

    private void labelBlocks() {
        String procedureName = schedule.programGraph().name();
        for (Block block : schedule.blockSchedule()) {
            String label = block == schedule.programGraph().startBlock() ? procedureName : procedureName + "$" + block.id();
            blockLabels.put(block, label);
        }
    }

    private Map<String, List<x8664Op>> generateOperations(StoreRequests<x8664Op, x8664Store> storeRequests) {
        Map<String, List<x8664Op>> operations = new HashMap<>();
        for (Block block : schedule.blockSchedule()) {
            String label = blockLabels.get(block);
            List<x8664Op> blockOperations = new ArrayList<>();
            for (Node node : schedule.nodeSchedules().get(block)) {
                List<x8664Op> nodeOperations = selectOperations(node);
                nodeOperations.forEach(op -> op.requestStores(storeRequests));
                blockOperations.addAll(nodeOperations);
            }

            operations.put(label, blockOperations);
        }

        return operations;
    }

    private OperationSchedule<x8664Op> createOperationSchedule(Map<String, List<x8664Op>> operations) {
        SequencedMap<String, OperationBlock<x8664Op>> blocks = new LinkedHashMap<>();
        OperationBlock<x8664Op> entry = null;
        Set<OperationBlock<x8664Op>> exits = new HashSet<>();
        for (Block block : schedule.blockSchedule()) {
            String label = label(block);
            List<x8664Op> blockOps = List.copyOf(operations.getOrDefault(label, List.of()));

            if (blockOps.isEmpty()) {
                continue;
            }

            var opBlock = new OperationBlock<>(label, blockOps);
            blocks.put(label, opBlock);

            if (blockOps.stream().anyMatch(op -> op instanceof x8664EnterOp)) {
                entry = opBlock;
            }

            if (blockOps.stream().anyMatch(op -> op instanceof x8664RetOp)) {
                exits.add(opBlock);
            }
        }

        if (entry == null) {
            throw new IllegalStateException("No entry block found");
        }

        if (exits.isEmpty()) {
            throw new IllegalStateException("No exit block found");
        }

        return new OperationSchedule<>(blocks, entry, Collections.unmodifiableSet(exits));
    }

    public SequencedSet<StoreReference<x8664Store>> getLiveReferences() {
        if (currentOp == null) {
            throw new IllegalStateException("Not currently writing an operation");
        }
        return livenessMap.getLiveAt(currentOp);
    }

    public SequencedSet<x8664Store> getLiveStores() {
        return getLiveReferences().stream()
                .map(locations::get)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String printAssembly(List<x8664Instr> instructions) {
        return instructions.stream()
                .map(x8664Instr::toAssembly)
                .collect(Collectors.joining("\n"));
    }

    private void applyOptimizations(List<InstructionBlock<x8664Instr>> instructions) {
        List<x8664InstrOptimization> optimizations = List.of(new x8664JumpOptimization());

        for (var optimization : optimizations) {
            optimization.accept(instructions);
        }
    }

    public void prepareStack() {
        write(x8664InstrType.PUSH, x8664Operand.Size.QUAD_WORD, x8664Register.RBP);
        write(x8664InstrType.MOV, x8664Operand.Size.QUAD_WORD, x8664Register.RBP, x8664Register.RSP);
        if (stackSize > 0) {
            write(x8664InstrType.SUB, x8664Operand.Size.QUAD_WORD, x8664Register.RSP, new x8664Immediate(stackSize));
        }
        stackMisalignment = stackSize % 16;
    }

    public SequencedSet<x8664Store> getWrittenTo() {
        return Collections.unmodifiableSequencedSet(writtenTo);
    }

    public void tearDownStack() {
        write(x8664InstrType.LEAVE);
    }

    public List<x8664Op> selectOperations(Node node) {
        List<x8664Op> operations = node instanceof JumpNode || node instanceof IfNode
                ? schedule.phiMoves().getOrDefault(node.block(), List.of())
                .stream()
                .map(entry -> new x8664PhiMoveOp(entry.first(), entry.second()))
                .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();

        x8664OpLike base = switch (node) {
            case StartNode _ -> new x8664EnterOp();
            case AddNode addNode -> new x8664AddOp(extractOperands(addNode));
            case SubNode subNode -> new x8664SubOp(extractOperands(subNode));
            case MulNode mulNode -> new x8664IMulOp(extractOperands(mulNode));
            case DivNode divNode -> new x8664DivPhantomOp(extractOperands(divNode));
            case ModNode modNode -> new x8664ModPhantomOp(extractOperands(modNode));
            case ConstIntNode constIntNode -> new x8664LoadConstPhantomOp(constIntNode);
            case ConstBoolNode constBoolNode -> new x8664LoadConstPhantomOp(constBoolNode);
            case ReturnNode returnNode ->
                    new x8664RetOp(NodeSupport.predecessorsSkipProj(returnNode).get(ReturnNode.RESULT));
            case JumpNode jumpNode -> new x8664JumpOp(label(jumpNode.target()));
            case IfNode ifNode -> {
                Map<Boolean, Block> targets = ifNode.targets();
                Block trueTarget = targets.get(true);
                Block falseTarget = targets.get(false);

                yield new x8664IfElseOpLike(NodeSupport.predecessorSkipProj(ifNode, IfNode.CONDITION), true, label(falseTarget),
                        label(trueTarget));
            }
            case BitwiseAndNode bitwiseAndNode -> new x8664AndOp(extractOperands(bitwiseAndNode));
            case BitwiseOrNode bitwiseOrNode -> new x8664OrOp(extractOperands(bitwiseOrNode));
            case BitwiseXorNode bitwiseXorNode -> new x8664XorOp(extractOperands(bitwiseXorNode));
            case ShiftLeftNode shiftLeftNode -> new x8664ShiftOp(x8664ShiftOp.Direction.LEFT, extractOperands(shiftLeftNode));
            case ShiftRightNode shiftRightNode -> new x8664ShiftOp(x8664ShiftOp.Direction.RIGHT, extractOperands(shiftRightNode));
            case NotNode notNode -> new x8664NotOp(notNode, NodeSupport.predecessorSkipProj(notNode, NotNode.IN));
            case EqNode eqNode -> new x8664EqOp(extractOperands(eqNode));
            case NotEqNode notEqNode -> new x8664NotEqOp(extractOperands(notEqNode));
            case GreaterNode greaterNode -> new x8664GreaterOp(extractOperands(greaterNode));
            case GreaterOrEqNode greaterOrEqNode -> new x8664GreaterEqOp(extractOperands(greaterOrEqNode));
            case LessNode lessNode -> new x8664LessOp(extractOperands(lessNode));
            case LessOrEqNode lessOrEqNode -> new x8664LessEqOp(extractOperands(lessOrEqNode));
            case ParameterNode parameterNode -> new x8664LoadParamPhantomOp(parameterNode);
            case CallNode callNode -> new x8664CallOp(callNode);
            case BuiltinCallNode builtinCallNode -> new x8664CallOp(builtinCallNode);
            case Phi _, Block _, ProjNode _ -> new x8664EmptyOpLike();
        };
        operations.addAll(base.asOperations());

        return operations;
    }

    private Operands.Binary<Node> extractOperands(BinaryOperationNode node) {
        List<Node> realPreds = NodeSupport.predecessorsSkipProj(node);
        return new Operands.Binary<>(node, realPreds.get(BinaryOperationNode.LEFT), realPreds.get(BinaryOperationNode.RIGHT));
    }

    public String label(Block block) {
        return blockLabels.get(block);
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

    public void write(x8664InstrType type,
                      x8664Operand.Size firstSize, x8664Operand.Size secondSize,
                      x8664Operand first, x8664Operand second) {
        instructions.add(new x8664Instr.Binary(type, first, second, firstSize, secondSize));
    }

    public void move(x8664Store to, x8664Operand from, x8664Operand.Size size) {
        if (to.equals(from)) {
            return;
        }
        x8664Operand actualFrom = from;
        if (to instanceof x8664StackStore && from instanceof x8664StackStore) {
            actualFrom = x8664Register.MEMORY_ACCESS_RESERVE;
            write(x8664InstrType.MOV, size, x8664Register.MEMORY_ACCESS_RESERVE, from);
        }

        write(x8664InstrType.MOV, size, to, actualFrom);
    }

    public void test(x8664Store first, x8664Store second) {
        if (second instanceof x8664StackStore stackStore) {
            second = x8664Register.MEMORY_ACCESS_RESERVE;
            move(second, stackStore, x8664Operand.Size.BYTE);
        }

        write(x8664InstrType.TEST, x8664Operand.Size.BYTE, first, second);
    }

    public void push(x8664Operand operand) {
        stackMisalignment = (stackMisalignment + 8) % 16;
        write(x8664InstrType.PUSH, x8664Operand.Size.QUAD_WORD, operand);
    }

    public void pop(x8664Operand operand) {
        stackMisalignment = Math.abs(stackMisalignment - 8) % 16;
        write(x8664InstrType.POP, x8664Operand.Size.QUAD_WORD, operand);
    }

    public void call(String label, x8664Store result, List<x8664Operand> arguments, Map<x8664Register, x8664Store> backupStores) {
        int callStackSize = prepareCallStack(arguments);

        Set<x8664Register> backedUp = backupCallerSavedRegisters(backupStores);
        moveArguments(arguments, backedUp, backupStores);

        write(x8664InstrType.CALL, new x8664Label(label));
        tearDownCallStack(callStackSize);

        if (result instanceof x8664Register) {
            backedUp.remove(result);
        }
        if (!result.equals(x8664Register.RAX)) {
            move(result, x8664Register.RAX, x8664Operand.Size.QUAD_WORD);
        }
        restoreRegisters(backedUp, backupStores);
    }

    /** @return the size of the call stack **/
    private int prepareCallStack(List<x8664Operand> arguments) {
        int stackArgumentCount = Math.max(0, arguments.size() - x8664Register.ARGUMENT_REGISTERS.size());
        int callStackSize = stackArgumentCount * 8;
        int misalignmentAfter = (stackMisalignment + stackArgumentCount * 8) % 16;
        if (misalignmentAfter != 0) {
            write(x8664InstrType.SUB, x8664Store.Size.QUAD_WORD, x8664Register.RSP, new x8664Immediate(16 - misalignmentAfter));
        }
        callStackSize += misalignmentAfter;

        return callStackSize;
    }

    private void tearDownCallStack(int size) {
        if (size > 0) {
            write(x8664InstrType.ADD, x8664Store.Size.QUAD_WORD, x8664Register.RSP, new x8664Immediate(size));
        }
    }

    /** @return the registers that were backed up */
    private Set<x8664Register> backupCallerSavedRegisters(Map<x8664Register, x8664Store> backupStores) {
        Set<x8664Register> backedUp = new HashSet<>();
        Set<x8664Store> live = getLiveStores();
        x8664Register.getRegisterSet().stream()
                .filter(x8664Register::isCallerSaved)
                .forEach(argReg -> {
                    if (!live.contains(argReg)) {
                        return;
                    }
                    if (!backupStores.containsKey(argReg)) {
                        throw new IllegalStateException("No backup store found");
                    }
                    move(backupStores.get(argReg), argReg, x8664Operand.Size.QUAD_WORD);
                    backedUp.add(argReg);
                });

        return backedUp;
    }

    private void restoreRegisters(Set<x8664Register> registers, Map<x8664Register, x8664Store> backupStores) {
        for (x8664Register register : registers) {
            move(register, backupStores.get(register), x8664Operand.Size.QUAD_WORD);
        }
    }

    private void moveArguments(List<x8664Operand> arguments, Set<x8664Register> backedUp,
                               Map<x8664Register, x8664Store> backupStores) {
        Set<x8664Register> writtenTo = new HashSet<>();
        Set<x8664Operand> sourcesSet = new HashSet<>(arguments);

        int registerArguments = Math.min(x8664Register.ARGUMENT_REGISTERS.size(), arguments.size());

        for (int i = 0; i < registerArguments; i++) {
            x8664Register target = x8664Register.ARGUMENT_REGISTERS.get(i);
            x8664Operand source = arguments.get(i);
            if (target.equals(source)) {
                continue;
            }

            if (source instanceof x8664Register sourceReg && writtenTo.contains(sourceReg)) {
                source = backupStores.get(source);
            }

            if (sourcesSet.contains(target) && writtenTo.add(target) && !backedUp.contains(target)) {
                move(backupStores.get(target), target, x8664Operand.Size.QUAD_WORD);
            }

            move(target, source, x8664Operand.Size.QUAD_WORD);
        }

        for (int i = arguments.size() - 1; i >= registerArguments; i--) {
            x8664Operand source = arguments.get(i);
            if (source instanceof x8664Register && writtenTo.contains(source)) {
                source = backupStores.get(source);
            }

            push(source);
        }
    }
}
