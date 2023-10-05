package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class NavButton implements IHook {
    static Class<?> BottomNavMenuView;
    static Class<?> IMenuItem;

    static Method getItemId;

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
            try {
                IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.b.b");
            } catch (ClassNotFoundException e2) {
                try {
                    IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.t.g");
                } catch (ClassNotFoundException e3) {
                    IMenuItem = classLoader.loadClass("com.zhihu.android.bottomnav.core.w.d");
                }
            }
        }

        try {
            getItemId = IMenuItem.getMethod("getItemId");
        } catch (NoSuchMethodException e) {
            getItemId = IMenuItem.getMethod("a");
        }

        Tab_tabView = classLoader.loadClass("com.google.android.material.tabs.TabLayout$Tab").getField("view");
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && (Helper.prefs.getBoolean("switch_vipnav", false) || Helper.prefs.getBoolean("switch_videonav", false)|| Helper.prefs.getBoolean("switch_friendnav", false) || Helper.prefs.getBoolean("switch_panelnav", false))) {
            XposedBridge.hookMethod(Helper.getMethodByParameterTypes(BottomNavMenuView, IMenuItem), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (("market".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_vipnav", false)) ||
                            ("video".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_videonav", false)) ||
                            ("friend".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_friendnav", false)) ||
                            ("panel".equals(getItemId.invoke(param.args[0]))&& Helper.prefs.getBoolean("switch_panelnav", false))){
                        ((View) Tab_tabView.get(param.getResult())).setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
