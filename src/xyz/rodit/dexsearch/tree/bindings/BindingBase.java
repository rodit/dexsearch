package xyz.rodit.dexsearch.tree.bindings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BindingBase<TBind> implements Binding<TBind> {

    private final List<Failure> failReasons = new ArrayList<>();
    private TBind bound;
    private int score;

    public <T> void test(Reason reason, T subject, Predicate<T> predicate) throws BindingException {
        test(reason, subject, predicate.test(subject));
    }

    public void test(Reason reason, Object subject, boolean test) throws BindingException {
        if (test) {
            succeed(reason, subject);
        } else {
            fail(reason, subject);
        }
    }

    @Override
    public void fail(Reason reason, Object subject) throws BindingException {
        failReasons.add(new Failure(reason, subject));
    }

    @Override
    public void succeed(Reason reason, Object subject) {
        this.score += reason.score();
    }

    @Override
    public void attach(TBind bind) {
        bound = bind;
    }

    public void reset() {
        failReasons.clear();
        bound = null;
        score = 0;
    }

    public List<Failure> getFailReasons() {
        return failReasons;
    }

    public boolean isExact() {
        return failReasons.size() == 0;
    }

    public TBind get() {
        return bound;
    }

    public int getScore() {
        return score;
    }
}
