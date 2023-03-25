package com.shatyuka.zhiliao.hooks;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.shatyuka.zhiliao.Helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class AnswerAd implements IHook {
    static Method shouldInterceptRequest;

    @Override
    public String getName() {
        return "去回答底部广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Helper.findClass(classLoader, "com.zhihu.android.appview.a$",
                (Class<?> ZhihuWebViewClient) -> {
                    shouldInterceptRequest = ZhihuWebViewClient.getMethod("a", Helper.IZhihuWebView, WebResourceRequest.class);
                    return true;
                });
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
                        && ((Helper.prefs.getBoolean("switch_answerad", true) && (segments.get(segments.size() - 1).equals("recommendations")
                        || (segments.get(2).equals("brand") && segments.get(segments.size() - 1).equals("card")))
                        || segments.get(segments.size() - 2).equals("hotmodule"))
                        || (Helper.prefs.getBoolean("switch_club", false) && segments.get(segments.size() - 1).equals("bind_club"))
                        || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods"))
                        || (Helper.prefs.getBoolean("switch_article", false) && segments.get(segments.size() - 1).equals("recommendation"))
                        || (Helper.prefs.getBoolean("switch_related", false) && segments.get(segments.size() - 3).equals("related_queries")))
                        || (Helper.prefs.getBoolean("switch_searchwords", false) && segments.get(segments.size() - 1).equals("entity_word"))) {
                    WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("{}".getBytes()));
                    response.setStatusCodeAndReasonPhrase(200, "OK");
                    param.setResult(response);
                }
            }

            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false))
                    return;
                WebResourceRequest request = (WebResourceRequest) param.args[1];
                List<String> segments = request.getUrl().getPathSegments();
                if (segments.size() > 2 && request.getMethod().equals("GET")) {
                    if (Helper.prefs.getBoolean("switch_searchwords", false) && segments.get(0).equals("appview") && segments.get(segments.size() - 2).equals("answer")) {
                        WebResourceResponse response = (WebResourceResponse) param.getResult();
                        try {
                            byte[] data = new byte[response.getData().available()];
                            response.getData().read(data);
                            String content = new String(data);
                            content = content.replace("\"searchWords\"", "\"searchWords_bak\"");
                            content = content.replace("\"search_words\"", "\"search_words_bak\"");
                            response.setData(new ByteArrayInputStream(content.getBytes()));
                            param.setResult(response);
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        });
    }
}
