package com.shatyuka.zhiliao.hooks;

import android.webkit.ValueCallback;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class WebView implements IHook {
    static Method onReceivedTitle;
    static Method evaluateJavascript;

    static final String script_water_mark_script = "setTimeout(function(){let styleEle=document.createElement(\"style\");styleEle.innerHTML=\".App{background-image:none!important}\";document.body.append(styleEle);},500);";

    @Override
    public String getName() {
        return "WebView修改";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Helper.findClass(classLoader, "com.zhihu.android.app.mercury.", 0, 2,
                (Class<?> ZhihuWebChromeClientExt) -> {
                    onReceivedTitle = ZhihuWebChromeClientExt.getMethod("onReceivedTitle", Helper.IZhihuWebView, String.class);
                    return true;
                });
        if (onReceivedTitle == null)
            throw new NoSuchMethodException("com.zhihu.android.app.mercury.ZhihuWebChromeClientExt.onReceivedTitle(IZhihuWebView, String)");

        evaluateJavascript = Helper.IZhihuWebView.getMethod("a", String.class, ValueCallback.class);
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllConstructors(android.webkit.WebView.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_webview_debug", false)) {
                    XposedHelpers.callStaticMethod(android.webkit.WebView.class, "setWebContentsDebuggingEnabled", true);
                }
            }
        });

        XposedBridge.hookMethod(onReceivedTitle, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_watermark", false)) {
                    evaluateJavascript.invoke(param.args[0], script_water_mark_script, null);
                }
            }
        });

        XposedHelpers.findAndHookMethod(Helper.WebViewClientWrapper, "onPageFinished", android.webkit.WebView.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_watermark", false)) {
                    // double insurance :D
                    ((android.webkit.WebView) param.args[0]).evaluateJavascript(script_water_mark_script, null);
                }

                String js = Helper.prefs.getString("edit_js", null);
                if (js != null) {
                    ((android.webkit.WebView) param.args[0]).evaluateJavascript(js, null);
                }
            }
        });
    }
}
