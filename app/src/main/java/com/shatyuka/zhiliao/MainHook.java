package com.shatyuka.zhiliao;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.crossbowffs.remotepreferences.RemotePreferences;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    final String hookPackage = "com.zhihu.android";
    private static String MODULE_PATH;
    private Context context;
    private SharedPreferences prefs;

    private int id_setting;
    private XModuleResources modRes;

    final boolean DEBUG_LOG_CARD_CLASS = false;
    final boolean DEBUG_WEBVIEW = false;
    final boolean DEBUG_ZHIHU = true;

    private boolean shouldBlock(String classname) {
        if (prefs.getBoolean("switch_feedad", true) && classname.equals("com.zhihu.android.api.model.FeedAdvert")) {
            return true;
        } else if (prefs.getBoolean("switch_marketcard", true) && classname.equals("com.zhihu.android.app.feed.ui.holder.marketcard.model.MarketCardModel")) {
            return true;
        } else if (prefs.getBoolean("switch_answerlistad", true) && classname.equals("com.zhihu.android.api.model.AnswerListAd")) {
            return true;
        }
        return false;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (hookPackage.equals(lpparam.packageName)) {
            XposedBridge.log("[Zhiliao] Inject into Zhihu.");

            XposedHelpers.findAndHookMethod(java.io.File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    File file = (File) param.thisObject;
                    if (file.getName().equals(".allowXposed")) {
                        param.setResult(true);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(android.app.Instrumentation.class, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] instanceof Application) {
                        context = ((Application) param.args[0]).getApplicationContext();
                        prefs = new RemotePreferences(context, "com.shatyuka.zhiliao.preferences", "com.shatyuka.zhiliao_preferences");

                        PackageManager pm = context.getPackageManager();
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
                                return;
                        }
                        Class<?> LaunchAdInterface = XposedHelpers.findClass(LaunchAdInterfaceName, lpparam.classLoader);
                        XposedHelpers.findAndHookMethod(LaunchAdInterface, "isShowLaunchAd", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (prefs.getBoolean("switch_mainswitch", true) && prefs.getBoolean("switch_launchad", true))
                                    param.setResult(false);
                            }
                        });
                    }
                }
            });

            Class<?> BasePagingFragment = XposedHelpers.findClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment", lpparam.classLoader);
            XposedBridge.hookAllMethods(BasePagingFragment, "postRefreshSucceed", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!prefs.getBoolean("switch_mainswitch", true))
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
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!prefs.getBoolean("switch_mainswitch", true))
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
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!prefs.getBoolean("switch_mainswitch", true))
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
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!prefs.getBoolean("switch_mainswitch", true))
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

            Class<?> BaseAppView = XposedHelpers.findClass("com.zhihu.android.appview.a.k", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(BaseAppView, "a", XposedHelpers.findClass("com.zhihu.android.app.mercury.a.i", lpparam.classLoader), WebResourceRequest.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!prefs.getBoolean("switch_mainswitch", true))
                        return;
                    WebResourceRequest request = (WebResourceRequest) param.args[1];
                    List<String> segments = request.getUrl().getPathSegments();
                    if (((prefs.getBoolean("switch_answerad", true) && segments.get(segments.size() - 1).equals("recommendations"))
                            || (prefs.getBoolean("switch_club", true) && segments.get(segments.size() - 1).equals("bind_club"))
                            || (prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("linkcard"))
                            || (prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods")))
                            && request.getMethod().equals("GET")) {
                        WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("null\n".getBytes()));
                        response.setStatusCodeAndReasonPhrase(200, "OK");
                        param.setResult(response);
                    }
                }
            });

            XposedHelpers.findAndHookMethod("com.zhihu.android.library.sharecore.fragment.ShareFragment", lpparam.classLoader, "a", View.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (prefs.getBoolean("switch_mainswitch", true) && prefs.getBoolean("switch_sharead", true))
                        param.setResult(null);
                }
            });

            XposedHelpers.findAndHookMethod("androidx.preference.i", lpparam.classLoader, "a", int.class, "androidx.preference.PreferenceGroup", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if ((int) param.args[0] == id_setting) {
                        try (XmlResourceParser parser = modRes.getXml(R.xml.settings)) {
                            Class<?> XmlPullParser = XposedHelpers.findClass("org.xmlpull.v1.XmlPullParser", lpparam.classLoader);
                            Class<?> PreferenceGroup = XposedHelpers.findClass("androidx.preference.PreferenceGroup", lpparam.classLoader);
                            Method inflate = param.thisObject.getClass().getMethod("a", XmlPullParser, PreferenceGroup);
                            param.setResult(inflate.invoke(param.thisObject, parser, param.args[1]));
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod("com.zhihu.android.app.ui.fragment.preference.SettingsFragment", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    Class<?> thisClass = thisObject.getClass();

                    Class<?> preferenceClass = XposedHelpers.findClass("androidx.preference.Preference", lpparam.classLoader);
                    Class<?> OnPreferenceClickListenerClass = XposedHelpers.findClass("androidx.preference.Preference.d", lpparam.classLoader);
                    Class<?> preferenceScreenClass = XposedHelpers.findClass("androidx.preference.PreferenceScreen", lpparam.classLoader);

                    Method findPreference = thisClass.getMethod("a", CharSequence.class);
                    Method setOnPreferenceClickListener = preferenceClass.getMethod("a", OnPreferenceClickListenerClass);
                    Method addPreference = preferenceScreenClass.getMethod("c", preferenceClass);

                    Object preference_zhiliao = findPreference.invoke(thisObject, "preference_id_zhiliao");
                    Object callback = Proxy.newProxyInstance(lpparam.classLoader, new Class[]{OnPreferenceClickListenerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("onPreferenceClick")) {
                                android.widget.Toast.makeText(context, "哼唧", android.widget.Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            return null;
                        }
                    });
                    setOnPreferenceClickListener.invoke(preference_zhiliao, callback);
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
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (hookPackage.equals(resparam.packageName)) {
            modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
            for (char i = 'a'; i <= 'z'; i++) {
                int id = resparam.res.getIdentifier(String.valueOf(i), "xml", hookPackage);
                InputStream inputStream = resparam.res.openRawResource(id);
                if (inputStream.available() > 4000 && inputStream.available() < 5000) {
                    id_setting = id;
                    break;
                }
                inputStream.close();
            }
        }
    }
}
