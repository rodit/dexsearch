package xyz.rodit.dexsearch.tree.bindings.options;

import java.util.HashMap;
import java.util.Map;

public class Options {

    private final Map<Option, Object> values = new HashMap<>();

    public <T> T get(Option option) {
        return (T) values.get(option);
    }

    public void set(Option option, Object value) {
        values.put(option, value);
    }
}
