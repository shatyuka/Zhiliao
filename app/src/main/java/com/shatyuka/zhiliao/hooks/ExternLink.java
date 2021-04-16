package com.shatyuka.zhiliao.hooks;

import android.content.Intent;
import android.net.Uri;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;
import java.net.URLDecoder;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class ExternLink implements IHook {
    static Class<?> LinkZhihuHelper;

    static Method isLinkZhihu;
    static Method isLinkZhihuWrap;

    @Override
    public String getName() {
        return "直接打开外部链接";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        LinkZhihuHelper = classLoader.loadClass("com.zhihu.android.app.mercury.k");

        try {
            isLinkZhihu = LinkZhihuHelper.getMethod("b", Uri.class);
        } catch (NoSuchMethodException e) {
            LinkZhihuHelper = classLoader.loadClass("com.zhihu.android.app.mercury.j");
            isLinkZhihu = LinkZhihuHelper.getMethod("b", Uri.class);
        }

        Method[] methods = LinkZhihuHelper.getMethods();
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (method.getName().equals("a") && method.getReturnType() == boolean.class &&
                    types.length == 3 && types[2] == String.class && types[1] != Uri.class) {
                isLinkZhihuWrap = method;
                break;
            }
        }
        if (isLinkZhihuWrap == null)
            throw new NoSuchMethodException("isLinkZhihuWrap");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(isLinkZhihuWrap, new XC_MethodHook() {
            XC_MethodHook.Unhook hook_isLinkZhihu;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && (Helper.prefs.getBoolean("switch_externlink", false) || Helper.prefs.getBoolean("switch_externlinkex", false))) {
                    String url = (String) param.args[2];
                    if (url.startsWith("https://link.zhihu.com/?target=")) {
                        param.args[2] = URLDecoder.decode(url.substring(31), "utf-8");
                        if (Helper.prefs.getBoolean("switch_externlinkex", false)) {
                            android.util.Log.d("Zhiliao", (String) param.args[2]);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) param.args[2]));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Helper.context.startActivity(intent);
                            param.setResult(true);
                        } else {
                            hook_isLinkZhihu = XposedBridge.hookMethod(isLinkZhihu, XC_MethodReplacement.returnConstant(true));
                        }
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (hook_isLinkZhihu != null)
                    hook_isLinkZhihu.unhook();
            }
        });
    }
}
