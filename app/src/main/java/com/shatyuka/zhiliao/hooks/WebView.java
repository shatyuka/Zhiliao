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

    static int[] timeout_array = {100, 500, 1000};
    static final String script_hide_water_mark = getScript("zhiliao_hide_water_mark", "let styleEle=document.createElement(\"style\");styleEle.innerHTML=\".App{background-image:none!important}\";document.body.append(styleEle);");
    static final String script_hide_subscribe = getScript("zhiliao_hide_subscribe", "var subscribe=document.getElementsByClassName(\"Toolbar-functionButtons\")[0].lastChild;if(subscribe.firstChild.classList.contains(\"Avatar\")){subscribe.style.display=\"none\"};");

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
                    evaluateJavascript.invoke(param.args[0], script_hide_water_mark, null);
                }
                if (Helper.prefs.getBoolean("switch_subscribe", false)) {
                    evaluateJavascript.invoke(param.args[0], script_hide_subscribe, null);
                }
            }
        });

        XposedHelpers.findAndHookMethod(Helper.WebViewClientWrapper, "onPageFinished", android.webkit.WebView.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                android.webkit.WebView webview = (android.webkit.WebView) param.args[0];
                // also works after page reload
                if (Helper.prefs.getBoolean("switch_watermark", false)) {
                    webview.evaluateJavascript(script_hide_water_mark, null);
                }
                if (Helper.prefs.getBoolean("switch_subscribe", false)) {
                    webview.evaluateJavascript(script_hide_subscribe, null);
                }

                String js = Helper.prefs.getString("edit_js", null);
                if (js != null) {
                    webview.evaluateJavascript(js, null);
                }
            }
        });
    }

    static String getScript(String name, String script) {
        StringBuilder sb = new StringBuilder();
        sb.append("function ");
        sb.append(name);
        sb.append("(){");
        sb.append(script);
        sb.append("}");
        for (int timeout : timeout_array) {
            sb.append("setTimeout(");
            sb.append(name);
            sb.append(",");
            sb.append(timeout);
            sb.append(");");
        }
        return sb.toString();
    }
}
