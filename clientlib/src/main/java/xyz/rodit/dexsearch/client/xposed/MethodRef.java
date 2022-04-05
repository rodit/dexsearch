package xyz.rodit.dexsearch.client.xposed;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.dexsearch.client.Mappings;

import java.util.Collection;

public class MethodRef extends HookableBase {

    private final String className;
    private final String name;

    public MethodRef(String className, String name) {
        this.className = className;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDexName() {
        return Mappings.get(className).getDexMethod(name);
    }

    @Override
    protected Collection<XC_MethodHook.Unhook> performHook(XC_MethodHook hook) {
        return MappedObject.hook(className, name, hook);
    }
}
