package com.shatyuka.zhiliao.hooks;

import android.content.Context;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class StartPage implements IHook {
    static Method getDefaultTab;

    @Override
    public String getName() {
        return "自定义起始页面";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            Class<?> FeedsTabsFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment");
            Class<?> FeedTabViewModel = FeedsTabsFragment.getField("mTabViewModel").getType();
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
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(getDefaultTab, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(Integer.parseInt(Helper.prefs.getString("list_startpage", "1")));
            }
        });
    }
}
