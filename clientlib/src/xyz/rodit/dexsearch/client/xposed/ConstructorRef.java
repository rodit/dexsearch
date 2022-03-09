package xyz.rodit.dexsearch.client.xposed;

import de.robv.android.xposed.XC_MethodHook;

import java.util.Collection;

public class ConstructorRef extends HookableBase {

    private final String className;

    public ConstructorRef(String className) {
        this.className = className;
    }

    @Override
    protected Collection<XC_MethodHook.Unhook> performHook(XC_MethodHook hook) {
        return MappedObject.hookConstructors(className, hook);
    }
}
