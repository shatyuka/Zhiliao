package com.crossbowffs.remotepreferences;

/**
 * Constants used for communicating with the preference provider.
 * These are implementation details of the RemotePreferences API,
 * and should not be used by client code. (Public for testing
 * purposes only)
 */
public class RemoteContract {
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_VALUE = "value";
    public static final String[] COLUMN_ALL = {
        RemoteContract.COLUMN_KEY,
        RemoteContract.COLUMN_TYPE,
        RemoteContract.COLUMN_VALUE
    };

    public static final int TYPE_NULL = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_STRING_SET = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_BOOLEAN = 6;
}
