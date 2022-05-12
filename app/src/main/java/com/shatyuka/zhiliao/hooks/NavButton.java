package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class NavButton implements IHook {
    static Class<?> BottomNavMenuView;
    static Class<?> IMenuItem;

    static Method getMenuName;

    static Field Tab_tabView;

    @Override
    public String getName() {
        return "隐藏导航栏按钮";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        BottomNavMenuView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuView");
        try {
            IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.a.b");
        } catch (ClassNotFoundException e) {
            IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.b.b");
        }

        getMenuName = IMenuItem.getMethod("a");

        Tab_tabView = classLoader.loadClass("com.google.android.material.tabs.TabLayout$Tab").getField("view");
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && (Helper.prefs.getBoolean("switch_vipnav", false) || Helper.prefs.getBoolean("switch_videonav", false)|| Helper.prefs.getBoolean("switch_friendnav", false) || Helper.prefs.getBoolean("switch_panelnav", false))) {
            XposedHelpers.findAndHookMethod(BottomNavMenuView, "a", IMenuItem, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (("market".equals(getMenuName.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_vipnav", false)) ||
                            ("video".equals(getMenuName.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_videonav", false)) ||
                            ("friend".equals(getMenuName.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_friendnav", false)) ||
                            ("panel".equals(getMenuName.invoke(param.args[0]))&& Helper.prefs.getBoolean("switch_panelnav", false))){
                        ((View) Tab_tabView.get(param.getResult())).setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
