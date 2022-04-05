package xyz.rodit.dexsearch.tree.bindings;

public class Failure {

    private final Reason reason;
    private final Object subject;

    public Failure(Reason reason, Object subject) {
        this.reason = reason;
        this.subject = subject;
    }

    public Reason getReason() {
        return reason;
    }

    public Object getSubject() {
        return subject;
    }
}
