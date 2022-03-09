package xyz.rodit.dexsearch.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CollectionUtils {

    public static <T> List<T> toList(Iterable<? extends T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    public static <T> void separate(Iterable<T> iterable, Collection<T> match, Collection<T> other, Predicate<T> predicate) {
        for (T obj : iterable) {
            (predicate.test(obj) ? match : other).add(obj);
        }
    }

    public static <T> String join(String delimiter, Iterable<T> iterable) {
        StringBuilder sb = new StringBuilder();
        for (T obj : iterable) {
            sb.append(obj)
                    .append(delimiter);
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - delimiter.length());
        }

        return sb.toString();
    }

    public static <T> int findIndex(Iterable<T> iterable, Predicate<T> predicate) {
        int i = 0;
        for (T item : iterable) {
            if (predicate.test(item)) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public static <T0, T1> boolean matches(Collection<T0> c0, Collection<T1> c1, ElementMatcher<T0, T1> matcher) {
        if (c0.size() != c1.size()) {
            return false;
        }

        var c0It = c0.iterator();
        var c1It = c1.iterator();
        while (c0It.hasNext()) {
            var c0Elem = c0It.next();
            var c1Elem = c1It.next();

            if (!matcher.match(c0Elem, c1Elem)) {
                return false;
            }
        }

        return true;
    }

    public static <T0, T1> boolean matchesUnstrict(Collection<T0> c0, Collection<T1> c1, ElementMatcher<T0, T1> matcher) {
        return matchesUntil(c0, c1, matcher, o -> false, false);
    }

    public static <T0, T1> boolean matchesUntil(Collection<T0> c0, Collection<T1> c1, ElementMatcher<T0, T1> matcher, Predicate<T0> until, boolean requireLength) {
        var c0It = c0.iterator();
        var c1It = c1.iterator();

        while (c0It.hasNext()) {
            var c0Elem = c0It.next();
            if (until.test(c0Elem)) {
                return true;
            }

            if (!c1It.hasNext()) {
                return false;
            }

            var c1Elem = c1It.next();
            if (!matcher.match(c0Elem, c1Elem)) {
                return false;
            }
        }

        return !requireLength || !c1It.hasNext();
    }

    public interface ElementMatcher<T0, T1> {

        boolean match(T0 o0, T1 o1);
    }
}
