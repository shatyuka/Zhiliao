package com.shatyuka.zhiliao;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Random;

public class MySettingsFragment extends PreferenceFragmentCompat {
    public static class DlgException extends RuntimeException {
    }

    public static class WarnIconDialogFragment extends DialogFragment {
        boolean result = true;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_warnicon)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FragmentActivity activity = getActivity();
                            assert activity != null;
                            PackageManager p = activity.getPackageManager();
                            p.setComponentEnabledSetting(new ComponentName(activity.getPackageName(), "com.shatyuka.zhiliao.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                            throw new DlgException();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            result = false;
                            throw new DlgException();
                        }
                    }).setCancelable(false);
            return builder.create();
        }
    }

    public static class EulaDialogFragment extends DialogFragment {
        SwitchPreference switch_main;

        public EulaDialogFragment(SwitchPreference pref_switch) {
            switch_main = pref_switch;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_eula)
                    .setPositiveButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FragmentActivity activity = getActivity();
                            assert activity != null;
                            SharedPreferences prefs = activity.getSharedPreferences("com.shatyuka.zhiliao_preferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("accept_eula", true);
                            editor.apply();
                            switch_main.setChecked(true);
                        }
                    })
                    .setNegativeButton(R.string.dialog_reject, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FragmentActivity activity = getActivity();
                            assert activity != null;
                            activity.finishAndRemoveTask();
                        }
                    });
            return builder.create();
        }
    }

    static int version_click = 0;
    static int author_click = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        FragmentActivity activity = getActivity();
        assert activity != null;
        SharedPreferences prefs = activity.getSharedPreferences("com.shatyuka.zhiliao_preferences", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("accept_eula", false)) {
            EulaDialogFragment dlg = new EulaDialogFragment((SwitchPreference) findPreference("switch_mainswitch"));
            dlg.setCancelable(false);
            dlg.show(activity.getSupportFragmentManager(), null);
        }

        Preference switch_hideicon = findPreference("switch_hideicon");
        assert switch_hideicon != null;
        switch_hideicon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean value = (boolean) newValue;
                FragmentActivity activity = getActivity();
                assert activity != null;
                PackageManager p = activity.getPackageManager();
                if (value) {
                    WarnIconDialogFragment dlg = new WarnIconDialogFragment();
                    dlg.show(activity.getSupportFragmentManager(), null);
                    try {
                        Looper.loop();
                    } catch (DlgException e) {
                        return dlg.result;
                    }
                } else
                    p.setComponentEnabledSetting(new ComponentName(activity.getPackageName(), "com.shatyuka.zhiliao.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                return true;
            }
        });

        Preference preference_version = findPreference("preference_version");
        assert preference_version != null;
        preference_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                version_click++;
                if (version_click == 5) {
                    Toast.makeText(getContext(), "点我次数再多，更新也不会变快哦", Toast.LENGTH_SHORT).show();
                    version_click = 0;
                }
                return true;
            }
        });

        Preference preference_author = findPreference("preference_author");
        assert preference_author != null;
        preference_author.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                author_click++;
                if (author_click == 5) {
                    Toast.makeText(getContext(), getResources().getStringArray(R.array.click_author)[new Random().nextInt(4)], Toast.LENGTH_LONG).show();
                    author_click = 0;
                }
                return true;
            }
        });

        Preference preference_telegram = findPreference("preference_telegram");
        assert preference_telegram != null;
        preference_telegram.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://t.me/joinchat/OibCWxbdCMkJ2fG8J1DpQQ");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });
    }
}
