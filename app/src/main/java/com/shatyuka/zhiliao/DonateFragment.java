package com.shatyuka.zhiliao;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class DonateFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_donate, rootKey);
    }
}
