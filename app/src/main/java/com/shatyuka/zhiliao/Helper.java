package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;

import org.xmlpull.v1.XmlPullParser;

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
    static Class<?> PreferenceInflater;
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
    static Class<?> FeedAdvert;
    static Class<?> Advert;
    static Class<?> Ad;
    static Class<?> NextContentAnimationView;
    static Class<?> ContentMixAdapter;
    static Class<?> ContentMixPagerFragment;
    static Class<?> BaseTemplateNewFeedHolder;
    static Class<?> TemplateFeed;
    static Class<?> ViewHolder;
    static Class<?> SugarHolder;
    static Class<?> TemplateRoot;
    static Class<?> JacksonResponseBodyConverter;
    static Class<?> SearchTopTabsItemList;
    static Class<?> PresetWords;

    static Method findPreference;
    static Method setSummary;
    static Method setIcon;
    static Method setVisible;
    static Method getKey;
    static Method setChecked;
    static Method setOnPreferenceChangeListener;
    static Method setOnPreferenceClickListener;
    static Method addFragmentToOverlay;
    static Method addFragmentToOverlay_old;
    static Method setSharedPreferencesName;
    static Method getContext;
    static Method getText;
    static Method isShowLaunchAd;
    static Method showShareAd;
    static Method onNestChildScrollRelease;
    static Method isLinkZhihu;
    static Method isLinkZhihuWrap;
    static Method isReadyPageTurning;
    static Method getMenuName;
    static Method shouldInterceptRequest;
    static Method addPreferencesFromResource;
    static Method inflate;
    static Method getItemCount;
    static Method convert;

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
    static Field Tab_tabView;
    static Field ActionSheetLayout_callbackList;
    static Field ViewHolder_itemView;
    static Field SugarHolder_mData;
    static Field TemplateRoot_unique;
    static Field SearchTopTabsItemList_commercialData;
    static Field PresetWords_preset;
    static Field ContentMixAdapter_fragment;
    static Field ContentMixPagerFragment_type;
    static Field FeedList_data;

    static Pattern regex_title;
    static Pattern regex_author;
    static Pattern regex_content;

    static int width;
    static int height;

    static int id_title;

    static Context context;
    static SharedPreferences prefs;
    static Resources modRes;
    static PackageInfo packageInfo;

    static boolean init(ClassLoader classLoader) {
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.zhihu.android", 0);

            SettingsFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.preference.SettingsFragment");
            DebugFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.DebugFragment");
            Preference = classLoader.loadClass("androidx.preference.Preference");
            SwitchPreference = classLoader.loadClass("com.zhihu.android.app.ui.widget.SwitchPreference");
            OnPreferenceChangeListener = classLoader.loadClass("androidx.preference.Preference$c");
            OnPreferenceClickListener = classLoader.loadClass("androidx.preference.Preference$d");
            PreferenceFragmentCompat = classLoader.loadClass("androidx.preference.g");
            PreferenceManager = classLoader.loadClass("androidx.preference.j");
            PreferenceInflater = classLoader.loadClass("androidx.preference.i");
            PageInfoType = classLoader.loadClass("com.zhihu.android.data.analytics.PageInfoType");
            ZHIntent = classLoader.loadClass("com.zhihu.android.answer.entrance.AnswerPagerEntance").getMethod("buildIntent", long.class).getReturnType();
            MainActivity = classLoader.loadClass("com.zhihu.android.app.ui.activity.MainActivity");
            BasePreferenceFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.BasePreferenceFragment");
            PreferenceGroup = classLoader.loadClass("androidx.preference.PreferenceGroup");
            IZhihuWebView = classLoader.loadClass("com.zhihu.android.app.search.ui.widget.SearchResultLayout").getDeclaredField("c").getType();
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
            try {
                VerticalPageTransformer = classLoader.loadClass("com.zhihu.android.answer.pager.VerticalViewPager$VerticalPageTransformer");
            } catch (ClassNotFoundException e) {
                VerticalPageTransformer = classLoader.loadClass("com.zhihu.android.answer.widget.VerticalViewPager$VerticalPageTransformer");
            }
            AnswerContentView = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView");
            AnswerPagerFragment = classLoader.loadClass("com.zhihu.android.answer.module.pager.AnswerPagerFragment");
            FeedsTabsFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment");
            FeedFollowAvatarCommonViewHolder = classLoader.loadClass("com.zhihu.android.moments.viewholders.FeedFollowAvatarCommonViewHolder");
            ZHMainTabLayout = classLoader.loadClass("com.zhihu.android.app.ui.widget.ZHMainTabLayout");
            BottomNavMenuItemView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemView");
            BottomNavMenuItemViewForIconOnly = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemViewForIconOnly");
            try {
                NotiUnreadCountKt = classLoader.loadClass("com.zhihu.android.notification.model.NotiUnreadCountKt");
            } catch (ClassNotFoundException ignored) {
            }
            NotiMsgModel = classLoader.loadClass("com.zhihu.android.notification.model.viewmodel.NotiMsgModel");
            LinkZhihuHelper = classLoader.loadClass("com.zhihu.android.app.mercury.k");
            VipEntranceView = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.widget.VipEntranceView");
            BottomNavMenuView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuView");
            IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.a.b");
            AdNetworkManager = classLoader.loadClass("com.zhihu.android.sdk.launchad.b");
            try {
                AnswerListWrapper = classLoader.loadClass("com.zhihu.android.question.api.model.AnswerListWrapper");
            } catch (ClassNotFoundException ignored) {
            }
            InternalNotificationManager = classLoader.loadClass("com.zhihu.android.app.feed.notification.InternalNotificationManager");
            ImageBaseActivity = classLoader.loadClass("com.zhihu.android.picture.activity.a");
            FeedAdvert = classLoader.loadClass("com.zhihu.android.api.model.FeedAdvert");
            Advert = classLoader.loadClass("com.zhihu.android.api.model.Advert");
            Ad = classLoader.loadClass("com.zhihu.android.api.model.Ad");
            if (packageInfo.versionCode > 2614) {
                NextContentAnimationView = classLoader.loadClass("com.zhihu.android.mix.widget.NextContentAnimationView");
                try {
                    ContentMixAdapter = classLoader.loadClass("com.zhihu.android.mix.a.a");
                    getItemCount = ContentMixAdapter.getMethod("getItemCount");
                } catch (NoSuchMethodException e) {
                    ContentMixAdapter = classLoader.loadClass("com.zhihu.android.mix.b.a");
                    getItemCount = ContentMixAdapter.getMethod("getItemCount");
                }
                ContentMixPagerFragment = classLoader.loadClass("com.zhihu.android.mix.fragment.ContentMixPagerFragment");
                ContentMixAdapter_fragment = ContentMixAdapter.getDeclaredField("f");
                ContentMixAdapter_fragment.setAccessible(true);
                ContentMixPagerFragment_type = ContentMixPagerFragment.getField("c");
            }
            BaseTemplateNewFeedHolder = classLoader.loadClass("com.zhihu.android.app.feed.ui.holder.template.optimal.BaseTemplateNewFeedHolder");
            TemplateFeed = classLoader.loadClass("com.zhihu.android.api.model.template.TemplateFeed");
            ViewHolder = classLoader.loadClass("androidx.recyclerview.widget.RecyclerView$ViewHolder");
            SugarHolder = classLoader.loadClass("com.zhihu.android.sugaradapter.SugarHolder");
            TemplateRoot = classLoader.loadClass("com.zhihu.android.api.model.template.TemplateRoot");
            try {
                JacksonResponseBodyConverter = classLoader.loadClass("com.zhihu.android.net.b.b");
            } catch (ClassNotFoundException e) {
                try {
                    JacksonResponseBodyConverter = classLoader.loadClass("retrofit2.b.a.c");
                } catch (ClassNotFoundException e2) {
                    JacksonResponseBodyConverter = classLoader.loadClass("j.b.a.c");
                }
            }
            SearchTopTabsItemList = classLoader.loadClass("com.zhihu.android.api.model.SearchTopTabsItemList");
            PresetWords = classLoader.loadClass("com.zhihu.android.api.model.PresetWords");

            findPreference = SettingsFragment.getMethod("a", CharSequence.class);
            setSummary = Preference.getMethod("a", CharSequence.class);
            setIcon = Preference.getMethod("a", Drawable.class);
            setVisible = Preference.getMethod("c", boolean.class);
            getKey = Preference.getMethod("C");
            setChecked = SwitchPreference.getMethod("g", boolean.class);
            setOnPreferenceChangeListener = Preference.getMethod("a", OnPreferenceChangeListener);
            setOnPreferenceClickListener = Preference.getMethod("a", OnPreferenceClickListener);
            setSharedPreferencesName = PreferenceManager.getMethod("a", String.class);
            getContext = BasePreferenceFragment.getMethod("getContext");
            getText = EditTextPreference.getMethod("i");
            onNestChildScrollRelease = NestChildScrollChange.getMethod("onNestChildScrollRelease", float.class, int.class);
            isReadyPageTurning = DirectionBoundView.getMethod("isReadyPageTurning");
            getMenuName = IMenuItem.getMethod("a");
            addPreferencesFromResource = PreferenceFragmentCompat.getMethod("b", int.class);
            inflate = PreferenceInflater.getMethod("a", XmlPullParser.class, Helper.PreferenceGroup);
            convert = JacksonResponseBodyConverter.getMethod("convert", Object.class);

            for (char i = 'a'; i <= 'z'; i++) {
                Class<?> ZhihuWebViewClient = XposedHelpers.findClassIfExists("com.zhihu.android.appview.a$" + i, classLoader);
                if (ZhihuWebViewClient != null) {
                    try {
                        shouldInterceptRequest = ZhihuWebViewClient.getMethod("a", IZhihuWebView, WebResourceRequest.class);
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    break;
                }
            }
            if (shouldInterceptRequest == null)
                throw new NoSuchMethodException("Method shouldInterceptRequest not found");

            for (char i = 'a'; i <= 'z'; i++) {
                Class<?> LaunchAdInterface = XposedHelpers.findClassIfExists("com.zhihu.android.app.util.c" + i, classLoader);
                if (LaunchAdInterface != null) {
                    try {
                        isShowLaunchAd = LaunchAdInterface.getMethod("isShowLaunchAd");
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    break;
                }
            }
            if (isShowLaunchAd == null)
                throw new NoSuchMethodException("Method isShowLaunchAd not found");

            Class<?> ShareFragment = XposedHelpers.findClassIfExists("com.zhihu.android.library.sharecore.fragment.ShareFragment", classLoader);
            if (ShareFragment != null) {
                Method[] methods = ShareFragment.getDeclaredMethods();
                for (Method method : methods) {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 1 && types[0] == View.class) {
                        showShareAd = method;
                        break;
                    }
                }
            }
            if (showShareAd == null)
                throw new NoSuchMethodException("Method showShareAd not found");

            try {
                addFragmentToOverlay = MainActivity.getMethod("addFragmentToOverlay", ZHIntent);
            } catch (NoSuchMethodException e) {
                Method[] methods = MainActivity.getMethods();
                for (Method method : methods) {
                    Class<?>[] types = method.getParameterTypes();
                    if (method.getName().equals("a") && types.length == 5 && types[0] == ZHIntent) {
                        addFragmentToOverlay_old = method;
                        break;
                    }
                }
                if (addFragmentToOverlay_old == null)
                    throw new NoSuchMethodException("Method addFragmentToOverlay not found");
            }

            try {
                isLinkZhihu = LinkZhihuHelper.getMethod("b", Uri.class);
            } catch (NoSuchMethodException e) {
                LinkZhihuHelper = classLoader.loadClass("com.zhihu.android.app.mercury.j");
                isLinkZhihu = LinkZhihuHelper.getMethod("b", Uri.class);
            }

            {
                Method[] methods = LinkZhihuHelper.getMethods();
                for (Method method : methods) {
                    Class<?>[] types = method.getParameterTypes();
                    if (method.getName().equals("a") && method.getReturnType() == boolean.class &&
                            types.length == 3 && types[2] == String.class && types[1] != Uri.class) {
                        isLinkZhihuWrap = method;
                        break;
                    }
                }
                if (isLinkZhihuWrap == null)
                    throw new NoSuchMethodException("Method isLinkZhihuWrap not found");
            }

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
            Tab_tabView = classLoader.loadClass("com.google.android.material.tabs.TabLayout$Tab").getField("view");
            ActionSheetLayout_callbackList = ActionSheetLayout.getDeclaredField("z");
            ActionSheetLayout_callbackList.setAccessible(true);
            ViewHolder_itemView = ViewHolder.getField("itemView");
            try {
                SugarHolder_mData = SugarHolder.getDeclaredField("c");
            } catch (NoSuchFieldException e) {
                SugarHolder_mData = SugarHolder.getDeclaredField("mData");
            }
            SugarHolder_mData.setAccessible(true);
            TemplateRoot_unique = TemplateRoot.getField("unique");
            SearchTopTabsItemList_commercialData = SearchTopTabsItemList.getField("commercialData");
            PresetWords_preset = PresetWords.getField("preset");
            FeedList_data = classLoader.loadClass("com.zhihu.android.api.model.FeedList").getField("data");

            regex_title = compileRegex(prefs.getString("edit_title", ""));
            regex_author = compileRegex(prefs.getString("edit_author", ""));
            regex_content = compileRegex(prefs.getString("edit_content", ""));

            height = (int) (context.getResources().getDisplayMetrics().density * 160);
            width = height / 2;

            id_title = context.getResources().getIdentifier("title", "id", MainHook.hookPackage);

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
