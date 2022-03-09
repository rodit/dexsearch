package xyz.rodit.dexsearch.tree.bindings;

public interface Binding<TBind> {

    void fail(Reason reason, Object subject) throws BindingException;
    void succeed(Reason reason, Object subject);
    void attach(TBind bind);
}
