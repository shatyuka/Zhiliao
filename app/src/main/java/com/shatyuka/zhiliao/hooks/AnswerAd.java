package com.shatyuka.zhiliao.hooks;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.shatyuka.zhiliao.Helper;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class AnswerAd implements IHook {
    static Class<?> IZhihuWebView;

    static Method shouldInterceptRequest;

    @Override
    public String getName() {
        return "去回答底部广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        IZhihuWebView = classLoader.loadClass("com.zhihu.android.app.search.ui.widget.SearchResultLayout").getDeclaredField("c").getType();

        for (char i = 'a'; i <= 'z'; i++) {
            Class<?> ZhihuWebViewClient = XposedHelpers.findClassIfExists("com.zhihu.android.appview.a$" + i, classLoader);
            if (ZhihuWebViewClient != null) {
                try {
                    shouldInterceptRequest = ZhihuWebViewClient.getMethod("a", IZhihuWebView, WebResourceRequest.class);
                } catch (NoSuchMethodException e) {
                    continue;
                }
                break;
            }
        }
        if (shouldInterceptRequest == null)
            throw new NoSuchMethodException("com.zhihu.android.appview.AppView$ZhihuWebViewClient.shouldInterceptRequest(IZhihuWebView, WebResourceRequest)");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(shouldInterceptRequest, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false))
                    return;
                WebResourceRequest request = (WebResourceRequest) param.args[1];
                List<String> segments = request.getUrl().getPathSegments();
                if (segments.size() > 2 && request.getMethod().equals("GET")
                        && ((Helper.prefs.getBoolean("switch_answerad", true) && (segments.get(segments.size() - 1).equals("recommendations") || (segments.get(2).equals("brand") && segments.get(segments.size() - 1).equals("card"))))
                        || (Helper.prefs.getBoolean("switch_club", false) && segments.get(segments.size() - 1).equals("bind_club"))
                        || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods"))
                        || (Helper.prefs.getBoolean("switch_article", false) && segments.get(segments.size() - 1).equals("recommendation")))) {
                    WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("null\n".getBytes()));
                    response.setStatusCodeAndReasonPhrase(200, "OK");
                    param.setResult(response);
                }
            }
        });
    }
}
