package xyz.rodit.dexsearch.tree;

import org.jf.dexlib2.AccessFlags;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccessUtils {

    private static final Map<String, Integer> modifierMap = new HashMap<>();

    static {
        modifierMap.put("class", 0);
        for (AccessFlags flag : AccessFlags.class.getEnumConstants()) {
            modifierMap.put(flag.name().toLowerCase(), flag.getValue());
        }
    }

    public static boolean isValidModifier(String modifier) {
        return modifierMap.containsKey(modifier.toLowerCase());
    }

    public static int getModifierValue(String modifier) {
        return modifierMap.get(modifier.toLowerCase());
    }

    public static int getModifiers(Collection<String> modifiers) {
        return modifiers.stream().map(AccessUtils::getModifierValue).reduce((m0, m1) -> m0 | m1).orElse(0);
    }

    public static boolean hasModifiers(int dexModifiers, int nodeModifiers) {
        return (dexModifiers & nodeModifiers) == nodeModifiers;
    }
}
