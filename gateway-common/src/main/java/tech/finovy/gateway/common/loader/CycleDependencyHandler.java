package tech.finovy.gateway.common.loader;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class CycleDependencyHandler {

    private static final ThreadLocal<Set<Object>> OBJECT_SET_LOCAL = new ThreadLocal<>();

    public static boolean isStarting() {
        return OBJECT_SET_LOCAL.get() != null;
    }

    public static void start() {
        OBJECT_SET_LOCAL.set(new HashSet<>(8));
    }

    public static void end() {
        OBJECT_SET_LOCAL.remove();
    }

    public static void addObject(Object obj) {
        if (obj == null) {
            return;
        }

        // get object set
        Set<Object> objectSet = OBJECT_SET_LOCAL.get();

        // add to object set
        objectSet.add(getUniqueSubstituteObject(obj));
    }

    public static boolean containsObject(Object obj) {
        if (obj == null) {
            return false;
        }

        // get object set
        Set<Object> objectSet = OBJECT_SET_LOCAL.get();
        if (objectSet.isEmpty()) {
            return false;
        }

        return objectSet.contains(getUniqueSubstituteObject(obj));
    }

    public static <O> String wrap(O obj, Function<O, String> function) {
        boolean isStarting = isStarting();
        try {
            if (!isStarting) {
                start();
            } else {
                if (containsObject(obj)) {
                    return toRefString(obj);
                }
            }

            // add object
            addObject(obj);

            // do function
            return function.apply(obj);
        } finally {
            if (!isStarting) {
                end();
            }
        }
    }

    public static String toRefString(Object obj) {
        return "(ref " + obj.getClass().getSimpleName() + ")";
    }

    /**
     * get Unique Substitute Object.
     * Avoid `obj.hashCode()` throwing `StackOverflowError` during cycle dependency.
     *
     * @param obj the object
     * @return the substitute object
     */
    private static Object getUniqueSubstituteObject(Object obj) {
        // TODO: HELP-WANTED: Optimize this method to ensure uniqueness
        return System.identityHashCode(obj);
    }
}
