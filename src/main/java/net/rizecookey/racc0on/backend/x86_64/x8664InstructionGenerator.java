package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.node.ConstBoolNode;
import edu.kit.kastel.vads.compiler.ir.node.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.JumpNode;
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
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstructionStream;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664AddOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664ConditionalJumpOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664DivPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664EnterOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664IMulOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664JumpOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664LoadConstPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664ModPhantomOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664MovOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664PhiMoveOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664RetOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664SubOp;
import net.rizecookey.racc0on.backend.x86_64.optimization.x8664AsmOptimization;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreAllocator;
import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.SsaSchedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class x8664InstructionGenerator implements InstructionGenerator<x8664Instr> {
    private final SsaSchedule schedule;
    private final List<x8664Instr> instructions;
    private final x8664CodeGenerator codeGenerator;
    private final Map<StoreReference<x8664Store>, x8664Store> locations;
    private final Map<Block, String> blockLabels;
    private int stackSize;
    private LivenessMap<x8664Op, x8664Store> livenessMap;

    public x8664InstructionGenerator(x8664CodeGenerator codeGenerator, SsaSchedule schedule) {
        this.schedule = schedule;
        this.codeGenerator = codeGenerator;
        this.locations = new HashMap<>();
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
        OperationSchedule<x8664Op> opSchedule = scheduleBlocks(operations);

        x8664StoreAllocator allocator = new x8664StoreAllocator();
        x8664StoreAllocator.Allocation allocation = allocator.allocate(operations, requestService);
        locations.putAll(allocation.allocations());
        stackSize = allocation.stackSize();
        livenessMap = allocation.livenessMap();

        List<InstructionBlock<x8664Instr>> blocks = new ArrayList<>();
        for (OperationBlock<x8664Op> opBlock : opSchedule.blocks().values()) {
            opBlock.operations().forEach(op -> op.write(this, ref -> Optional.ofNullable(locations.get(ref))));
            blocks.add(new InstructionBlock<>(opBlock.label(), List.copyOf(performOptimizations())));
            instructions.clear();
        }

        return blocks;
    }

    private List<Block> scheduleSsaBlocks() {
        SsaBlockScheduler scheduler = new SsaBlockScheduler();
        scheduler.traverse(schedule.programGraph());

        return scheduler.getResult();
    }

    private static class SsaBlockScheduler extends IrGraphTraverser {
        private final List<Block> result = new ArrayList<>();

        @Override
        public Collection<? extends Node> getPredecessors(Node node) {
            return node.predecessors().stream().map(Node::block).collect(Collectors.toSet());
        }

        @Override
        public void consume(Node node) {
            result.add(node.block());
        }

        public List<Block> getResult() {
            return result;
        }
    }

    private Map<String, List<x8664Op>> generateOperations(StoreRequests<x8664Op, x8664Store> storeRequests) {
        Map<String, List<x8664Op>> operations = new HashMap<>();
        for (Block block : schedule.blockSchedules().keySet()) {
            String label = blockLabels.get(block);
            List<x8664Op> blockOperations = new ArrayList<>();
            for (Node node : schedule.blockSchedules().get(block)) {
                List<x8664Op> nodeOperations = selectOperations(node);
                nodeOperations.forEach(op -> op.makeStoreRequests(storeRequests));
                blockOperations.addAll(nodeOperations);
            }

            operations.put(label, blockOperations);
        }

        return operations;
    }

    private OperationSchedule<x8664Op> scheduleBlocks(Map<String, List<x8664Op>> operations) {
        List<Block> ssaBlockSchedule = scheduleSsaBlocks();
        NavigableMap<String, OperationBlock<x8664Op>> blocks = new TreeMap<>();
        OperationBlock<x8664Op> entry = null;
        Set<OperationBlock<x8664Op>> exits = new HashSet<>();
        for (Block block : ssaBlockSchedule) {
            String label = label(block);
            List<x8664Op> blockOps = List.copyOf(operations.getOrDefault(label, List.of()));
            var opBlock = new OperationBlock<>(label, blockOps);
            blocks.put(label, opBlock);

            if (blockOps.stream().anyMatch(op -> op instanceof x8664EnterOp)) {
                entry = opBlock;
            }

            if (blockOps.stream().anyMatch(op -> op instanceof x8664JumpOp || op instanceof x8664ConditionalJumpOp)) {
                exits.add(opBlock);
            }
        }

        if (entry == null) {
            throw new IllegalStateException("No entry block found");
        }

        if (exits.isEmpty()) {
            throw new IllegalStateException("No exit block found");
        }

        return new OperationSchedule<>(blocks, entry, Set.copyOf(exits));
    }

    private void labelBlocks() {
        String procedureName = schedule.programGraph().name();
        int index = 0;
        for (Block block : schedule.blockSchedules().keySet()) {
            String label = block == schedule.programGraph().startBlock() ? procedureName : procedureName + "#" + index++;
            blockLabels.put(block, label);
        }
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

    public List<x8664Op> selectOperations(Node node) {
        List<x8664Op> base = switch (node) {
            case StartNode _ -> List.of(new x8664EnterOp());
            case AddNode addNode -> List.of(new x8664AddOp(extractOperands(addNode)));
            case SubNode subNode -> List.of(new x8664SubOp(extractOperands(subNode)));
            case MulNode mulNode -> List.of(new x8664IMulOp(extractOperands(mulNode)));
            case DivNode divNode -> List.of(new x8664DivPhantomOp(extractOperands(divNode)));
            case ModNode modNode -> List.of(new x8664ModPhantomOp(extractOperands(modNode)));
            case ConstIntNode constIntNode -> List.of(new x8664LoadConstPhantomOp(constIntNode));
            case ConstBoolNode constBoolNode -> List.of(new x8664LoadConstPhantomOp(constBoolNode));
            case ReturnNode returnNode ->
                    List.of(new x8664RetOp(NodeUtils.shortcutPredecessors(returnNode).get(ReturnNode.RESULT)));
            case JumpNode jumpNode -> List.of(new x8664JumpOp(label(jumpNode.target())));
            case IfNode ifNode -> {
                Map<Boolean, Block> targets = ifNode.targets();
                Block trueTarget = targets.get(true);
                Block falseTarget = targets.get(false);

                yield List.of(new x8664ConditionalJumpOp(ifNode.predecessor(IfNode.CONDITION), true, label(falseTarget)),
                        new x8664JumpOp(label(trueTarget)));
            }
            case Phi _, Block _, ProjNode _ -> List.of();
            case BinaryOperationNode _, UnaryOperationNode _ ->
                    throw new IllegalStateException("Operation not supported by x86 backend"); // TODO
            default -> throw new IllegalStateException(node + " not implemented by x86 backend"); //TODO
        };

        Set<Phi> phiSuccessors = schedule.programGraph().successors(node).stream()
                .filter(succ -> succ instanceof Phi)
                .map(succ -> (Phi) succ)
                .collect(Collectors.toSet());

        if (phiSuccessors.isEmpty()) {
            return base;
        }

        List<x8664Op> seq = new ArrayList<>(base);
        for (Phi phiSuccessor : phiSuccessors) {
            seq.add(new x8664PhiMoveOp(phiSuccessor, node));
        }
        return seq;
    }

    private Operands.Binary<Node> extractOperands(BinaryOperationNode node) {
        List<Node> realPreds = NodeUtils.shortcutPredecessors(node);
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
