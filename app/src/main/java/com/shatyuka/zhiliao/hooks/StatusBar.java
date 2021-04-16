package com.shatyuka.zhiliao.hooks;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class StatusBar implements IHook {
    static Class<?> StatusBarUtil;

    @Override
    public String getName() {
        return "状态栏沉浸";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            StatusBarUtil = classLoader.loadClass("com.zhihu.android.base.util.x$b");
        } catch (ClassNotFoundException ignored) {
            StatusBarUtil = classLoader.loadClass("com.zhihu.android.base.util.y$b");
        }
    }

    @Override
    public void hook() throws Throwable {
        XposedHelpers.findAndHookConstructor(StatusBarUtil, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_statusbar", false))
                    param.args[0] = getStatusbarColor();
            }
        });
        XposedHelpers.findAndHookMethod(StatusBarUtil, "a", int.class, new XC_MethodHook() {
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
    }

    static int getStatusbarColor() {
        boolean darkMode = (Helper.context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (darkMode)
            return 0xFF121212;
        else
            return 0xFFFFFFFF;
    }
}
