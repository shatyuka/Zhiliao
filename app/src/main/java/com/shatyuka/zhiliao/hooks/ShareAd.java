package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class ShareAd implements IHook {
    static Method showShareAd;

    @Override
    public String getName() {
        return "去分享广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Class<?> ShareFragment = classLoader.loadClass("com.zhihu.android.library.sharecore.fragment.ShareFragment");
        if (ShareFragment != null) {
            Method[] methods = ShareFragment.getDeclaredMethods();
            for (Method method : methods) {
                Class<?>[] types = method.getParameterTypes();
                if (types.length == 1 && types[0] == View.class) {
                    showShareAd = method;
                    break;
                }
            }
        }
        if (showShareAd == null)
            throw new NoSuchMethodException("com.zhihu.android.library.sharecore.fragment.ShareFragment.showShareAd()");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(showShareAd, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_sharead", true))
                    param.setResult(null);
            }
        });
    }
}
