package com.crossbowffs.remotepreferences;

/**
 * Thrown if the preference provider could not be accessed.
 * This is commonly thrown under these conditions:
 * <ul>
 *     <li>Preference provider component is disabled</li>
 *     <li>Preference provider denied access via {@link RemotePreferenceProvider#checkAccess(String, String, boolean)}</li>
 *     <li>Insufficient permissions to access provider (via {@code AndroidManifest.xml})</li>
 *     <li>Incorrect provider authority/file name passed to constructor</li>
 * </ul>
 */
public class RemotePreferenceAccessException extends RuntimeException {
    public RemotePreferenceAccessException() {

    }

    public RemotePreferenceAccessException(String detailMessage) {
        super(detailMessage);
    }

    public RemotePreferenceAccessException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RemotePreferenceAccessException(Throwable throwable) {
        super(throwable);
    }
}
