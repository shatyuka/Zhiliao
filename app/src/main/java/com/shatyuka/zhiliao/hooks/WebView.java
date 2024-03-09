package com.shatyuka.zhiliao.hooks;

import android.graphics.Bitmap;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class WebView implements IHook {
    static final String script_hide_water_mark = "var styleWaterMark=document.createElement('style');styleWaterMark.innerHTML='.App{background-image:none!important}';document.body.append(styleWaterMark);";

    /**
     * todo: 抽成文件
     */
    static final String script_hide_subscribe = "let subscribe = document.getElementsByClassName('Toolbar-functionButtons')[0].lastChild;\n" +
            "if (subscribe) {\n" +
            "    for (let i = 0; i < subscribe.children.length; i++) {\n" +
            "        if (subscribe.children[i].classList.contains('Avatar')) {\n" +
            "            let styleSubscribe = document.createElement('style');\n" +
            "            styleSubscribe.innerHTML = '.' + subscribe.className + '{display:none!important}';\n" +
            "            document.body.append(styleSubscribe);\n" +
            "            break\n" +
            "        }\n" +
            "    };\n" +
            "}\n" +
            "let subscribe2 = document.getElementsByClassName('UserLine AuthorCard')[0].lastChild\n" +
            "if (subscribe2) {\n" +
            "    if (subscribe2.tagName === 'BUTTON') {\n" +
            "        let styleSubscribe = document.createElement('style');\n" +
            "        styleSubscribe.innerHTML = '.' + subscribe2.className + '{display:none!important}';\n" +
            "        document.body.append(styleSubscribe);\n" +
            "    }\n" +
            "}";

    static Class<?> answerAppView;

    @Override
    public String getName() {
        return "WebView修改";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        answerAppView = classLoader.loadClass("com.zhihu.android.answer.module.content.appview.AnswerAppView");
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_subscribe", false)) {
            // 禁用, 使用pre render时, js会失效
            XposedBridge.hookAllMethods(answerAppView, "canPreRender", XC_MethodReplacement.returnConstant(false));
        }

        XposedBridge.hookAllConstructors(android.webkit.WebView.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_webview_debug", false)) {
                    XposedHelpers.callStaticMethod(android.webkit.WebView.class, "setWebContentsDebuggingEnabled", true);
                }
            }
        });

        XposedHelpers.findAndHookMethod(Helper.WebViewClientWrapper, "onPageStarted", android.webkit.WebView.class, String.class, Bitmap.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                android.webkit.WebView webview = (android.webkit.WebView) param.args[0];
                boolean hideWaterMark = Helper.prefs.getBoolean("switch_watermark", false);
                boolean hideSubscribe = Helper.prefs.getBoolean("switch_subscribe", false);
                webview.evaluateJavascript(getScript(hideWaterMark, hideSubscribe), null);
            }
        });

        XposedHelpers.findAndHookMethod(Helper.WebViewClientWrapper, "onPageFinished", android.webkit.WebView.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                android.webkit.WebView webview = (android.webkit.WebView) param.args[0];
                String js = Helper.prefs.getString("edit_js", null);
                if (js != null) {
                    webview.evaluateJavascript(js, null);
                }
            }
        });
    }

    static String getScript(boolean hideWaterMark, boolean hideSubscribe) {
        StringBuilder sb = new StringBuilder();
        sb.append("document.addEventListener('DOMContentLoaded',(event)=>{");
        if (hideWaterMark) {
            sb.append(script_hide_water_mark);
        }
        if (hideSubscribe) {
            sb.append(script_hide_subscribe);
        }
        sb.append("});");
        return sb.toString();
    }
}
