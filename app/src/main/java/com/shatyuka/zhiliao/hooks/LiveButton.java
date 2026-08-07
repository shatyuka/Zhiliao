package com.shatyuka.zhiliao.hooks;

import android.widget.FrameLayout;

import com.shatyuka.zhiliao.Helper;

import com.shatyuka.zhiliao.xposed.XC_MethodHook;
import com.shatyuka.zhiliao.xposed.XposedBridge;

public class LiveButton implements IHook {
    static Class<?> FeedsTabsTopEntranceManager;

    @Override
    public String getName() {
        return "移除直播按钮";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            FeedsTabsTopEntranceManager = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment").getDeclaredField("mEntranceManger").getType();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (FeedsTabsTopEntranceManager != null) {
            XposedBridge.hookAllConstructors(FeedsTabsTopEntranceManager, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_livebutton", false)) {
                        param.args[0] = new FrameLayout(((FrameLayout) param.args[0]).getContext());
                    }
                }
            });
        }
    }
}
