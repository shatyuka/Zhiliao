package com.shatyuka.zhiliao.hooks;

import android.content.Context;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class StartPage implements IHook {
    static Class<?> AbCenterExtensions;

    static Method getDefaultTab;
    static Method getPreferenceInt;

    @Override
    public String getName() {
        return "自定义起始页面";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            Class<?> FeedsTabsFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment");
            Class<?> FeedTabViewModel = FeedsTabsFragment.getDeclaredField("mTabViewModel").getType();
            getDefaultTab = FeedTabViewModel.getDeclaredMethod("m");
        } catch (Exception ignored) {
            try {
                Class<?> FeedsViewModel = classLoader.loadClass("com.zhihu.android.app.feed.ui.e.b");
                getDefaultTab = FeedsViewModel.getMethod("a", Context.class, int.class);
            } catch (Exception ignored2) {
                Class<?> HotToRecommendHelper = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.helper.m");
                getDefaultTab = HotToRecommendHelper.getMethod("a", int.class, String.class);
            }
        }

        try {
            Class<?> FeedSharePreferencesHelper = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.b");
            getPreferenceInt = FeedSharePreferencesHelper.getMethod("b", Context.class, String.class, int.class);
        } catch (Exception ignored) {
        }

        try {
            AbCenterExtensions = classLoader.loadClass("com.zhihu.android.bootstrap.util.a");
        } catch (Exception ignored) {
        }

        try {
            Class<?> AppConfigParamUtil = classLoader.loadClass("com.zhihu.android.app.feed.util.c");
            Field isExplore = AppConfigParamUtil.getDeclaredField("c");
            isExplore.setAccessible(true);
            isExplore.set(null, false);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(getDefaultTab, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(Integer.parseInt(Helper.prefs.getString("list_startpage", "1")));
            }
        });

        if (getPreferenceInt != null) {
            XposedBridge.hookMethod(getPreferenceInt, XC_MethodReplacement.returnConstant(0));
        }

        XposedBridge.hookAllMethods(AbCenterExtensions, "b", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if ("exploreab_new".equals(param.args[1])) {
                    param.setResult(0);
                }
            }
        });
    }
}
