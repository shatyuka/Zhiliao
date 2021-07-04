package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class NavRes implements IHook {
    static Class<?> BottomNav;

    @Override
    public String getName() {
        return "禁用活动主题";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        BottomNav = classLoader.loadClass("com.zhihu.android.bottomnav.b");
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_navres", false)) {
            XposedHelpers.findAndHookMethod(BottomNav, "b", XC_MethodReplacement.returnConstant(null));
        }
    }
}
