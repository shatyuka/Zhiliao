package com.crossbowffs.remotepreferences;

/**
 * Represents a single preference file and the information needed to
 * access that preference file.
 */
public class RemotePreferenceFile {
    private final String mFileName;
    private final boolean mIsDeviceProtected;

    /**
     * Initializes the preference file information. If you are targeting Android
     * N or above and the preference needs to be accessed before the first unlock,
     * set {@code isDeviceProtected} to {@code true}.
     *
     * @param fileName Name of the preference file.
     * @param isDeviceProtected {@code true} if the preference file is device protected,
     *                          {@code false} if it is credential protected.
     */
    public RemotePreferenceFile(String fileName, boolean isDeviceProtected) {
        mFileName = fileName;
        mIsDeviceProtected = isDeviceProtected;
    }

    /**
     * Initializes the preference file information. Assumes the preferences are
     * located in credential protected storage.
     *
     * @param fileName Name of the preference file.
     */
    public RemotePreferenceFile(String fileName) {
        this(fileName, false);
    }

    /**
     * Returns the name of the preference file.
     *
     * @return The name of the preference file.
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * Returns whether the preferences are located in device protected storage.
     *
     * @return {@code true} if the preference file is device protected,
     *         {@code false} if it is credential protected.
     */
    public boolean isDeviceProtected() {
        return mIsDeviceProtected;
    }

    /**
     * Converts an array of preference file names to {@link RemotePreferenceFile}
     * objects. Assumes all preference files are NOT in device protected storage.
     *
     * @param prefFileNames The names of the preference files to expose.
     * @return An array of {@link RemotePreferenceFile} objects.
     */
    public static RemotePreferenceFile[] fromFileNames(String[] prefFileNames) {
        RemotePreferenceFile[] prefFiles = new RemotePreferenceFile[prefFileNames.length];
        for (int i = 0; i < prefFileNames.length; i++) {
            prefFiles[i] = new RemotePreferenceFile(prefFileNames[i]);
        }
        return prefFiles;
    }
}
