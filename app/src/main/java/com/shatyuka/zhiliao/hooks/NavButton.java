package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

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

        Class<?> tabLayoutTabClass = classLoader.loadClass("com.google.android.material.tabs.TabLayout$Tab");
        IMenuItem = Arrays.stream(BottomNavMenuView.getDeclaredMethods())
                .filter(method -> method.getReturnType() == tabLayoutTabClass)
                .map(method -> method.getParameterTypes()[0]).findFirst().get();

        getItemId = Arrays.stream(IMenuItem.getDeclaredMethods())
                .filter(method -> method.getReturnType() == String.class).findFirst().get();

        Tab_tabView = tabLayoutTabClass.getField("view");
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
                            ("panel".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_panelnav", false)) ||
                            ("find".equals(getItemId.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_findnav", false))) {
                        ((View) Tab_tabView.get(param.getResult())).setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
