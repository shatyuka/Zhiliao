package com.shatyuka.zhiliao.hooks;

import android.view.Window;

import com.shatyuka.zhiliao.Helper;

import com.shatyuka.zhiliao.xposed.XC_MethodHook;
import com.shatyuka.zhiliao.xposed.XposedHelpers;

public class ColorMode implements IHook {
    @Override
    public String getName() {
        return "禁止切换色彩模式";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

    }

    @Override
    public void hook() throws Throwable {
        XposedHelpers.findAndHookMethod(Window.class, "setColorMode", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_colormode", false)) {
                    param.setResult(null);
                }
            }
        });
    }
}
