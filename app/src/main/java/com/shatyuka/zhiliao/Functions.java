package com.shatyuka.zhiliao;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Functions {
    final static boolean DEBUG_WEBVIEW = false;

    static boolean init(final ClassLoader classLoader) {
        try {
            XposedBridge.hookMethod(Helper.isShowLaunchAd, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_launchad", true))
                        param.setResult(false);
                }
            });

            XposedBridge.hookAllMethods(Helper.InnerDeserializer, "deserialize", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true)) {
                        Object result = param.getResult();
                        if (result == null)
                            return;
                        Class<?> resultClass = result.getClass();
                        if (result != null) {
                            if (resultClass == Helper.ApiTemplateRoot) {
                                Object extra = Helper.ApiTemplateRoot.getField("extra").get(result);
                                String type = (String) Helper.DataUnique.getField("type").get(extra);
                                if (Helper.prefs.getBoolean("switch_video", false) && (type.equals("zvideo") || type.equals("drama"))) {
                                    param.setResult(null);
                                } else {
                                    if (Helper.regex_title == null && Helper.regex_author == null && Helper.regex_content == null)
                                        return;
                                    Object common_card = resultClass.getField("common_card").get(result);
                                    Object feed_content = common_card.getClass().getField("feed_content").get(common_card);
                                    if (feed_content == null)
                                        return;
                                    Object title = Helper.ApiFeedContent.getField("title").get(feed_content);
                                    Object content = Helper.ApiFeedContent.getField("content").get(feed_content);
                                    Object sourceLine = Helper.ApiFeedContent.getField("sourceLine").get(feed_content);
                                    List elements = (List) sourceLine.getClass().getField("elements").get(sourceLine);
                                    Object author = elements.get(1).getClass().getField("text").get(elements.get(1));
                                    String title_str = (String) Helper.panel_text.get(title);
                                    String author_str = (String) Helper.panel_text.get(author);
                                    String content_str = (String) Helper.panel_text.get(content);
                                    if ((Helper.regex_title != null && Helper.regex_title.matcher(title_str).find()) ||
                                            (Helper.regex_author != null && Helper.regex_author.matcher(author_str).find()) ||
                                            (Helper.regex_content != null && Helper.regex_content.matcher(content_str).find())) {
                                        param.setResult(null);
                                    }
                                }
                            } else if (resultClass == Helper.MarketCard) {
                                if (Helper.prefs.getBoolean("switch_marketcard", false)) {
                                    param.setResult(null);
                                }
                            }
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolve", Context.class, "com.zhihu.android.api.model.FeedAdvert", boolean.class, Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveAnswerAdParam", Context.class, "com.zhihu.android.api.model.AnswerListAd", Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveCommentAdParam", Context.class, "com.zhihu.android.api.model.CommentListAd", Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_commentad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.BaseAppView, "a", Helper.IZhihuWebView, WebResourceRequest.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    WebResourceRequest request = (WebResourceRequest) param.args[1];
                    List<String> segments = request.getUrl().getPathSegments();
                    if (segments.size() > 2 && request.getMethod().equals("GET")
                            && ((Helper.prefs.getBoolean("switch_answerad", true) && segments.get(segments.size() - 1).equals("recommendations"))
                            || (Helper.prefs.getBoolean("switch_club", false) && segments.get(segments.size() - 1).equals("bind_club"))
                            || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods")))) {
                        WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("null\n".getBytes()));
                        response.setStatusCodeAndReasonPhrase(200, "OK");
                        param.setResult(response);
                    }
                }
            });

            XposedBridge.hookMethod(Helper.showShareAd, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_sharead", true))
                        param.setResult(null);
                }
            });

            XposedBridge.hookAllConstructors(Helper.FeedsTabsTopEntranceManager, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_livebutton", false)) {
                        param.args[0] = new FrameLayout(((FrameLayout) param.args[0]).getContext());
                    }
                }
            });

            XposedHelpers.findAndHookMethod(java.io.File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file.getName().equals(".allowXposed")) {
                        param.setResult(true);
                    }
                }
            });

            if (DEBUG_WEBVIEW) {
                XposedBridge.hookAllConstructors(WebView.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedHelpers.callStaticMethod(WebView.class, "setWebContentsDebuggingEnabled", true);
                    }
                });
            }

            return true;
        } catch (NoSuchMethodError e) {
            XposedBridge.log("[Zhilaio] " + e.toString());
            return false;
        }
    }
}
