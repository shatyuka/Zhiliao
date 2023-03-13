package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class NavRes implements IHook {
    static Class<?> BottomNav;
    static Class<?> BottomNavBgViewExploreA;
    static Class<?> FeedConfigManager;

    static Field BottomNavBgViewExploreA_right;
    static Field BottomNavBgViewExploreA_center;

    @Override
    public String getName() {
        return "导航栏样式";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            BottomNav = classLoader.loadClass("com.zhihu.android.bottomnav.a");
            if (BottomNav.getDeclaredField("a").getType() != boolean.class)
                throw new Throwable("");
        } catch (Throwable ignored) {
            BottomNav = classLoader.loadClass("com.zhihu.android.bottomnav.b");
            if (BottomNav.getDeclaredField("a").getType() != boolean.class)
                throw new ClassNotFoundException("com.zhihu.android.bottomnav.BottomNav");
        }
        try {
            BottomNavBgViewExploreA = classLoader.loadClass("com.zhihu.android.bottomnav.core.explore.BottomNavBgViewExploreA");
            BottomNavBgViewExploreA_right = BottomNavBgViewExploreA.getDeclaredField("b");
            BottomNavBgViewExploreA_right.setAccessible(true);
            BottomNavBgViewExploreA_center = BottomNavBgViewExploreA.getDeclaredField("c");
            BottomNavBgViewExploreA_center.setAccessible(true);
        } catch (Throwable ignored) {
        }
        FeedConfigManager = Helper.findClass(classLoader, "com.zhihu.android.", (Class<?> FeedConfigManager) -> FeedConfigManager.getMethod("a").getReturnType().getDeclaredField("a").getType().equals(String.class));
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_navres", false)) {
            XposedHelpers.findAndHookMethod(BottomNav, "b", XC_MethodReplacement.returnConstant(null));
            if (FeedConfigManager != null) {
                XposedHelpers.findAndHookMethod(FeedConfigManager, "a", XC_MethodReplacement.returnConstant(null));
            }
        }

        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_nipple", false)) {
            if (BottomNavBgViewExploreA != null) {
                XposedHelpers.findAndHookMethod(BottomNavBgViewExploreA, "setBackground", boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View right = (View) BottomNavBgViewExploreA_right.get(param.thisObject);
                        View center = (View) BottomNavBgViewExploreA_center.get(param.thisObject);
                        if (right != null && center != null) {
                            center.setBackground(right.getBackground());
                        }
                    }
                });
            }
        }
    }
}
