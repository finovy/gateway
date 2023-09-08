package tech.finovy.gateway.common.util;


import tech.finovy.gateway.common.loader.CycleDependencyHandler;

import java.lang.reflect.Array;

public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * arrayObj cast to Object[]
     *
     * @param arrayObj the array obj
     * @return array
     */
    public static Object[] toArray(Object arrayObj) {
        if (arrayObj == null) {
            return null;
        }

        if (!arrayObj.getClass().isArray()) {
            throw new ClassCastException("'arrayObj' is not an array, can't cast to Object[]");
        }

        int length = Array.getLength(arrayObj);
        Object[] array = new Object[length];
        if (length > 0) {
            for (int i = 0; i < length; ++i) {
                array[i] = Array.get(arrayObj, i);
            }
        }
        return array;
    }

    /**
     * Array To String.
     *
     * @param array the array
     * @return str the string
     */
    public static String toString(final Object[] array) {
        if (array == null) {
            return "null";
        }
        if (array.length == 0) {
            return "[]";
        }

        return CycleDependencyHandler.wrap(array, o -> {
            StringBuilder sb = new StringBuilder(32);
            sb.append("[");
            for (Object obj : array) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                if (obj == array) {
                    sb.append("(this ").append(obj.getClass().getSimpleName()).append(")");
                } else {
                    sb.append(StringUtils.toString(obj));
                }
            }
            sb.append("]");
            return sb.toString();
        });
    }

    /**
     * Array To String.
     *
     * @param arrayObj the array obj
     * @return str the string
     */
    public static String toString(final Object arrayObj) {
        if (arrayObj == null) {
            return "null";
        }
        if (!arrayObj.getClass().isArray()) {
            return StringUtils.toString(arrayObj);
        }

        if (Array.getLength(arrayObj) == 0) {
            return "[]";
        }

        if (arrayObj.getClass().getComponentType().isPrimitive()) {
            return toString(toArray(arrayObj));
        } else {
            return toString((Object[]) arrayObj);
        }
    }
}
