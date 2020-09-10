package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;

public class Helper {
    static Class<?> SettingsFragment;
    static Class<?> DebugFragment;
    static Class<?> Preference;
    static Class<?> OnPreferenceChangeListener;
    static Class<?> OnPreferenceClickListener;
    static Class<?> PreferenceFragmentCompat;
    static Class<?> PreferenceManager;
    static Class<?> PageInfoType;
    static Class<?> ZHIntent;
    static Class<?> MainActivity;
    static Class<?> BasePreferenceFragment;
    static Class<?> PreferenceGroup;

    static Method findPreference;
    static Method setSummary;
    static Method setIcon;
    static Method setOnPreferenceChangeListener;
    static Method setOnPreferenceClickListener;
    static Method addFragmentToOverlay;
    static Method setSharedPreferencesName;
    static Method getContext;

    static Context context;
    static SharedPreferences prefs;
    static Resources modRes;

    static boolean init(ClassLoader classLoader) {
        try {
            SettingsFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.preference.SettingsFragment");
            DebugFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.DebugFragment");
            Preference = classLoader.loadClass("androidx.preference.Preference");
            OnPreferenceChangeListener = classLoader.loadClass("androidx.preference.Preference$c");
            OnPreferenceClickListener = classLoader.loadClass("androidx.preference.Preference$d");
            PreferenceFragmentCompat = classLoader.loadClass("androidx.preference.g");
            PreferenceManager = classLoader.loadClass("androidx.preference.j");
            PageInfoType = classLoader.loadClass("com.zhihu.android.data.analytics.PageInfoType");
            ZHIntent = classLoader.loadClass("com.zhihu.android.answer.entrance.AnswerPagerEntance").getMethod("buildIntent", long.class).getReturnType();
            MainActivity = classLoader.loadClass("com.zhihu.android.app.ui.activity.MainActivity");
            BasePreferenceFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.BasePreferenceFragment");
            PreferenceGroup = classLoader.loadClass("androidx.preference.PreferenceGroup");

            findPreference = SettingsFragment.getMethod("a", CharSequence.class);
            setSummary = Preference.getMethod("a", CharSequence.class);
            setIcon = Preference.getMethod("a", Drawable.class);
            setOnPreferenceChangeListener = Preference.getMethod("a", OnPreferenceChangeListener);
            setOnPreferenceClickListener = Preference.getMethod("a", OnPreferenceClickListener);
            addFragmentToOverlay = MainActivity.getMethod("addFragmentToOverlay", ZHIntent);
            setSharedPreferencesName = PreferenceManager.getMethod("a", String.class);
            getContext = BasePreferenceFragment.getMethod("getContext");

            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            XposedBridge.log(e.getMessage());
            return false;
        }
    }
}
