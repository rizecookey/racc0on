package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.ArrayList;
import java.util.List;

public class SsaBlockScheduler extends IrGraphTraverser {
    private final List<Block> schedule = new ArrayList<>();

    @Override
    protected boolean visit(Node node) {
        if (addSeen(node)) {
            node.predecessors().stream().map(Node::block).forEach(this::push);
            return true;
        }

        return false;
    }

    @Override
    public void consume(Node node) {
        schedule.add(node.block());
    }

    public List<Block> getSchedule() {
        return List.copyOf(schedule);
    }
}
