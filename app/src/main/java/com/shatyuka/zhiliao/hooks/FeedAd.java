package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class FeedAd implements IHook {

    static Class<?> FeedFragment;
    static Class<?> BasePagingFragment;
    static Class<?> FeedAdvert;
    static Class<?> ListAd;
    static Class<?> Advert;
    static Class<?> Ad;

    static Field FeedList_data;

    @Override
    public String getName() {
        return "去信息流广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        FeedFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui2.feed.FeedFragment");

        BasePagingFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment");
        try {
            FeedAdvert = classLoader.loadClass("com.zhihu.android.api.model.FeedAdvert");
            ListAd = classLoader.loadClass("com.zhihu.android.api.model.ListAd");
            Advert = classLoader.loadClass("com.zhihu.android.api.model.Advert");
            Ad = classLoader.loadClass("com.zhihu.android.api.model.Ad");
        } catch (ClassNotFoundException ignore) {
            FeedAdvert = classLoader.loadClass("com.zhihu.android.adbase.model.FeedAdvert");
            ListAd = classLoader.loadClass("com.zhihu.android.adbase.model.ListAd");
            Advert = classLoader.loadClass("com.zhihu.android.adbase.model.Advert");
            Ad = classLoader.loadClass("com.zhihu.android.adbase.model.Ad");
        }

        FeedList_data = classLoader.loadClass("com.zhihu.android.api.model.FeedList").getField("data");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllMethods(BasePagingFragment, "postRefreshSucceed", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    if (param.thisObject.getClass() == FeedFragment && param.args[0] != null) {
                        filterFeedList((List<?>) FeedList_data.get(param.args[0]));
                    }
                }
            }
        });

        XposedBridge.hookAllMethods(BasePagingFragment, "postLoadMoreSucceed", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    if (param.thisObject.getClass() == FeedFragment && param.args[0] != null) {
                        filterFeedList((List<?>) FeedList_data.get(param.args[0]));
                    }
                }
            }
        });

        XposedBridge.hookAllMethods(Helper.MorphAdHelper, "resolveCommonAdParam", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });

        XposedBridge.hookAllMethods(Helper.MorphAdHelper, "resolveAnswerAdParam", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });

        XposedBridge.hookAllMethods(Helper.MorphAdHelper, "resolve", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });

        XposedBridge.hookAllMethods(Advert, "isSlidingWindow", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });

        XposedBridge.hookAllMethods(Ad, "isFloatAdCard", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });

    }

    private void filterFeedList(List<?> feedListData) {
        if (feedListData == null || feedListData.isEmpty()) {
            return;
        }

        feedListData.removeIf(this::shouldFilterFeed);
    }

    private boolean shouldFilterFeed(Object feedData) {
        return feedData.getClass() == FeedAdvert;
    }
}
