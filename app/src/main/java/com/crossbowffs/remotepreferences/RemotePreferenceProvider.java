package com.crossbowffs.remotepreferences;

import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Exposes {@link SharedPreferences} to other apps running on the device.
 * </p>
 *
 * <p>
 * You must extend this class and declare a 0-argument constructor which
 * calls the super constructor with the appropriate authority and
 * preference file name parameters. Remember to add your provider to
 * your {@code AndroidManifest.xml} file and set the {@code android:exported}
 * property to true.
 * </p>
 *
 * <p>
 * For granular access control, override {@link #checkAccess(String, String, boolean)}
 * and return {@code false} to deny the operation.
 * </p>
 *
 * <p>
 * To access the data from a remote process, use {@link RemotePreferences}
 * initialized with the same authority and the desired preference file name.
 * You may also manually query the provider; here are some example queries
 * and their equivalent {@link SharedPreferences} API calls:
 * </p>
 *
 * <pre>
 * query(uri = content://authority/foo/bar)
 * = getSharedPreferences("foo").get("bar")
 *
 * query(uri = content://authority/foo)
 * = getSharedPreferences("foo").getAll()
 *
 * insert(uri = content://authority/foo/bar, values = [{type = TYPE_STRING, value = "baz"}])
 * = getSharedPreferences("foo").edit().putString("bar", "baz").commit()
 *
 * insert(uri = content://authority/foo, values = [{key = "bar", type = TYPE_STRING, value = "baz"}])
 * = getSharedPreferences("foo").edit().putString("bar", "baz").commit()
 *
 * delete(uri = content://authority/foo/bar)
 * = getSharedPreferences("foo").edit().remove("bar").commit()
 *
 * delete(uri = content://authority/foo)
 * = getSharedPreferences("foo").edit().clear().commit()
 * </pre>
 *
 * <p>
 * Also note that if you are querying string sets, they will be returned
 * in a serialized form: {@code ["foo;bar", "baz"]} is converted to
 * {@code "foo\\;bar;baz;"} (note the trailing semicolon). Booleans are
 * converted into integers: 1 for true, 0 for false. This is only applicable
 * if you are using raw queries; all of these subtleties are transparently
 * handled by {@link RemotePreferences}.
 * </p>
 */
public abstract class RemotePreferenceProvider extends ContentProvider implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int PREFERENCES_ID = 1;
    private static final int PREFERENCE_ID = 2;

    private final Uri mBaseUri;
    private final RemotePreferenceFile[] mPrefFiles;
    private final Map<String, SharedPreferences> mPreferences;
    private final UriMatcher mUriMatcher;

    /**
     * Initializes the remote preference provider with the specified
     * authority and preference file names. The authority must match the
     * {@code android:authorities} property defined in your manifest
     * file. Only the specified preference files will be accessible
     * through the provider. This constructor assumes all preferences
     * are located in credential protected storage; if you are using
     * device protected storage, use
     * {@link #RemotePreferenceProvider(String, RemotePreferenceFile[])}.
     *
     * @param authority The authority of the provider.
     * @param prefFileNames The names of the preference files to expose.
     */
    public RemotePreferenceProvider(String authority, String[] prefFileNames) {
        this(authority, RemotePreferenceFile.fromFileNames(prefFileNames));
    }

    /**
     * Initializes the remote preference provider with the specified
     * authority and preference files. The authority must match the
     * {@code android:authorities} property defined in your manifest
     * file. Only the specified preference files will be accessible
     * through the provider.
     *
     * @param authority The authority of the provider.
     * @param prefFiles The preference files to expose.
     */
    public RemotePreferenceProvider(String authority, RemotePreferenceFile[] prefFiles) {
        mBaseUri = Uri.parse("content://" + authority);
        mPrefFiles = prefFiles;
        mPreferences = new HashMap<String, SharedPreferences>(prefFiles.length);
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(authority, "*/", PREFERENCES_ID);
        mUriMatcher.addURI(authority, "*/*", PREFERENCE_ID);
    }

    /**
     * Checks whether the specified preference is accessible by callers.
     * The default implementation returns {@code true} for all accesses.
     * You may override this method to control which preferences can be
     * read or written. Note that {@code prefKey} will be {@code ""} when
     * accessing an entire file, so a whitelist is strongly recommended
     * over a blacklist (your default case should be {@code return false},
     * not {@code return true}).
     *
     * @param prefFileName The name of the preference file.
     * @param prefKey The preference key. This is an empty string when handling the
     *                {@link SharedPreferences#getAll()} and
     *                {@link SharedPreferences.Editor#clear()} operations.
     * @param write {@code true} for put/remove/clear operations; {@code false} for get operations.
     * @return {@code true} if the access is allowed; {@code false} otherwise.
     */
    protected boolean checkAccess(String prefFileName, String prefKey, boolean write) {
        return true;
    }

    /**
     * Called at application startup to register preference change listeners.
     *
     * @return Always returns {@code true}.
     */
    @Override
    public boolean onCreate() {
        // We register the shared preference listeners whenever the provider
        // is created. This method is called before almost all other code in
        // the app, which ensures that we never miss a preference change.
        for (RemotePreferenceFile file : mPrefFiles) {
            Context context = getContext();
            if (file.isDeviceProtected() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context = context.createDeviceProtectedStorageContext();
            }
            SharedPreferences prefs = context.getSharedPreferences(file.getFileName(), Context.MODE_PRIVATE);
            prefs.registerOnSharedPreferenceChangeListener(this);
            mPreferences.put(file.getFileName(), prefs);
        }
        return true;
    }

    /**
     * Returns a cursor for the specified preference(s). If {@code uri}
     * is in the form {@code content://authority/prefFileName/prefKey}, the
     * cursor will contain a single row containing the queried preference.
     * If {@code uri} is in the form {@code content://authority/prefFileName},
     * the cursor will contain one row for each preference in the specified
     * file.
     *
     * @param uri Specifies the preference file and key (optional) to query.
     * @param projection Specifies which fields should be returned in the cursor.
     * @param selection Ignored.
     * @param selectionArgs Ignored.
     * @param sortOrder Ignored.
     * @return A cursor used to access the queried preference data.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        PrefPath prefPath = parseUri(uri);
        String prefFileName = prefPath.fileName;
        String rawPrefKey = prefPath.key;

        SharedPreferences prefs = getSharedPreferencesOrThrow(prefFileName, rawPrefKey, false);
        Map<String, ?> prefMap = prefs.getAll();

        // If no projection is specified, we return all columns.
        if (projection == null) {
            projection = RemoteContract.COLUMN_ALL;
        }

        // Fill out the cursor with the preference data. If the caller
        // didn't ask for a particular preference, we return all of them.
        MatrixCursor cursor = new MatrixCursor(projection);
        if (isSingleKey(rawPrefKey)) {
            Object prefValue = prefMap.get(rawPrefKey);
            cursor.addRow(buildRow(projection, rawPrefKey, prefValue));
        } else {
            for (Map.Entry<String, ?> entry : prefMap.entrySet()) {
                String prefKey = entry.getKey();
                Object prefValue = entry.getValue();
                cursor.addRow(buildRow(projection, prefKey, prefValue));
            }
        }

        return cursor;
    }

    /**
     * Not used in RemotePreferences. Always returns {@code null}.
     *
     * @param uri Ignored.
     * @return Always returns {@code null}.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Writes the value of the specified preference(s). If no key is specified,
     * {@link RemoteContract#COLUMN_TYPE} must be equal to {@link RemoteContract#TYPE_NULL},
     * representing the {@link SharedPreferences.Editor#clear()} operation.
     *
     * @param uri Specifies the preference file and key (optional) to write.
     * @param values Specifies the key (optional), type and value of the preference to write.
     * @return A URI representing the preference written, or {@code null} on failure.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            return null;
        }

        PrefPath prefPath = parseUri(uri);
        String prefFileName = prefPath.fileName;
        String prefKey = getKeyFromUriOrValues(prefPath, values);

        SharedPreferences prefs = getSharedPreferencesOrThrow(prefFileName, prefKey, true);
        SharedPreferences.Editor editor = prefs.edit();

        putPreference(editor, prefKey, values);

        if (editor.commit()) {
            return getPreferenceUri(prefFileName, prefKey);
        } else {
            return null;
        }
    }

    /**
     * Writes multiple preference values at once. {@code uri} must
     * be in the form {@code content://authority/prefFileName}. See
     * {@link #insert(Uri, ContentValues)} for more information.
     *
     * @param uri Specifies the preference file to write to.
     * @param values See {@link #insert(Uri, ContentValues)}.
     * @return The number of preferences written, or 0 on failure.
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        PrefPath prefPath = parseUri(uri);
        String prefFileName = prefPath.fileName;
        if (isSingleKey(prefPath.key)) {
            throw new IllegalArgumentException("Cannot bulk insert with single key URI");
        }

        SharedPreferences prefs = getSharedPreferencesByName(prefFileName);
        SharedPreferences.Editor editor = prefs.edit();

        for (ContentValues value : values) {
            String prefKey = getKeyFromValues(value);
            checkAccessOrThrow(prefFileName, prefKey, true);
            putPreference(editor, prefKey, value);
        }

        if (editor.commit()) {
            return values.length;
        } else {
            return 0;
        }
    }

    /**
     * Deletes the specified preference(s). If {@code uri} is in the form
     * {@code content://authority/prefFileName/prefKey}, this will only delete
     * the one preference specified in the URI; if {@code uri} is in the form
     * {@code content://authority/prefFileName}, clears all preferences.
     *
     * @param uri Specifies the preference file and key (optional) to delete.
     * @param selection Ignored.
     * @param selectionArgs Ignored.
     * @return 1 if the preferences committed successfully, or 0 on failure.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        PrefPath prefPath = parseUri(uri);
        String prefFileName = prefPath.fileName;
        String prefKey = prefPath.key;

        SharedPreferences prefs = getSharedPreferencesOrThrow(prefFileName, prefKey, true);
        SharedPreferences.Editor editor = prefs.edit();

        if (isSingleKey(prefKey)) {
            editor.remove(prefKey);
        } else {
            editor.clear();
        }

        // There's no reliable method of getting the actual number of
        // preference values changed, so callers should not rely on this
        // value. A return value of 1 means success, 0 means failure.
        if (editor.commit()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Updates the value of the specified preference(s). This is a wrapper
     * around {@link #insert(Uri, ContentValues)} if {@code values} is not
     * {@code null}, or {@link #delete(Uri, String, String[])} if {@code values}
     * is {@code null}.
     *
     * @param uri Specifies the preference file and key (optional) to update.
     * @param values {@code null} to delete the preference,
     * @param selection Ignored.
     * @param selectionArgs Ignored.
     * @return 1 if the preferences committed successfully, or 0 on failure.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values == null) {
            return delete(uri, selection, selectionArgs);
        } else {
            return insert(uri, values) != null ? 1 : 0;
        }
    }

    /**
     * Listener for preference value changes in the local application.
     * Re-raises the event through the
     * {@link ContentResolver#notifyChange(Uri, ContentObserver)} API
     * to any registered {@link ContentObserver} objects. Note that this
     * is NOT called for {@link SharedPreferences.Editor#clear()}.
     *
     * @param prefs The preference file that changed.
     * @param prefKey The preference key that changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String prefKey) {
        RemotePreferenceFile prefFile = getSharedPreferencesFile(prefs);
        Uri uri = getPreferenceUri(prefFile.getFileName(), prefKey);
        Context context = getContext();
        if (prefFile.isDeviceProtected() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = context.createDeviceProtectedStorageContext();
        }
        ContentResolver resolver = context.getContentResolver();
        resolver.notifyChange(uri, null);
    }

    /**
     * Writes the value of the specified preference(s). If {@code prefKey}
     * is empty, {@code values} must contain {@link RemoteContract#TYPE_NULL}
     * for the type, representing the {@link SharedPreferences.Editor#clear()}
     * operation.
     *
     * @param editor The preference file to modify.
     * @param prefKey The preference key to modify, or {@code ""} for the entire file.
     * @param values The values to write.
     */
    @SuppressWarnings("ConstantConditions")
    private void putPreference(SharedPreferences.Editor editor, String prefKey, ContentValues values) {
        // Get the new value type. Note that we manually check
        // for null, then unbox the Integer so we don't cause a NPE.
        Integer type = values.getAsInteger(RemoteContract.COLUMN_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Invalid or no preference type specified");
        }

        // deserializeInput makes sure the actual object type matches
        // the expected type, so we must perform this step before actually
        // performing any actions.
        Object rawValue = values.get(RemoteContract.COLUMN_VALUE);
        Object value = RemoteUtils.deserializeInput(rawValue, type);

        // If we are writing to the "directory" and the type is null,
        // then we should clear the preferences.
        if (!isSingleKey(prefKey)) {
            if (type == RemoteContract.TYPE_NULL) {
                editor.clear();
                return;
            } else {
                throw new IllegalArgumentException("Attempting to insert preference with null or empty key");
            }
        }

        switch (type) {
        case RemoteContract.TYPE_NULL:
            editor.remove(prefKey);
            break;
        case RemoteContract.TYPE_STRING:
            editor.putString(prefKey, (String)value);
            break;
        case RemoteContract.TYPE_STRING_SET:
            if (Build.VERSION.SDK_INT >= 11) {
                editor.putStringSet(prefKey, RemoteUtils.castStringSet(value));
            } else {
                throw new IllegalArgumentException("String set preferences not supported on API < 11");
            }
            break;
        case RemoteContract.TYPE_INT:
            editor.putInt(prefKey, (Integer)value);
            break;
        case RemoteContract.TYPE_LONG:
            editor.putLong(prefKey, (Long)value);
            break;
        case RemoteContract.TYPE_FLOAT:
            editor.putFloat(prefKey, (Float)value);
            break;
        case RemoteContract.TYPE_BOOLEAN:
            editor.putBoolean(prefKey, (Boolean)value);
            break;
        default:
            throw new IllegalArgumentException("Cannot set preference with type " + type);
        }
    }

    /**
     * Used to project a preference value to the schema requested by the caller.
     *
     * @param projection The projection requested by the caller.
     * @param key The preference key.
     * @param value The preference value.
     * @return A row representing the preference using the given schema.
     */
    private Object[] buildRow(String[] projection, String key, Object value) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < row.length; ++i) {
            String col = projection[i];
            if (RemoteContract.COLUMN_KEY.equals(col)) {
                row[i] = key;
            } else if (RemoteContract.COLUMN_TYPE.equals(col)) {
                row[i] = RemoteUtils.getPreferenceType(value);
            } else if (RemoteContract.COLUMN_VALUE.equals(col)) {
                row[i] = RemoteUtils.serializeOutput(value);
            } else {
                throw new IllegalArgumentException("Invalid column name: " + col);
            }
        }
        return row;
    }

    /**
     * Parses the preference file and key from a query URI. If the key
     * is not specified, the returned tuple will contain {@code ""} as the key.
     *
     * @param uri The URI to parse.
     * @return A tuple containing the preference file name and key.
     */
    private PrefPath parseUri(Uri uri) {
        int match = mUriMatcher.match(uri);
        if (match != PREFERENCE_ID && match != PREFERENCES_ID) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        // The URI must fall under one of these patterns:
        //
        //   content://authority/prefFileName/prefKey
        //   content://authority/prefFileName/
        //   content://authority/prefFileName
        //
        // The match ID will be PREFERENCE_ID under the first case,
        // and PREFERENCES_ID under the second and third cases
        // (UriMatcher ignores trailing slashes).
        List<String> pathSegments = uri.getPathSegments();
        String prefFileName = pathSegments.get(0);
        String prefKey = "";
        if (match == PREFERENCE_ID) {
            prefKey = pathSegments.get(1);
        }
        return new PrefPath(prefFileName, prefKey);
    }

    /**
     * Returns whether the specified key represents a single preference key
     * (as opposed to the entire preference file).
     *
     * @param prefKey The preference key to check.
     * @return Whether the key refers to a single preference.
     */
    private static boolean isSingleKey(String prefKey) {
        return prefKey != null && prefKey.length() != 0;
    }

    /**
     * Parses the preference key from {@code values}. If the key is not
     * specified in the values, {@code ""} is returned.
     *
     * @param values The query values to parse.
     * @return The parsed key, or {@code ""} if no key was found.
     */
    private static String getKeyFromValues(ContentValues values) {
        String key = values.getAsString(RemoteContract.COLUMN_KEY);
        if (key == null) {
            key = "";
        }
        return key;
    }

    /**
     * Parses the preference key from the specified sources. Since there
     * are two ways to specify the key (from the URI or from the query values),
     * the only allowed combinations are:
     *
     * uri.key == values.key
     * uri.key != null and values.key == null = URI key is used
     * uri.key == null and values.key != null = values key is used
     * uri.key == null and values.key == null = no key
     *
     * If none of these conditions are met, an exception is thrown.
     *
     * @param prefPath Parsed URI key from {@link #parseUri(Uri)}.
     * @param values Query values provided by the caller.
     * @return The parsed key.
     */
    private static String getKeyFromUriOrValues(PrefPath prefPath, ContentValues values) {
        String uriKey = prefPath.key;
        String valuesKey = getKeyFromValues(values);
        if (uriKey.length() != 0 && valuesKey.length() != 0) {
            // If a key is specified in both the URI and
            // ContentValues, they must match
            if (!uriKey.equals(valuesKey)) {
                throw new IllegalArgumentException("Conflicting keys specified in URI and ContentValues");
            }
            return uriKey;
        } else if (uriKey.length() != 0) {
            return uriKey;
        } else if (valuesKey.length() != 0) {
            return valuesKey;
        } else {
            return "";
        }
    }

    /**
     * Checks that the caller has permissions to access the specified preference.
     * Throws an exception if permission is denied.
     *
     * @param prefFileName The preference file to be accessed.
     * @param prefKey The preference key to be accessed.
     * @param write Whether the operation will modify the preference.
     */
    private void checkAccessOrThrow(String prefFileName, String prefKey, boolean write) {
        if (!checkAccess(prefFileName, prefKey, write)) {
            throw new SecurityException("Insufficient permissions to access: " + prefFileName + "/" + prefKey);
        }
    }

    /**
     * Returns the {@link SharedPreferences} instance with the specified name.
     * This is essentially equivalent to {@link Context#getSharedPreferences(String, int)},
     * except that it will used the internally cached version, and throws an
     * exception if the provider was not configured to access that preference file.
     *
     * @param prefFileName The name of the preference file to access.
     * @return The {@link SharedPreferences} instance with the specified file name.
     */
    private SharedPreferences getSharedPreferencesByName(String prefFileName) {
        SharedPreferences prefs = mPreferences.get(prefFileName);
        if (prefs == null) {
            throw new IllegalArgumentException("Unknown preference file name: " + prefFileName);
        }
        return prefs;
    }

    /**
     * Returns the file name for a {@link SharedPreferences} instance.
     * Throws an exception if the provider was not configured to access
     * the specified preferences.
     *
     * @param prefs The shared preferences object.
     * @return The name of the preference file.
     */
    private String getSharedPreferencesFileName(SharedPreferences prefs) {
        for (Map.Entry<String, SharedPreferences> entry : mPreferences.entrySet()) {
            if (entry.getValue() == prefs) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Unknown preference file");
    }

    /**
     * Get the corresponding {@link RemotePreferenceFile} object for a
     * {@link SharedPreferences} instance. Throws an exception if the
     * provider was not configured to access the specified preferences.
     *
     * @param prefs The shared preferences object.
     * @return The corresponding {@link RemotePreferenceFile} object.
     */
    private RemotePreferenceFile getSharedPreferencesFile(SharedPreferences prefs) {
        String prefFileName = getSharedPreferencesFileName(prefs);
        for (RemotePreferenceFile file : mPrefFiles) {
            if (file.getFileName().equals(prefFileName)) {
                return file;
            }
        }
        throw new IllegalArgumentException("Unknown preference file");
    }

    /**
     * Returns the {@link SharedPreferences} instance with the specified name,
     * checking that the caller has permissions to access the specified key within
     * that file. If not, an exception will be thrown.
     *
     * @param prefFileName The preference file to be accessed.
     * @param prefKey The preference key to be accessed.
     * @param write Whether the operation will modify the preference.
     * @return The {@link SharedPreferences} instance with the specified file name.
     */
    private SharedPreferences getSharedPreferencesOrThrow(String prefFileName, String prefKey, boolean write) {
        checkAccessOrThrow(prefFileName, prefKey, write);
        return getSharedPreferencesByName(prefFileName);
    }

    /**
     * Builds a URI for the specified preference file and key that can be used
     * to later query the same preference.
     *
     * @param prefFileName The preference file.
     * @param prefKey The preference key.
     * @return A URI representing the specified preference.
     */
    private Uri getPreferenceUri(String prefFileName, String prefKey) {
        Uri.Builder builder = mBaseUri.buildUpon().appendPath(prefFileName);
        if (isSingleKey(prefKey)) {
            builder.appendPath(prefKey);
        }
        return builder.build();
    }

    /**
     * Basically just a tuple of (preference file, preference key).
     */
    private static class PrefPath {
        private final String fileName;
        private final String key;

        private PrefPath(String prefFileName, String prefKey) {
            fileName = prefFileName;
            key = prefKey;
        }
    }
}
