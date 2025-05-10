package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class x8664CodeGenerator {
    public static final String ENTRYPOINT_NAME = "_entry";

    private static final String BOILERPLATE_ENTRY = """
            _entry:
            call main
            mov rdi, rax
            mov rax, 0x3C
            syscall
            """;

    private final StringBuilder builder = new StringBuilder();

    public String generateCode(List<IrGraph> program) {
        appendLine(".intel_syntax noprefix"); // enable intel assembly syntax
        declareGlobal(ENTRYPOINT_NAME);
        for (IrGraph graph : program) {
            declareGlobal(graph.name());
        }

        initializeTextSegment();

        for (IrGraph graph : program) {
            List<Node> sequentialProcedure = NodeUtils.transformToSequential(graph);
            x8664StorageAllocator allocator = new x8664StorageAllocator();
            x8664StorageAllocator.Allocation allocation = allocator.allocate(sequentialProcedure);
            generateProcedure(graph, allocation);
        }

        return builder.toString();
    }

    private x8664CodeGenerator append(String text) {
        builder.append(text);
        return this;
    }

    private void appendLine(String text) {
        append(text);
        newline();
    }

    private void initializeTextSegment() {
        newline();
        appendLine(".text");
        appendLine(BOILERPLATE_ENTRY);
    }

    private void generateProcedure(IrGraph program, x8664StorageAllocator.Allocation allocation) {
        append(program.name()).appendLine(":");

        InstructionSelector selector = new InstructionSelector(allocation);
        selector.prepareStack();
        List<Node> sequential = NodeUtils.transformToSequential(program);
        for (Node node : sequential) {
            selector.selectInstruction(node);
        }
    }

    private void newline() {
        append("\n");
    }

    private void declareGlobal(String symbolName) {
        append(".globl ").appendLine(symbolName);
    }

    private class InstructionSelector {
        private final Map<Node, x8664StorageLocation> locations;
        private final int stackSize;
        private InstructionSelector(x8664StorageAllocator.Allocation allocation) {
            this.locations = Map.copyOf(allocation.allocations());
            this.stackSize = allocation.stackSize();
        }

        public void prepareStack() {
            if (stackSize > 0) {
                append("sub rsp, ").appendLine(String.valueOf(stackSize));
            }
        }

        public void tearDownStack() {
            if (stackSize > 0) {
                append("add rsp, ").appendLine(String.valueOf(stackSize));
            }
        }

        public void selectInstruction(Node node) {
            switch (node) {
                case AddNode addNode -> twoAddressArithmetic("add", addNode);
                case SubNode subNode -> twoAddressArithmetic("sub", subNode);
                case MulNode mulNode -> mul(mulNode);
                case DivNode divNode -> throw new UnsupportedOperationException("Div not supported");
                case ModNode modNode -> throw new UnsupportedOperationException("Mod not supported");
                case ConstIntNode constIntNode -> loadConstant(constIntNode);
                case ReturnNode returnNode -> returnStatement(returnNode);
                case Phi _ -> throw new IllegalStateException("Phi instruction not supported");
                case Block _, ProjNode _, StartNode _ -> {}
            }
        }

        private void loadConstant(ConstIntNode node) {
            var out = locations.get(node);
            append("mov ").append(out.getId().dwordName()).append(", ").appendLine(String.valueOf(node.value()));
        }

        private void twoAddressArithmetic(String name, BinaryOperationNode node) {
            x8664StorageLocation out = locations.get(node);
            x8664StorageLocation inLeft = locations.get(node.predecessor(BinaryOperationNode.LEFT));
            x8664StorageLocation inRight = locations.get(node.predecessor(BinaryOperationNode.RIGHT));
            twoAddressArithmetic(name, out, inLeft, inRight);
        }

        private void twoAddressArithmetic(String name, x8664StorageLocation out, x8664StorageLocation inLeft, x8664StorageLocation inRight) {
            if (out instanceof x8664StackLocation && (inLeft instanceof x8664StackLocation || inRight instanceof x8664StackLocation)) {
                move(x8664Register.RBP, inLeft);
                writeInstruction(name, x8664Register.RBP, inRight);
                move(out, x8664Register.RBP);
            } else {
                if (out != inLeft) {
                    if (out == inRight) {
                        move(x8664Register.RBP, inRight);
                        inRight = x8664Register.RBP;
                    }
                    move(out, inLeft);
                }

                writeInstruction(name, out, inRight);
            }
        }

        private void move(x8664StorageLocation to, x8664StorageLocation from) {
            if (to instanceof x8664StackLocation && from instanceof x8664StackLocation) {
                move(x8664Register.RBP, from);
                move(to, x8664Register.RBP);
                return;
            }

            writeInstruction("mov", to, from);
        }

        private void mul(MulNode node) {
            x8664StorageLocation out = locations.get(node);
            x8664StorageLocation target = out;
            x8664StorageLocation inLeft = locations.get(node.predecessor(BinaryOperationNode.LEFT));
            x8664StorageLocation inRight = locations.get(node.predecessor(BinaryOperationNode.RIGHT));

            if (out instanceof x8664StackLocation) {
                target = x8664Register.RBP;
            }
            twoAddressArithmetic("imul", target, inLeft, inRight);

            if (target != out) {
                move(out, target);
            }
        }

        private void returnStatement(ReturnNode node) {
            x8664StorageLocation in = locations.get(node.predecessor(ReturnNode.RESULT));
            if (in != x8664Register.RAX) {
                move(x8664Register.RAX, in);
            }
            tearDownStack();
            writeInstruction("ret");
        }

        private void writeInstruction(String name, x8664StorageLocation... locations) {
            append(name);

            if (locations.length == 0) {
                newline();
                return;
            }

            List<String> names = Arrays.stream(locations).map(loc -> loc.getId().dwordName()).toList();
            append(" ").appendLine(String.join(", ", names));
        }
    }
}
