package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.ir.IrGraph;

import java.util.ArrayList;
import java.util.List;

public final class Block extends Node {
    private final List<Node> exits;

    public Block(IrGraph graph) {
        super(graph);
        exits = new ArrayList<>();
    }

    public List<? extends Node> getEntrances() {
        return predecessors();
    }

    public List<? extends Node> getExits() {
        return List.copyOf(exits);
    }

    public void addExit(Node exit) {
        exits.add(exit);
    }
}
