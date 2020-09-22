package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    static Class<?> BasePagingFragment;
    static Class<?> BaseAppView;
    static Class<?> MorphAdHelper;
    static Class<?> InnerDeserializer;
    static Class<?> ApiTemplateRoot;
    static Class<?> DataUnique;
    static Class<?> MarketCard;
    static Class<?> FeedsTabsTopEntranceManager;
    static Class<?> ApiFeedContent;
    static Class<?> ApiText;
    static Class<?> EditTextPreference;
    static Class<?> ActionSheetLayout;
    static Class<?> NestChildScrollChange;
    static Class<?> NextBtnClickListener;
    static Class<?> DirectionBoundView;
    static Class<?> VerticalPageTransformer;
    static Class<?> AnswerContentView;
    static Class<?> AnswerPagerFragment;

    static Method findPreference;
    static Method setSummary;
    static Method setIcon;
    static Method getKey;
    static Method setOnPreferenceChangeListener;
    static Method setOnPreferenceClickListener;
    static Method addFragmentToOverlay;
    static Method setSharedPreferencesName;
    static Method getContext;
    static Method getText;
    static Method isShowLaunchAd;
    static Method showShareAd;
    static Method onNestChildScrollRelease;

    static Field panel_text;

    static Pattern regex_title;
    static Pattern regex_author;
    static Pattern regex_content;

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
            BasePagingFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment");
            BaseAppView = classLoader.loadClass("com.zhihu.android.appview.a$k");
            MorphAdHelper = classLoader.loadClass("com.zhihu.android.morph.ad.utils.MorphAdHelper");
            InnerDeserializer = classLoader.loadClass("com.zhihu.android.api.util.ZHObjectRegistryCenter$InnerDeserializer");
            ApiTemplateRoot = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiTemplateRoot");
            DataUnique = classLoader.loadClass("com.zhihu.android.api.model.template.DataUnique");
            MarketCard = classLoader.loadClass("com.zhihu.android.api.model.MarketCard");
            FeedsTabsTopEntranceManager = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment").getDeclaredField("mEntranceManger").getType();
            ApiFeedContent = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiFeedContent");
            ApiText = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiText");
            EditTextPreference = classLoader.loadClass("androidx.preference.EditTextPreference");
            ActionSheetLayout = classLoader.loadClass("com.zhihu.android.content.widget.ActionSheetLayout");
            NestChildScrollChange = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView$mNestChildScrollChange$1");
            NextBtnClickListener = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView$mNextBtnClickListener$1");
            DirectionBoundView = classLoader.loadClass("com.zhihu.android.answer.widget.DirectionBoundView");
            VerticalPageTransformer = classLoader.loadClass("com.zhihu.android.answer.pager.VerticalViewPager$VerticalPageTransformer");
            AnswerContentView = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView");
            AnswerPagerFragment = classLoader.loadClass("com.zhihu.android.answer.module.pager.AnswerPagerFragment");

            findPreference = SettingsFragment.getMethod("a", CharSequence.class);
            setSummary = Preference.getMethod("a", CharSequence.class);
            setIcon = Preference.getMethod("a", Drawable.class);
            getKey = Preference.getMethod("C");
            setOnPreferenceChangeListener = Preference.getMethod("a", OnPreferenceChangeListener);
            setOnPreferenceClickListener = Preference.getMethod("a", OnPreferenceClickListener);
            addFragmentToOverlay = MainActivity.getMethod("addFragmentToOverlay", ZHIntent);
            setSharedPreferencesName = PreferenceManager.getMethod("a", String.class);
            getContext = BasePreferenceFragment.getMethod("getContext");
            getText = EditTextPreference.getMethod("i");
            onNestChildScrollRelease = NestChildScrollChange.getMethod("onNestChildScrollRelease", float.class, int.class);

            boolean foundisShowLaunchAd = false;
            for (char i = 'a'; i <= 'z'; i++) {
                Class<?> LaunchAdInterface = XposedHelpers.findClassIfExists("com.zhihu.android.app.util.c" + i, classLoader);
                if (LaunchAdInterface != null) {
                    try {
                        isShowLaunchAd = LaunchAdInterface.getMethod("isShowLaunchAd");
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    foundisShowLaunchAd = true;
                    break;
                }
            }
            if (!foundisShowLaunchAd)
                throw new NoSuchMethodException("Method isShowLaunchAd not found");

            boolean foundshowShareAd = false;
            Class<?> ShareFragment = XposedHelpers.findClassIfExists("com.zhihu.android.library.sharecore.fragment.ShareFragment", classLoader);
            if (ShareFragment != null) {
                Method[] methods = ShareFragment.getDeclaredMethods();
                for (Method method : methods) {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 1 && types[0] == View.class) {
                        foundshowShareAd = true;
                        showShareAd = method;
                        break;
                    }
                }
            }
            if (ShareFragment == null || !foundshowShareAd)
                throw new NoSuchMethodException("Method showShareAd not found");

            panel_text = ApiText.getField("panel_text");

            regex_title = compileRegex(prefs.getString("edit_title", ""));
            regex_author = compileRegex(prefs.getString("edit_author", ""));
            regex_content = compileRegex(prefs.getString("edit_content", ""));

            return true;
        } catch (Exception e) {
            XposedBridge.log("[Zhiliao] " + e.toString());
            return false;
        }
    }

    static Pattern compileRegex(String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        } else try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException ignore) {
            return null;
        }
    }
}
