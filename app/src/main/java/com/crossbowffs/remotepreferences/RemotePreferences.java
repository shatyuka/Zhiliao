package com.crossbowffs.remotepreferences;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * <p>
 * Provides a {@link SharedPreferences} compatible API to
 * {@link RemotePreferenceProvider}. See {@link RemotePreferenceProvider}
 * for more information.
 * </p>
 *
 * <p>
 * If you are reading preferences from the same context as the
 * provider, you should not use this class; just access the
 * {@link SharedPreferences} API as you would normally.
 * </p>
 */
public class RemotePreferences implements SharedPreferences {
    private final Context mContext;
    private final Handler mHandler;
    private final Uri mBaseUri;
    private final WeakHashMap<OnSharedPreferenceChangeListener, PreferenceContentObserver> mListeners;
    private final boolean mStrictMode;

    /**
     * Initializes a new remote preferences object, with strict
     * mode disabled.
     *
     * @param context Used to access the preference provider.
     * @param authority The authority of the preference provider.
     * @param prefFileName The name of the preference file to access.
     */
    public RemotePreferences(Context context, String authority, String prefFileName) {
        this(context, authority, prefFileName, false);
    }

    /**
     * Initializes a new remote preferences object. If {@code strictMode}
     * is {@code true} and the remote preference provider cannot be accessed,
     * read/write operations on this object will throw a
     * {@link RemotePreferenceAccessException}. Otherwise, default values
     * will be returned.
     *
     * @param context Used to access the preference provider.
     * @param authority The authority of the preference provider.
     * @param prefFileName The name of the preference file to access.
     * @param strictMode Whether strict mode is enabled.
     */
    public RemotePreferences(Context context, String authority, String prefFileName, boolean strictMode) {
        checkNotNull("authority", authority);
        checkNotNull("context", context);
        checkNotNull("prefFileName", prefFileName);
        mContext = context;
        mHandler = new Handler(context.getMainLooper());
        mBaseUri = Uri.parse("content://" + authority).buildUpon().appendPath(prefFileName).build();
        mListeners = new WeakHashMap<OnSharedPreferenceChangeListener, PreferenceContentObserver>();
        mStrictMode = strictMode;
    }

    @Override
    public Map<String, ?> getAll() {
        return queryAll();
    }

    @Override
    public String getString(String key, String defValue) {
        return (String)querySingle(key, defValue, RemoteContract.TYPE_STRING);
    }

    @Override
    @TargetApi(11)
    public Set<String> getStringSet(String key, Set<String> defValues) {
        if (Build.VERSION.SDK_INT < 11) {
            throw new UnsupportedOperationException("String sets only supported on API 11 and above");
        }
        return RemoteUtils.castStringSet(querySingle(key, defValues, RemoteContract.TYPE_STRING_SET));
    }

    @Override
    public int getInt(String key, int defValue) {
        return (Integer)querySingle(key, defValue, RemoteContract.TYPE_INT);
    }

    @Override
    public long getLong(String key, long defValue) {
        return (Long)querySingle(key, defValue, RemoteContract.TYPE_LONG);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return (Float)querySingle(key, defValue, RemoteContract.TYPE_FLOAT);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return (Boolean)querySingle(key, defValue, RemoteContract.TYPE_BOOLEAN);
    }

    @Override
    public boolean contains(String key) {
        return containsKey(key);
    }

    @Override
    public Editor edit() {
        return new RemotePreferencesEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        checkNotNull("listener", listener);
        if (mListeners.containsKey(listener)) return;
        PreferenceContentObserver observer = new PreferenceContentObserver(listener);
        mListeners.put(listener, observer);
        mContext.getContentResolver().registerContentObserver(mBaseUri, true, observer);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        checkNotNull("listener", listener);
        PreferenceContentObserver observer = mListeners.remove(listener);
        if (observer != null) {
            mContext.getContentResolver().unregisterContentObserver(observer);
        }
    }

    /**
     * If {@code object} is {@code null}, throws an exception.
     *
     * @param name The name of the object, for use in the exception message.
     * @param object The object to check.
     */
    private static void checkNotNull(String name, Object object) {
        if (object == null) {
            throw new IllegalArgumentException(name + " is null");
        }
    }

    /**
     * If {@code key} is {@code null} or {@code ""}, throws an exception.
     *
     * @param key The object to check.
     */
    private static void checkKeyNotEmpty(String key) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Key is null or empty");
        }
    }

    /**
     * If strict mode is enabled, wraps and throws the given exception.
     * Otherwise, does nothing.
     *
     * @param e The exception to wrap.
     */
    private void wrapException(Exception e) {
        if (mStrictMode) {
            throw new RemotePreferenceAccessException(e);
        }
    }

    /**
     * Queries the specified URI. If the query fails and strict mode is
     * enabled, an exception will be thrown; otherwise {@code null} will
     * be returned.
     *
     * @param uri The URI to query.
     * @param columns The columns to include in the returned cursor.
     * @return A cursor used to access the queried preference data.
     */
    private Cursor query(Uri uri, String[] columns) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, columns, null, null, null);
        } catch (Exception e) {
            wrapException(e);
        }
        if (cursor == null && mStrictMode) {
            throw new RemotePreferenceAccessException("query() failed or returned null cursor");
        }
        return cursor;
    }

    /**
     * Writes multiple preferences at once to the preference provider.
     * If the operation fails and strict mode is enabled, an exception
     * will be thrown; otherwise {@code false} will be returned.
     *
     * @param uri The URI to modify.
     * @param values The values to write.
     * @return Whether the operation succeeded.
     */
    private boolean bulkInsert(Uri uri, ContentValues[] values) {
        int count;
        try {
            count = mContext.getContentResolver().bulkInsert(uri, values);
        } catch (Exception e) {
            wrapException(e);
            return false;
        }
        if (count != values.length && mStrictMode) {
            throw new RemotePreferenceAccessException("bulkInsert() failed");
        }
        return count == values.length;
    }

    /**
     * Reads a single preference from the preference provider. This may
     * throw a {@link ClassCastException} even if strict mode is disabled
     * if the provider returns an incompatible type. If strict mode is
     * disabled and the preference cannot be read, the default value is returned.
     *
     * @param key The preference key to read.
     * @param defValue The default value, if there is no existing value.
     * @param expectedType The expected type of the value.
     * @return The value of the preference, or {@code defValue} if no value exists.
     */
    private Object querySingle(String key, Object defValue, int expectedType) {
        checkKeyNotEmpty(key);
        Uri uri = mBaseUri.buildUpon().appendPath(key).build();
        String[] columns = {RemoteContract.COLUMN_TYPE, RemoteContract.COLUMN_VALUE};
        Cursor cursor = query(uri, columns);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return defValue;
            }

            int typeCol = cursor.getColumnIndexOrThrow(RemoteContract.COLUMN_TYPE);
            int type = cursor.getInt(typeCol);
            if (type == RemoteContract.TYPE_NULL) {
                return defValue;
            } else if (type != expectedType) {
                throw new ClassCastException("Preference type mismatch");
            }

            int valueCol = cursor.getColumnIndexOrThrow(RemoteContract.COLUMN_VALUE);
            return getValue(cursor, typeCol, valueCol);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Reads all preferences from the preference provider. If strict
     * mode is disabled and the preferences cannot be read, an empty
     * map is returned.
     *
     * @return A map containing all preferences.
     */
    private Map<String, Object> queryAll() {
        Uri uri = mBaseUri.buildUpon().appendPath("").build();
        String[] columns = {RemoteContract.COLUMN_KEY, RemoteContract.COLUMN_TYPE, RemoteContract.COLUMN_VALUE};
        Cursor cursor = query(uri, columns);
        try {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (cursor == null) {
                return map;
            }

            int keyCol = cursor.getColumnIndexOrThrow(RemoteContract.COLUMN_KEY);
            int typeCol = cursor.getColumnIndexOrThrow(RemoteContract.COLUMN_TYPE);
            int valueCol = cursor.getColumnIndexOrThrow(RemoteContract.COLUMN_VALUE);
            while (cursor.moveToNext()) {
                String key = cursor.getString(keyCol);
                map.put(key, getValue(cursor, typeCol, valueCol));
            }
            return map;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Checks whether the preference exists. If strict mode is
     * disabled and the preferences cannot be read, {@code false}
     * is returned.
     *
     * @param key The key to check existence for.
     * @return Whether the preference exists.
     */
    private boolean containsKey(String key) {
        checkKeyNotEmpty(key);
        Uri uri = mBaseUri.buildUpon().appendPath(key).build();
        String[] columns = {RemoteContract.COLUMN_TYPE};
        Cursor cursor = query(uri, columns);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return false;
            }

            int typeCol = cursor.getColumnIndexOrThrow(RemoteContract.COLUMN_TYPE);
            return cursor.getInt(typeCol) != RemoteContract.TYPE_NULL;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Extracts a preference value from a cursor. Performs deserialization
     * of the value if necessary.
     *
     * @param cursor The cursor containing the preference value.
     * @param typeCol The index containing the {@link RemoteContract#COLUMN_TYPE} column.
     * @param valueCol The index containing the {@link RemoteContract#COLUMN_VALUE} column.
     * @return The value from the cursor.
     */
    private Object getValue(Cursor cursor, int typeCol, int valueCol) {
        int expectedType = cursor.getInt(typeCol);
        switch (expectedType) {
        case RemoteContract.TYPE_STRING:
            return cursor.getString(valueCol);
        case RemoteContract.TYPE_STRING_SET:
            return RemoteUtils.deserializeStringSet(cursor.getString(valueCol));
        case RemoteContract.TYPE_INT:
            return cursor.getInt(valueCol);
        case RemoteContract.TYPE_LONG:
            return cursor.getLong(valueCol);
        case RemoteContract.TYPE_FLOAT:
            return cursor.getFloat(valueCol);
        case RemoteContract.TYPE_BOOLEAN:
            return cursor.getInt(valueCol) != 0;
        default:
            throw new AssertionError("Invalid expected type: " + expectedType);
        }
    }

    /**
     * Implementation of the {@link SharedPreferences.Editor} interface
     * for use with RemotePreferences.
     */
    private class RemotePreferencesEditor implements Editor {
        private final ArrayList<ContentValues> mValues = new ArrayList<ContentValues>();

        /**
         * Creates a new {@link ContentValues} with the specified key and
         * type columns pre-filled. The {@link RemoteContract#COLUMN_VALUE}
         * field is NOT filled in.
         *
         * @param key The preference key.
         * @param type The preference type.
         * @return The pre-filled values.
         */
        private ContentValues createContentValues(String key, int type) {
            ContentValues values = new ContentValues(4);
            values.put(RemoteContract.COLUMN_KEY, key);
            values.put(RemoteContract.COLUMN_TYPE, type);
            return values;
        }

        /**
         * Creates an operation to add/set a new preference. Again, the
         * {@link RemoteContract#COLUMN_VALUE} field is NOT filled in.
         * This will also add the values to the operation queue.
         *
         * @param key The preference key to add.
         * @param type The preference type to add.
         * @return The pre-filled values.
         */
        private ContentValues createAddOp(String key, int type) {
            checkKeyNotEmpty(key);
            ContentValues values = createContentValues(key, type);
            mValues.add(values);
            return values;
        }

        /**
         * Creates an operation to delete a preference. All fields
         * are pre-filled. This will also add the values to the
         * operation queue.
         *
         * @param key The preference key to delete.
         * @return The pre-filled values.
         */
        private ContentValues createRemoveOp(String key) {
            // Note: Remove operations are inserted at the beginning
            // of the list (this preserves the SharedPreferences behavior
            // that all removes are performed before any adds)
            ContentValues values = createContentValues(key, RemoteContract.TYPE_NULL);
            values.putNull(RemoteContract.COLUMN_VALUE);
            mValues.add(0, values);
            return values;
        }

        @Override
        public Editor putString(String key, String value) {
            createAddOp(key, RemoteContract.TYPE_STRING).put(RemoteContract.COLUMN_VALUE, value);
            return this;
        }

        @Override
        @TargetApi(11)
        public Editor putStringSet(String key, Set<String> value) {
            if (Build.VERSION.SDK_INT < 11) {
                throw new UnsupportedOperationException("String sets only supported on API 11 and above");
            }
            String serializedSet = RemoteUtils.serializeStringSet(value);
            createAddOp(key, RemoteContract.TYPE_STRING_SET).put(RemoteContract.COLUMN_VALUE, serializedSet);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            createAddOp(key, RemoteContract.TYPE_INT).put(RemoteContract.COLUMN_VALUE, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            createAddOp(key, RemoteContract.TYPE_LONG).put(RemoteContract.COLUMN_VALUE, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            createAddOp(key, RemoteContract.TYPE_FLOAT).put(RemoteContract.COLUMN_VALUE, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            createAddOp(key, RemoteContract.TYPE_BOOLEAN).put(RemoteContract.COLUMN_VALUE, value ? 1 : 0);
            return this;
        }

        @Override
        public Editor remove(String key) {
            checkKeyNotEmpty(key);
            createRemoveOp(key);
            return this;
        }

        @Override
        public Editor clear() {
            createRemoveOp("");
            return this;
        }

        @Override
        public boolean commit() {
            ContentValues[] values = mValues.toArray(new ContentValues[mValues.size()]);
            Uri uri = mBaseUri.buildUpon().appendPath("").build();
            return bulkInsert(uri, values);
        }

        @Override
        public void apply() {
            commit();
        }
    }

    /**
     * {@link ContentObserver} subclass used to monitor preference changes
     * in the remote preference provider. When a change is detected, this will notify
     * the corresponding {@link SharedPreferences.OnSharedPreferenceChangeListener}.
     */
    private class PreferenceContentObserver extends ContentObserver {
        private final WeakReference<OnSharedPreferenceChangeListener> mListener;

        private PreferenceContentObserver(OnSharedPreferenceChangeListener listener) {
            super(mHandler);
            mListener = new WeakReference<OnSharedPreferenceChangeListener>(listener);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            String prefKey = uri.getLastPathSegment();

            // We use a weak reference to mimic the behavior of SharedPreferences.
            // The code which registered the listener is responsible for holding a
            // reference to it. If at any point we find that the listener has been
            // garbage collected, we unregister the observer.
            OnSharedPreferenceChangeListener listener = mListener.get();
            if (listener == null) {
                mContext.getContentResolver().unregisterContentObserver(this);
            } else {
                listener.onSharedPreferenceChanged(RemotePreferences.this, prefKey);
            }
        }
    }
}
