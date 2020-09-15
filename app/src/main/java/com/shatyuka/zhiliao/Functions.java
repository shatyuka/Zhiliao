package com.shatyuka.zhiliao;

import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Functions {
    final static boolean DEBUG_LOG_CARD_CLASS = false;
    final static boolean DEBUG_WEBVIEW = false;

    private static boolean shouldBlock(String classname) {
        if (Helper.prefs.getBoolean("switch_marketcard", true) && classname.equals("com.zhihu.android.app.feed.ui.holder.marketcard.model.MarketCardModel")) {
            return true;
        }
        return false;
    }

    static boolean init(final ClassLoader classLoader) {
        try {
            XposedBridge.hookMethod(Helper.isShowLaunchAd, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_launchad", true))
                        param.setResult(false);
                }
            });

            XposedBridge.hookAllMethods(Helper.BasePagingFragment, "postRefreshSucceed", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    if (param.args[0] == null)
                        return;
                    List<?> list = (List<?>) XposedHelpers.getObjectField(param.args[0], "data");
                    if (list == null || list.isEmpty())
                        return;
                    if (DEBUG_LOG_CARD_CLASS)
                        Log.d("Zhiliao", "postRefreshSucceed");
                    for (int i = list.size() - 1; i >= 0; i--) {
                        String classname = list.get(i).getClass().getName();
                        if (DEBUG_LOG_CARD_CLASS)
                            Log.d("Zhiliao", classname);
                        if (shouldBlock(classname)) {
                            list.remove(i);
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.BasePagingFragment, "addItemAfterClearAll", Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    if (param.args[0] == null)
                        return;
                    String classname = param.args[0].getClass().getName();
                    if (DEBUG_LOG_CARD_CLASS) {
                        Log.d("Zhiliao", "addItemAfterClearAll");
                        Log.d("Zhiliao", classname);
                    }
                    if (shouldBlock(classname)) {
                        param.setResult(null);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.BasePagingFragment, "insertDataItemToList", int.class, Object.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    if (param.args[1] == null)
                        return;
                    String classname = param.args[1].getClass().getName();
                    if (DEBUG_LOG_CARD_CLASS) {
                        Log.d("Zhiliao", "insertDataItemToList");
                        Log.d("Zhiliao", classname);
                    }
                    if (shouldBlock(classname)) {
                        param.setResult(null);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.BasePagingFragment, "insertDataRangeToList", int.class, List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    if (param.args[1] == null)
                        return;
                    List<?> list = (List<?>) param.args[1];
                    if (DEBUG_LOG_CARD_CLASS)
                        Log.d("Zhiliao", "insertDataRangeToList");
                    for (int i = list.size() - 1; i >= 0; i--) {
                        String classname = list.get(i).getClass().getName();
                        if (DEBUG_LOG_CARD_CLASS)
                            Log.d("Zhiliao", classname);
                        if (shouldBlock(classname)) {
                            list.remove(i);
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
                            || (Helper.prefs.getBoolean("switch_club", true) && segments.get(segments.size() - 1).equals("bind_club"))
                            || (Helper.prefs.getBoolean("switch_goods", true) && segments.get(segments.size() - 2).equals("goods")))) {
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
