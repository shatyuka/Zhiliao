package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

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

        List<String> classNameList = Arrays.asList(
                "com.zhihu.android.bottomnav.core.a.b",
                // 9.36.0 b.b#a
                "com.zhihu.android.bottomnav.core.b.b",
                "com.zhihu.android.bottomnav.core.t.g",
                "com.zhihu.android.bottomnav.core.w.d",
                // 9.37.0 r.g#getItemId
                "com.zhihu.android.bottomnav.core.r.g");

        for (String className : classNameList) {
            try {
                IMenuItem = classLoader.loadClass(className);
                try {
                    getItemId = IMenuItem.getMethod("getItemId");
                } catch (NoSuchMethodException ignore) {
                    getItemId = IMenuItem.getMethod("a");
                }
                break;
            } catch (ClassNotFoundException ignore) {
            }
        }

        Tab_tabView = classLoader.loadClass("com.google.android.material.tabs.TabLayout$Tab").getField("view");
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && (Helper.prefs.getBoolean("switch_vipnav", false) || Helper.prefs.getBoolean("switch_videonav", false) || Helper.prefs.getBoolean("switch_friendnav", false) || Helper.prefs.getBoolean("switch_panelnav", false))) {
            XposedBridge.hookMethod(Helper.getMethodByParameterTypes(BottomNavMenuView, IMenuItem), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (("market".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_vipnav", false)) ||
                            ("video".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_videonav", false)) ||
                            ("friend".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_friendnav", false)) ||
                            ("panel".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_panelnav", false))) {
                        ((View) Tab_tabView.get(param.getResult())).setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
