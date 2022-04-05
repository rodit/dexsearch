package xyz.rodit.dexsearch.tree;

import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.nodes.NodeBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MarkerUtils {

    public static <T extends NodeBase<?, ?, ?>> List<List<T>> partition(Collection<T> nodes) {
        List<List<T>> partitions = new ArrayList<>();
        List<T> current = new ArrayList<>();
        for (T node : nodes) {
            current.add(node);
            if (node.hasAttribute(Attribute.MARKER)) {
                partitions.add(current);
                current.clear();
            }
        }

        if (!current.isEmpty()) {
            partitions.add(current);
        }

        return partitions;
    }
}
