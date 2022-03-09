package xyz.rodit.dexsearch.client.xposed;

import de.robv.android.xposed.XC_MethodHook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class HookableBase {

    private final List<XC_MethodHook.Unhook> unhooks = new ArrayList<>();

    protected abstract Collection<XC_MethodHook.Unhook> performHook(XC_MethodHook hook);

    public void hook(XC_MethodHook hook) {
        unhooks.addAll(performHook(hook));
    }

    public void unhook() {
        for (XC_MethodHook.Unhook unhook : unhooks) {
            unhook.unhook();
        }

        unhooks.clear();
    }
}
