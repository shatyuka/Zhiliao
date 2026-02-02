package com.shatyuka.zhiliao.hooks;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class StatusBar implements IHook {
    static Class<?> CombinedDrawable;
    static Class<?> StatusBarDrawable;
    static Class<?> ThemeChangedEvent;

    static Method setColor;

    static Field CombinedDrawable_statusBarDrawable;

    @Override
    public String getName() {
        return "状态栏沉浸";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Helper.findClass(classLoader, "com.zhihu.android.base.util.", 0, 2,
                (Class<?> StatusBarUtil) -> {
                    String className = StatusBarUtil.getName();
                    Class<?> combinedDrawable = classLoader.loadClass(className + "$a");
                    Class<?> statusBarDrawable = classLoader.loadClass(className + "$b");
                    if (!LayerDrawable.class.isAssignableFrom(combinedDrawable) || !Drawable.class.isAssignableFrom(statusBarDrawable))
                        return false;
                    CombinedDrawable = combinedDrawable;
                    StatusBarDrawable = statusBarDrawable;
                    return true;
                });
        if (CombinedDrawable == null)
            throw new ClassNotFoundException("com.zhihu.android.base.util.StatusBarUtil$CombinedDrawable");

        ThemeChangedEvent = classLoader.loadClass("com.zhihu.android.app.event.ThemeChangedEvent");

        setColor = StatusBarDrawable.getMethod("a", int.class);

        try {
            CombinedDrawable_statusBarDrawable = CombinedDrawable.getDeclaredField("b");
            CombinedDrawable_statusBarDrawable.setAccessible(true);
        } catch (NoSuchFieldException e) {
            CombinedDrawable_statusBarDrawable = CombinedDrawable.getDeclaredField("j");
            CombinedDrawable_statusBarDrawable.setAccessible(true);
        }
    }

    @Override
    public void hook() throws Throwable {
        XposedHelpers.findAndHookConstructor(StatusBarDrawable, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_statusbar", false))
                    param.args[0] = getStatusbarColor();
            }
        });
        XposedBridge.hookMethod(setColor, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_statusbar", false))
                    param.args[0] = getStatusbarColor();
            }
        });
        XposedHelpers.findAndHookMethod(Helper.AnswerPagerFragment, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_statusbar", false))
                    ((View) param.args[0]).setBackgroundColor(getStatusbarColor());
            }
        });

        XposedHelpers.findAndHookConstructor(ThemeChangedEvent, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_statusbar", false)) {
                    if (Helper.settingsView != null) {
                        Object background = ((View) Helper.settingsView).getBackground();
                        if (CombinedDrawable.isInstance(background)) {
                            Object statusBarDrawable = CombinedDrawable_statusBarDrawable.get(background);
                            setColor.invoke(statusBarDrawable, 0);
                        }
                    }
                }
            }
        });
    }

    static int getStatusbarColor() {
        if (Helper.getDarkMode())
            return 0xFF121212;
        else
            return 0xFFFFFFFF;
    }
}
