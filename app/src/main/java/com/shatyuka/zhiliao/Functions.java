package com.shatyuka.zhiliao;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
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
        if (Helper.prefs.getBoolean("switch_feedad", true) && classname.equals("com.zhihu.android.api.model.FeedAdvert")) {
            return true;
        } else if (Helper.prefs.getBoolean("switch_marketcard", true) && classname.equals("com.zhihu.android.app.feed.ui.holder.marketcard.model.MarketCardModel")) {
            return true;
        } else if (Helper.prefs.getBoolean("switch_answerlistad", true) && classname.equals("com.zhihu.android.api.model.AnswerListAd")) {
            return true;
        }
        return false;
    }

    static boolean init(final ClassLoader classLoader) {
        try {
            PackageManager pm = Helper.context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo("com.zhihu.android", 0);
            String LaunchAdInterfaceName;

            switch (pi.versionCode) {
                case 2168: //6.51.0
                    LaunchAdInterfaceName = "com.zhihu.android.app.util.cx";
                    break;
                case 2186: //6.52.0
                case 2226: //6.52.1
                    LaunchAdInterfaceName = "com.zhihu.android.app.util.cw";
                    break;
                case 2244: //6.55.0
                    LaunchAdInterfaceName = "com.zhihu.android.app.util.ct";
                    break;
                default:
                    XposedBridge.log("[Zhiliao] Version not support: " + pi.versionName);
                    return false;
            }

            Class<?> LaunchAdInterface = XposedHelpers.findClass(LaunchAdInterfaceName, classLoader);
            XposedHelpers.findAndHookMethod(LaunchAdInterface, "isShowLaunchAd", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_launchad", true))
                        param.setResult(false);
                }
            });

            Class<?> BasePagingFragment = XposedHelpers.findClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment", classLoader);
            XposedBridge.hookAllMethods(BasePagingFragment, "postRefreshSucceed", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
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
            XposedHelpers.findAndHookMethod(BasePagingFragment, "addItemAfterClearAll", Object.class, new XC_MethodHook() {
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
            XposedHelpers.findAndHookMethod(BasePagingFragment, "insertDataItemToList", int.class, Object.class, new XC_MethodHook() {
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
            XposedHelpers.findAndHookMethod(BasePagingFragment, "insertDataRangeToList", int.class, List.class, new XC_MethodHook() {
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

            Class<?> BaseAppView = XposedHelpers.findClass("com.zhihu.android.appview.a.k", classLoader);
            XposedHelpers.findAndHookMethod(BaseAppView, "a", XposedHelpers.findClass("com.zhihu.android.app.mercury.a.i", classLoader), WebResourceRequest.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    WebResourceRequest request = (WebResourceRequest) param.args[1];
                    List<String> segments = request.getUrl().getPathSegments();
                    if (((Helper.prefs.getBoolean("switch_answerad", true) && segments.get(segments.size() - 1).equals("recommendations"))
                            || (Helper.prefs.getBoolean("switch_club", true) && segments.get(segments.size() - 1).equals("bind_club"))
                            || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("linkcard"))
                            || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods")))
                            && request.getMethod().equals("GET")) {
                        WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("null\n".getBytes()));
                        response.setStatusCodeAndReasonPhrase(200, "OK");
                        param.setResult(response);
                    }
                }
            });

            XposedHelpers.findAndHookMethod("com.zhihu.android.library.sharecore.fragment.ShareFragment", classLoader, "a", View.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_sharead", true))
                        param.setResult(null);
                }
            });

            XposedHelpers.findAndHookMethod(java.io.File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
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
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
