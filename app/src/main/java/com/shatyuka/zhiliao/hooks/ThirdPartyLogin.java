package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class ThirdPartyLogin implements IHook {
    static Class<?> UiConfigImpl;

    static boolean obfuse = false;

    @Override
    public String getName() {
        return "第三方登录";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            UiConfigImpl = classLoader.loadClass("com.zhihu.android.account.provider.UiConfigImpl");
        } catch (ClassNotFoundException e) {
            UiConfigImpl = classLoader.loadClass("com.zhihu.android.app.f$6");
            obfuse = true;
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_thirdpartylogin", false)) {
            if (obfuse) {
                XposedHelpers.findAndHookMethod(UiConfigImpl, "b", XC_MethodReplacement.returnConstant(true));
                XposedHelpers.findAndHookMethod(UiConfigImpl, "c", XC_MethodReplacement.returnConstant(true));
                XposedHelpers.findAndHookMethod(UiConfigImpl, "d", XC_MethodReplacement.returnConstant(true));
            } else {
                XposedHelpers.findAndHookMethod(UiConfigImpl, "showQQ", XC_MethodReplacement.returnConstant(true));
                XposedHelpers.findAndHookMethod(UiConfigImpl, "showSina", XC_MethodReplacement.returnConstant(true));
                XposedHelpers.findAndHookMethod(UiConfigImpl, "showWeChat", XC_MethodReplacement.returnConstant(true));
            }
        }
    }
}
