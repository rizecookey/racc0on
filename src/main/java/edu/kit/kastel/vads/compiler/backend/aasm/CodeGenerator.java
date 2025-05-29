package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BitwiseAndNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BitwiseOrNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BitwiseXorNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.EqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.GreaterNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.GreaterOrEqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.LessNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.LessOrEqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.NotEqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ShiftLeftNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ShiftRightNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.SubNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.unary.BitwiseNotNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.unary.NotNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.unary.UnaryOperationNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class CodeGenerator {

    public String generateCode(List<IrGraph> program) {
        StringBuilder builder = new StringBuilder();
        for (IrGraph graph : program) {
            RegisterAllocator allocator = new AasmRegisterAllocator();
            Map<Node, Register> registers = allocator.allocateRegisters(graph);
            builder.append("function ")
                .append(graph.name())
                .append(" {\n");
            generateForGraph(graph, builder, registers);
            builder.append("}");
        }
        return builder.toString();
    }

    private void generateForGraph(IrGraph graph, StringBuilder builder, Map<Node, Register> registers) {
        Set<Node> visited = new HashSet<>();
        scan(graph.endBlock(), visited, builder, registers);
    }

    private void scan(Node node, Set<Node> visited, StringBuilder builder, Map<Node, Register> registers) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, builder, registers);
            }
        }

        switch (node) {
            case AddNode add -> binary(builder, registers, add, "add");
            case SubNode sub -> binary(builder, registers, sub, "sub");
            case MulNode mul -> binary(builder, registers, mul, "mul");
            case DivNode div -> binary(builder, registers, div, "div");
            case ModNode mod -> binary(builder, registers, mod, "mod");
            case BitwiseAndNode bitwiseAnd -> binary(builder, registers, bitwiseAnd, "&");
            case BitwiseOrNode bitwiseOr -> binary(builder, registers, bitwiseOr, "|");
            case BitwiseXorNode bitwiseXor -> binary(builder, registers, bitwiseXor, "^");
            case EqNode eq -> binary(builder, registers, eq, "==");
            case GreaterNode greater -> binary(builder, registers, greater, ">");
            case GreaterOrEqNode greaterOrEq -> binary(builder, registers, greaterOrEq, ">=");
            case LessNode less -> binary(builder, registers, less, "<");
            case LessOrEqNode lessOrEq -> binary(builder, registers, lessOrEq, "<=");
            case NotEqNode notEq -> binary(builder, registers, notEq, "!=");
            case ShiftLeftNode shiftLeft -> binary(builder, registers, shiftLeft, "<<");
            case ShiftRightNode shiftRight -> binary(builder, registers, shiftRight, ">>");
            case NotNode not -> unary(builder, registers, not, "!");
            case BitwiseNotNode bitwiseNot -> unary(builder, registers, bitwiseNot, "~");
            case ReturnNode r -> builder.repeat(" ", 2).append("ret ")
                .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)));
            case ConstIntNode c -> builder.repeat(" ", 2)
                .append(registers.get(c))
                .append(" = const ")
                .append(c.value());
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
        builder.append("\n");
    }

    private static void unary(
            StringBuilder builder,
            Map<Node, Register> registers,
            UnaryOperationNode node,
            String opcode
    ) {
        builder.repeat(" ", 2).append(registers.get(node))
                .append(" = ")
                .append(opcode)
                .append(registers.get(predecessorSkipProj(node, UnaryOperationNode.IN)));
    }

    private static void binary(
        StringBuilder builder,
        Map<Node, Register> registers,
        BinaryOperationNode node,
        String opcode
    ) {
        builder.repeat(" ", 2).append(registers.get(node))
            .append(" = ")
            .append(opcode)
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)));
    }
}
