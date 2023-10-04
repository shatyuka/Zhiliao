package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
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
                throw new ClassNotFoundException("com.zhihu.android.bottomnav.BottomNav");
        } catch (Throwable e) {
            try {
                BottomNav = classLoader.loadClass("com.zhihu.android.bottomnav.b");
                if (BottomNav.getDeclaredField("a").getType() != boolean.class)
                    throw new ClassNotFoundException("com.zhihu.android.bottomnav.BottomNav");
            } catch (Throwable e2) {
                BottomNav = classLoader.loadClass("com.zhihu.android.bottomnav.e");
                if (BottomNav.getDeclaredField("a").getType() != boolean.class)
                    throw new ClassNotFoundException("com.zhihu.android.bottomnav.BottomNav");
            }
        }
        try {
            BottomNavBgViewExploreA = classLoader.loadClass("com.zhihu.android.bottomnav.core.explore.BottomNavBgViewExploreA");
            try {
                BottomNavBgViewExploreA_right = BottomNavBgViewExploreA.getDeclaredField("b");
                BottomNavBgViewExploreA_right.setAccessible(true);
                BottomNavBgViewExploreA_center = BottomNavBgViewExploreA.getDeclaredField("c");
                BottomNavBgViewExploreA_center.setAccessible(true);
            } catch (NoSuchFieldException e) {
                BottomNavBgViewExploreA_right = BottomNavBgViewExploreA.getDeclaredField("l");
                BottomNavBgViewExploreA_right.setAccessible(true);
                BottomNavBgViewExploreA_center = BottomNavBgViewExploreA.getDeclaredField("m");
                BottomNavBgViewExploreA_center.setAccessible(true);
            }
        } catch (Throwable ignored) {
        }
        FeedConfigManager = Helper.findClass(classLoader, "com.zhihu.android.", 0, 2,
                (Class<?> FeedConfigManager) -> {
                    Class<?>[] types = FeedConfigManager.getConstructors()[0].getParameterTypes();
                    return types.length == 6 && types[0] == String.class && types[1] == String.class && types[2] == long.class && types[3] == long.class && types[4] == types[5];
                });
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_navres", false)) {
            XposedHelpers.findAndHookMethod(BottomNav, "b", XC_MethodReplacement.returnConstant(null));
            if (FeedConfigManager != null) {
                XposedBridge.hookAllConstructors(FeedConfigManager, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[3] = 0;
                    }
                });
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
