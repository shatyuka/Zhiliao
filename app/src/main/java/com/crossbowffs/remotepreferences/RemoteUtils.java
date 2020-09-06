package com.crossbowffs.remotepreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Common utilities used to serialize and deserialize
 * preferences between the preference provider and caller.
 */
/* package */ class RemoteUtils {
    /**
     * Casts the parameter to a string set. Useful to avoid the unchecked
     * warning that would normally come with the cast. The value must
     * already be a string set; this does not deserialize it.
     *
     * @param value The value, as type {@link Object}.
     * @return The value, as type {@link Set<String>}.
     */
    @SuppressWarnings("unchecked")
    public static Set<String> castStringSet(Object value) {
        return (Set<String>)value;
    }

    /**
     * Returns the {@code TYPE_*} constant corresponding to the given
     * object's type.
     *
     * @param value The original object.
     * @return One of the {@link RemoteContract}{@code .TYPE_*} constants.
     */
    public static int getPreferenceType(Object value) {
        if (value == null) return RemoteContract.TYPE_NULL;
        if (value instanceof String) return RemoteContract.TYPE_STRING;
        if (value instanceof Set<?>) return RemoteContract.TYPE_STRING_SET;
        if (value instanceof Integer) return RemoteContract.TYPE_INT;
        if (value instanceof Long) return RemoteContract.TYPE_LONG;
        if (value instanceof Float) return RemoteContract.TYPE_FLOAT;
        if (value instanceof Boolean) return RemoteContract.TYPE_BOOLEAN;
        throw new AssertionError("Unknown preference type: " + value.getClass());
    }

    /**
     * Serializes the specified object to a format that is safe to use
     * with {@link android.content.ContentValues}. To recover the original
     * object, use {@link #deserializeInput(Object, int)}.
     *
     * @param value The object to serialize.
     * @return The serialized object.
     */
    public static Object serializeOutput(Object value) {
        if (value instanceof Boolean) {
            return serializeBoolean((Boolean)value);
        } else if (value instanceof Set<?>) {
            return serializeStringSet(castStringSet(value));
        } else {
            return value;
        }
    }

    /**
     * Deserializes an object that was serialized using
     * {@link #serializeOutput(Object)}. If the expected type does
     * not match the actual type of the object, a {@link ClassCastException}
     * will be thrown.
     *
     * @param value The object to deserialize.
     * @param expectedType The expected type of the deserialized object.
     * @return The deserialized object.
     */
    @SuppressWarnings("RedundantCast")
    public static Object deserializeInput(Object value, int expectedType) {
        if (expectedType == RemoteContract.TYPE_NULL) {
            if (value != null) {
                throw new IllegalArgumentException("Expected null, got non-null value");
            } else {
                return null;
            }
        }
        try {
            switch (expectedType) {
            case RemoteContract.TYPE_STRING:
                return (String)value;
            case RemoteContract.TYPE_STRING_SET:
                return deserializeStringSet((String)value);
            case RemoteContract.TYPE_INT:
                return (Integer)value;
            case RemoteContract.TYPE_LONG:
                return (Long)value;
            case RemoteContract.TYPE_FLOAT:
                return (Float)value;
            case RemoteContract.TYPE_BOOLEAN:
                return deserializeBoolean(value);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected type " + expectedType + ", got " + value.getClass(), e);
        }
        throw new IllegalArgumentException("Unknown type: " + expectedType);
    }

    /**
     * Serializes a {@link Boolean} to a format that is safe to use
     * with {@link android.content.ContentValues}.
     *
     * @param value The {@link Boolean} to serialize.
     * @return 1 if {@code value} is {@code true}, 0 if {@code value} is {@code false}.
     */
    private static Integer serializeBoolean(Boolean value) {
        if (value == null) {
            return null;
        } else {
            return value ? 1 : 0;
        }
    }

    /**
     * Deserializes a {@link Boolean} that was serialized using
     * {@link #serializeBoolean(Boolean)}.
     *
     * @param value The {@link Boolean} to deserialize.
     * @return {@code true} if {@code value} is 1, {@code false} if {@code value} is 0.
     */
    private static Boolean deserializeBoolean(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            return (Integer)value != 0;
        }
    }

    /**
     * Serializes a {@link Set<String>} to a format that is safe to use
     * with {@link android.content.ContentValues}.
     *
     * @param stringSet The {@link Set<String>} to serialize.
     * @return The serialized string set.
     */
    public static String serializeStringSet(Set<String> stringSet) {
        if (stringSet == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : stringSet) {
            sb.append(s.replace("\\", "\\\\").replace(";", "\\;"));
            sb.append(';');
        }
        return sb.toString();
    }

    /**
     * Deserializes a {@link Set<String>} that was serialized using
     * {@link #serializeStringSet(Set)}.
     *
     * @param serializedString The {@link Set<String>} to deserialize.
     * @return The deserialized string set.
     */
    public static Set<String> deserializeStringSet(String serializedString) {
        if (serializedString == null) {
            return null;
        }
        HashSet<String> stringSet = new HashSet<String>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < serializedString.length(); ++i) {
            char c = serializedString.charAt(i);
            if (c == '\\') {
                char next = serializedString.charAt(++i);
                sb.append(next);
            } else if (c == ';') {
                stringSet.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        // Our implementation always ensures a trailing semicolon, but
        // as the saying goes - be conservative in what you do, be
        // liberal in what you accept.
        if (sb.length() != 0) {
            stringSet.add(sb.toString());
        }

        return stringSet;
    }
}
