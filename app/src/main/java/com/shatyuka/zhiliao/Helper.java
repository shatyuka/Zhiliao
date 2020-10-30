package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;

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
    static Class<?> SwitchPreference;
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
    static Class<?> MorphAdHelper;
    static Class<?> InnerDeserializer;
    static Class<?> ApiTemplateRoot;
    static Class<?> DataUnique;
    static Class<?> ApiFeedCard;
    static Class<?> MarketCard;
    static Class<?> FeedsTabsTopEntranceManager;
    static Class<?> ApiFeedContent;
    static Class<?> ApiText;
    static Class<?> ApiLine;
    static Class<?> ApiElement;
    static Class<?> EditTextPreference;
    static Class<?> ActionSheetLayout;
    static Class<?> NestChildScrollChange;
    static Class<?> NextBtnClickListener;
    static Class<?> DirectionBoundView;
    static Class<?> VerticalPageTransformer;
    static Class<?> AnswerContentView;
    static Class<?> AnswerPagerFragment;
    static Class<?> FeedsTabsFragment;
    static Class<?> FeedFollowAvatarCommonViewHolder;
    static Class<?> ZHMainTabLayout;
    static Class<?> BottomNavMenuItemView;
    static Class<?> BottomNavMenuItemViewForIconOnly;
    static Class<?> NotiUnreadCountKt;
    static Class<?> NotiMsgModel;
    static Class<?> LinkZhihuHelper;
    static Class<?> VipEntranceView;
    static Class<?> BottomNavMenuView;
    static Class<?> IMenuItem;
    static Class<?> AdNetworkManager;
    static Class<?> AnswerListWrapper;
    static Class<?> InternalNotificationManager;
    static Class<?> ImageBaseActivity;

    static Method findPreference;
    static Method setSummary;
    static Method setIcon;
    static Method setVisible;
    static Method getKey;
    static Method setChecked;
    static Method setOnPreferenceChangeListener;
    static Method setOnPreferenceClickListener;
    static Method addFragmentToOverlay;
    static Method setSharedPreferencesName;
    static Method getContext;
    static Method getText;
    static Method isShowLaunchAd;
    static Method showShareAd;
    static Method onNestChildScrollRelease;
    static Method isLinkZhihu;
    static Method isReadyPageTurning;
    static Method getMenuName;
    static Method shouldInterceptRequest;

    static Field ApiTemplateRoot_extra;
    static Field ApiTemplateRoot_common_card;
    static Field DataUnique_type;
    static Field ApiFeedCard_feed_content;
    static Field ApiFeedContent_title;
    static Field ApiFeedContent_content;
    static Field ApiFeedContent_sourceLine;
    static Field ApiText_panel_text;
    static Field ApiLine_elements;
    static Field ApiElement_text;
    static Field tabView;
    static Field callbackList;

    static Pattern regex_title;
    static Pattern regex_author;
    static Pattern regex_content;

    static int width;
    static int height;

    static Context context;
    static SharedPreferences prefs;
    static Resources modRes;

    static boolean init(ClassLoader classLoader) {
        try {
            SettingsFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.preference.SettingsFragment");
            DebugFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.DebugFragment");
            Preference = classLoader.loadClass("androidx.preference.Preference");
            SwitchPreference = classLoader.loadClass("com.zhihu.android.app.ui.widget.SwitchPreference");
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
            MorphAdHelper = classLoader.loadClass("com.zhihu.android.morph.ad.utils.MorphAdHelper");
            InnerDeserializer = classLoader.loadClass("com.zhihu.android.api.util.ZHObjectRegistryCenter$InnerDeserializer");
            ApiTemplateRoot = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiTemplateRoot");
            DataUnique = classLoader.loadClass("com.zhihu.android.api.model.template.DataUnique");
            ApiFeedCard = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiFeedCard");
            MarketCard = classLoader.loadClass("com.zhihu.android.api.model.MarketCard");
            FeedsTabsTopEntranceManager = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment").getDeclaredField("mEntranceManger").getType();
            ApiFeedContent = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiFeedContent");
            ApiText = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiText");
            ApiLine = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiLine");
            ApiElement = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiElement");
            EditTextPreference = classLoader.loadClass("androidx.preference.EditTextPreference");
            ActionSheetLayout = classLoader.loadClass("com.zhihu.android.content.widget.ActionSheetLayout");
            NestChildScrollChange = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView$mNestChildScrollChange$1");
            NextBtnClickListener = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView$mNextBtnClickListener$1");
            DirectionBoundView = classLoader.loadClass("com.zhihu.android.answer.widget.DirectionBoundView");
            VerticalPageTransformer = classLoader.loadClass("com.zhihu.android.answer.pager.VerticalViewPager$VerticalPageTransformer");
            AnswerContentView = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView");
            AnswerPagerFragment = classLoader.loadClass("com.zhihu.android.answer.module.pager.AnswerPagerFragment");
            FeedsTabsFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment");
            FeedFollowAvatarCommonViewHolder = classLoader.loadClass("com.zhihu.android.moments.viewholders.FeedFollowAvatarCommonViewHolder");
            ZHMainTabLayout = classLoader.loadClass("com.zhihu.android.app.ui.widget.ZHMainTabLayout");
            BottomNavMenuItemView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemView");
            BottomNavMenuItemViewForIconOnly = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemViewForIconOnly");
            NotiUnreadCountKt = classLoader.loadClass("com.zhihu.android.notification.model.NotiUnreadCountKt");
            NotiMsgModel = classLoader.loadClass("com.zhihu.android.notification.model.viewmodel.NotiMsgModel");
            LinkZhihuHelper = classLoader.loadClass("com.zhihu.android.app.mercury.k");
            VipEntranceView = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.widget.VipEntranceView");
            BottomNavMenuView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuView");
            IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.a.b");
            AdNetworkManager = classLoader.loadClass("com.zhihu.android.sdk.launchad.b");
            AnswerListWrapper = classLoader.loadClass("com.zhihu.android.question.api.model.AnswerListWrapper");
            InternalNotificationManager = classLoader.loadClass("com.zhihu.android.app.feed.notification.InternalNotificationManager");
            ImageBaseActivity = classLoader.loadClass("com.zhihu.android.picture.activity.a");

            findPreference = SettingsFragment.getMethod("a", CharSequence.class);
            setSummary = Preference.getMethod("a", CharSequence.class);
            setIcon = Preference.getMethod("a", Drawable.class);
            setVisible = Preference.getMethod("c", boolean.class);
            getKey = Preference.getMethod("C");
            setChecked = SwitchPreference.getMethod("g", boolean.class);
            setOnPreferenceChangeListener = Preference.getMethod("a", OnPreferenceChangeListener);
            setOnPreferenceClickListener = Preference.getMethod("a", OnPreferenceClickListener);
            addFragmentToOverlay = MainActivity.getMethod("addFragmentToOverlay", ZHIntent);
            setSharedPreferencesName = PreferenceManager.getMethod("a", String.class);
            getContext = BasePreferenceFragment.getMethod("getContext");
            getText = EditTextPreference.getMethod("i");
            onNestChildScrollRelease = NestChildScrollChange.getMethod("onNestChildScrollRelease", float.class, int.class);
            isLinkZhihu = LinkZhihuHelper.getMethod("b", Uri.class);
            isReadyPageTurning = DirectionBoundView.getMethod("isReadyPageTurning");
            getMenuName = IMenuItem.getMethod("a");

            boolean foundshouldInterceptRequest = false;
            for (char i = 'a'; i <= 'z'; i++) {
                Class<?> ZhihuWebViewClient = XposedHelpers.findClassIfExists("com.zhihu.android.appview.a$" + i, classLoader);
                if (ZhihuWebViewClient != null) {
                    try {
                        shouldInterceptRequest = ZhihuWebViewClient.getMethod("a", IZhihuWebView, WebResourceRequest.class);
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    foundshouldInterceptRequest = true;
                    break;
                }
            }
            if (!foundshouldInterceptRequest)
                throw new NoSuchMethodException("Method shouldInterceptRequest not found");

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

            ApiTemplateRoot_extra = ApiTemplateRoot.getField("extra");
            ApiTemplateRoot_common_card = ApiTemplateRoot.getField("common_card");
            DataUnique_type = DataUnique.getField("type");
            ApiFeedCard_feed_content = ApiFeedCard.getField("feed_content");
            ApiFeedContent_title = ApiFeedContent.getField("title");
            ApiFeedContent_content = ApiFeedContent.getField("content");
            ApiFeedContent_sourceLine = ApiFeedContent.getField("sourceLine");
            ApiText_panel_text = ApiText.getField("panel_text");
            ApiLine_elements = ApiLine.getField("elements");
            ApiElement_text = ApiElement.getField("text");
            tabView = classLoader.loadClass("com.google.android.material.tabs.TabLayout$Tab").getField("view");
            callbackList = Helper.ActionSheetLayout.getDeclaredField("z");
            callbackList.setAccessible(true);

            regex_title = compileRegex(prefs.getString("edit_title", ""));
            regex_author = compileRegex(prefs.getString("edit_author", ""));
            regex_content = compileRegex(prefs.getString("edit_content", ""));

            height = (int) (context.getResources().getDisplayMetrics().density * 160);
            width = height / 2;

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

    static Resources getModuleRes(String path) throws Throwable {
        AssetManager assetManager = AssetManager.class.newInstance();
        AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(assetManager, path);
        return new Resources(assetManager, null, null);
    }
}
