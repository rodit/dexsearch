package xyz.rodit.dexsearch.tree.bindings.options;

import java.util.HashMap;
import java.util.Map;

public class EnumParser<T> implements ValueParser<T> {

    private final Map<String, T> values = new HashMap<>();

    public EnumParser(Class<T> cls) {
        for (T value : cls.getEnumConstants()) {
            values.put(value.toString().toLowerCase(), value);
        }
    }

    @Override
    public T parse(String value) {
        return values.get(value.toLowerCase());
    }
}
