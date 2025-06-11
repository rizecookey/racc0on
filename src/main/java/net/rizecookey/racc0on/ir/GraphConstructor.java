package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.ReturnNode;
import net.rizecookey.racc0on.ir.node.StartNode;
import net.rizecookey.racc0on.ir.node.operation.binary.AddNode;
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
import net.rizecookey.racc0on.ir.node.operation.binary.NotEqNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ShiftLeftNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ShiftRightNode;
import net.rizecookey.racc0on.ir.node.operation.binary.SubNode;
import net.rizecookey.racc0on.ir.node.operation.unary.NotNode;
import net.rizecookey.racc0on.ir.optimize.Optimizer;
import net.rizecookey.racc0on.parser.symbol.Name;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphConstructor {

    private final Optimizer optimizer;
    private final IrGraph graph;
    private final Map<Name, Map<Block, Node>> currentDef = new HashMap<>();
    private final Map<Block, Map<Name, Phi>> incompletePhis = new HashMap<>();
    private final Map<Block, Node> currentSideEffect = new HashMap<>();
    private final Map<Block, Phi> incompleteSideEffectPhis = new HashMap<>();
    private final Map<Block, JumpNode> jumps = new HashMap<>();
    private final Set<Block> sealedBlocks = new HashSet<>();
    private Block currentBlock;

    public GraphConstructor(Optimizer optimizer, String name) {
        this.optimizer = optimizer;
        this.graph = new IrGraph(name);
        this.currentBlock = this.graph.startBlock();
        // the start block never gets any more predecessors
        sealBlock(this.currentBlock);
    }

    public Node newStart() {
        assert currentBlock() == this.graph.startBlock() : "start must be in start block";
        return new StartNode(currentBlock());
    }

    @FunctionalInterface
    public interface BinaryOpConstr {
        Node create(Block block, Node left, Node right);
    }

    @FunctionalInterface
    public interface BinarySideEffectOpConstr {
        Node create(Block block, Node left, Node right, Node sideEffect);
    }

    public Node newBinaryOp(BinaryOpConstr constr, Node left, Node right) {
        return this.optimizer.transform(constr.create(currentBlock(), left, right));
    }

    public Node newBinarySideEffectOp(BinarySideEffectOpConstr constr, Node left, Node right) {
        return this.optimizer.transform(constr.create(currentBlock(), left, right, readCurrentSideEffect()));
    }

    public Node newSub(Node left, Node right) {
        return newBinaryOp(SubNode::new, left, right);
    }

    public Node newAdd(Node left, Node right) {
        return newBinaryOp(AddNode::new, left, right);
    }

    public Node newMul(Node left, Node right) {
        return newBinaryOp(MulNode::new, left, right);
    }

    public Node newDiv(Node left, Node right) {
        return newBinarySideEffectOp(DivNode::new, left, right);
    }

    public Node newMod(Node left, Node right) {
        return newBinarySideEffectOp(ModNode::new, left, right);
    }

    public Node newBitwiseAnd(Node left, Node right) {
        return newBinaryOp(BitwiseAndNode::new, left, right);
    }

    public Node newBitwiseXor(Node left, Node right) {
        return newBinaryOp(BitwiseXorNode::new, left, right);
    }

    public Node newBitwiseOr(Node left, Node right) {
        return newBinaryOp(BitwiseOrNode::new, left, right);
    }

    public Node newShiftLeft(Node left, Node right) {
        return newBinaryOp(ShiftLeftNode::new, left, right);
    }

    public Node newShiftRight(Node left, Node right) {
        return newBinaryOp(ShiftRightNode::new, left, right);
    }

    public Node newLessThan(Node left, Node right) {
        return newBinaryOp(LessNode::new, left, right);
    }

    public Node newLessOrEqual(Node left, Node right) {
        return newBinaryOp(LessOrEqNode::new, left, right);
    }

    public Node newGreaterThan(Node left, Node right) {
        return newBinaryOp(GreaterNode::new, left, right);
    }

    public Node newGreaterOrEqual(Node left, Node right) {
        return newBinaryOp(GreaterOrEqNode::new, left, right);
    }

    public Node newEqual(Node left, Node right) {
        return newBinaryOp(EqNode::new, left, right);
    }

    public Node newNotEqual(Node left, Node right) {
        return newBinaryOp(NotEqNode::new, left, right);
    }

    public Node newNot(Node in) {
        return optimizer.transform(new NotNode(currentBlock(), in));
    }

    public Node newReturn(Node result) {
        return new ReturnNode(currentBlock(), readCurrentSideEffect(), result);
    }

    public Node newConstInt(int value) {
        // always move const into start block, this allows better deduplication
        // and resultingly in better value numbering
        return this.optimizer.transform(new ConstIntNode(this.graph.startBlock(), value));
    }

    public Node newConstBool(boolean value) {
        return this.optimizer.transform(new ConstBoolNode(this.graph.startBlock(), value));
    }

    public Node newSideEffectProj(Node node) {
        return new ProjNode(currentBlock(), node, ProjNode.SimpleProjectionInfo.SIDE_EFFECT);
    }

    public Node newResultProj(Node node) {
        return new ProjNode(currentBlock(), node, ProjNode.SimpleProjectionInfo.RESULT);
    }

    public Node newIfTrueProj(Node node) {
        ProjNode proj = new ProjNode(currentBlock(), node, ProjNode.SimpleProjectionInfo.IF_TRUE);
        currentBlock().addExit(proj);
        return proj;
    }

    public Node newIfFalseProj(Node node) {
        ProjNode proj = new ProjNode(currentBlock(), node, ProjNode.SimpleProjectionInfo.IF_FALSE);
        currentBlock().addExit(proj);
        return proj;
    }

    public Node newIf(Node condition) {
        return this.optimizer.transform(new IfNode(currentBlock(), condition));
    }

    public Node newJump() {
        JumpNode jumpNode = new JumpNode(currentBlock());
        jumps.put(currentBlock(), jumpNode);
        currentBlock().addExit(jumpNode);
        return jumpNode;
    }

    public boolean hasUnconditionalJump() {
        return jumps.containsKey(currentBlock());
    }

    public Block newBlock(Node... predecessors) {
        return newBlock(Arrays.asList(predecessors));
    }

    public Block newBlock(Collection<Node> predecessors) {
        Block block = new Block(this.graph);
        this.currentBlock = block;
        for (var predecessor : predecessors) {
            block.addPredecessor(predecessor);
        }
        return block;
    }

    public Block currentBlock() {
        return this.currentBlock;
    }

    void setCurrentBlock(Block currentBlock) {
        this.currentBlock = currentBlock;
    }

    public Phi newPhi() {
        // don't transform phi directly, it is not ready yet
        return new Phi(currentBlock());
    }

    public IrGraph graph() {
        return this.graph;
    }

    void writeVariable(Name variable, Block block, Node value) {
        this.currentDef.computeIfAbsent(variable, _ -> new HashMap<>()).put(block, value);
    }

    Node readVariable(Name variable, Block block) {
        Node node = this.currentDef.getOrDefault(variable, Map.of()).get(block);
        if (node != null) {
            return node;
        }
        return readVariableRecursive(variable, block);
    }


    private Node readVariableRecursive(Name variable, Block block) {
        Node val;
        if (!this.sealedBlocks.contains(block)) {
            val = new Phi(block);
            this.incompletePhis.computeIfAbsent(block, _ -> new HashMap<>()).put(variable, (Phi) val);
        } else if (block.predecessors().size() == 1) {
            val = readVariable(variable, block.predecessors().getFirst().block());
        } else {
            val = new Phi(block);
            writeVariable(variable, block, val);
            val = addPhiOperands(variable, (Phi) val);
        }
        writeVariable(variable, block, val);
        return val;
    }

    Node addPhiOperands(Name variable, Phi phi) {
        for (Node pred : phi.block().predecessors()) {
            phi.appendOperand(readVariable(variable, pred.block()));
        }
        return tryRemoveTrivialPhi(phi);
    }

    Node tryRemoveTrivialPhi(Phi phi) {
        Node same = null;
        for (var op : phi.predecessors()) {
            if (op == same || op == phi) {
                continue;
            }
            if (same != null) {
                return phi;
            }
            same = op;
        }

        List<Node> users = List.of();
        if (same == null) {
            same = phi;
        } else {
            users = graph.successors(phi).stream().filter(user -> user != phi).toList();
            phi.replaceBy(same);
        }

        for (var use : users) {
            if (use instanceof Phi other) {
                tryRemoveTrivialPhi(other);
            }
        }

        return same;
    }

    void sealBlock(Block block) {
        for (Map.Entry<Name, Phi> entry : this.incompletePhis.getOrDefault(block, Map.of()).entrySet()) {
            addPhiOperands(entry.getKey(), entry.getValue());
        }
        Phi sideEffectPhi = this.incompleteSideEffectPhis.get(block);
        if (sideEffectPhi != null) {
            addPhiOperands(sideEffectPhi);
        }
        this.sealedBlocks.add(block);
    }

    public void writeCurrentSideEffect(Node node) {
        writeSideEffect(currentBlock(), node);
    }

    private void writeSideEffect(Block block, Node node) {
        this.currentSideEffect.put(block, node);
    }

    public Node readCurrentSideEffect() {
        return readSideEffect(currentBlock());
    }

    private Node readSideEffect(Block block) {
        Node node = this.currentSideEffect.get(block);
        if (node != null) {
            return node;
        }
        return readSideEffectRecursive(block);
    }

    private Node readSideEffectRecursive(Block block) {
        Node val;
        if (!this.sealedBlocks.contains(block)) {
            val = new Phi(block);
            Phi old = this.incompleteSideEffectPhis.put(block, (Phi) val);
            assert old == null : "double readSideEffectRecursive for " + block;
        } else if (block.predecessors().size() == 1) {
            val = readSideEffect(block.predecessors().getFirst().block());
        } else {
            val = new Phi(block);
            writeSideEffect(block, val);
            val = addPhiOperands((Phi) val);
        }
        writeSideEffect(block, val);
        return val;
    }

    Node addPhiOperands(Phi phi) {
        for (Node pred : phi.block().predecessors()) {
            phi.appendOperand(readSideEffect(pred.block()));
        }
        return tryRemoveTrivialPhi(phi);
    }

}
