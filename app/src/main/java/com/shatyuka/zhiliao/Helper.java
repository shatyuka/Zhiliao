package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

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
    static Class<?> IZhihuWebView;

    static Method findPreference;
    static Method setSummary;
    static Method setIcon;
    static Method setOnPreferenceChangeListener;
    static Method setOnPreferenceClickListener;
    static Method addFragmentToOverlay;
    static Method setSharedPreferencesName;
    static Method getContext;
    static Method isShowLaunchAd;
    static Method showShareAd;

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
            IZhihuWebView = classLoader.loadClass("com.zhihu.android.app.market.newhome.ui.view.VillaLayout").getDeclaredMethod("getWebView").getReturnType();

            findPreference = SettingsFragment.getMethod("a", CharSequence.class);
            setSummary = Preference.getMethod("a", CharSequence.class);
            setIcon = Preference.getMethod("a", Drawable.class);
            setOnPreferenceChangeListener = Preference.getMethod("a", OnPreferenceChangeListener);
            setOnPreferenceClickListener = Preference.getMethod("a", OnPreferenceClickListener);
            addFragmentToOverlay = MainActivity.getMethod("addFragmentToOverlay", ZHIntent);
            setSharedPreferencesName = PreferenceManager.getMethod("a", String.class);
            getContext = BasePreferenceFragment.getMethod("getContext");

            boolean foundLaunchAdInterface = false;
            for (char i = 'a'; i <= 'z'; i++) {
                Class<?> LaunchAdInterface = XposedHelpers.findClassIfExists("com.zhihu.android.app.util.c" + i, classLoader);
                if (LaunchAdInterface != null) {
                    try {
                        isShowLaunchAd = LaunchAdInterface.getMethod("isShowLaunchAd");
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    foundLaunchAdInterface = true;
                    break;
                }
            }
            if (!foundLaunchAdInterface)
                return false;

            boolean foundshowShareAd = false;
            Class<?> ShareFragment = XposedHelpers.findClassIfExists("com.zhihu.android.library.sharecore.fragment.ShareFragment", classLoader);
            if (ShareFragment == null)
                return false;
            Method[] methods = ShareFragment.getDeclaredMethods();
            for (Method method : methods) {
                Class<?>[] types = method.getParameterTypes();
                if (types.length == 1 && types[0] == View.class) {
                    foundshowShareAd = true;
                    showShareAd = method;
                    break;
                }
            }
            if (!foundshowShareAd)
                return false;

            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            XposedBridge.log("[Zhilaio] " + e.toString());
            return false;
        }
    }
}
