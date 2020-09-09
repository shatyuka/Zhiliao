package com.shatyuka.zhiliao;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    final String hookPackage = "com.zhihu.android";
    private Context context;
    private SharedPreferences prefs;

    private Resources modRes;
    private Object preference_zhiliao;

    final boolean DEBUG_LOG_CARD_CLASS = false;
    final boolean DEBUG_WEBVIEW = false;

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
                        modRes = context.getPackageManager().getResourcesForApplication("com.shatyuka.zhiliao");
                        prefs = context.getSharedPreferences("zhiliao_preferences", Context.MODE_PRIVATE);

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
                    XmlResourceParser parser;
                    int id = (int) param.args[0];
                    InputStream inputStream = context.getResources().openRawResource(id);
                    if (inputStream.available() > 4000 && inputStream.available() < 5000)
                        parser = modRes.getXml(R.xml.settings);
                    else if (inputStream.available() > 5000)
                        parser = modRes.getXml(R.xml.preferences_zhihu);
                    else
                        return;
                    try {
                        Class<?> XmlPullParser = XposedHelpers.findClass("org.xmlpull.v1.XmlPullParser", lpparam.classLoader);
                        Class<?> PreferenceGroup = XposedHelpers.findClass("androidx.preference.PreferenceGroup", lpparam.classLoader);
                        Method inflate = param.thisObject.getClass().getMethod("a", XmlPullParser, PreferenceGroup);
                        param.setResult(inflate.invoke(param.thisObject, parser, param.args[1]));
                    } finally {
                        parser.close();
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

                    Method findPreference = thisClass.getMethod("a", CharSequence.class);
                    Method setSummary = preferenceClass.getMethod("a", CharSequence.class);
                    Method setOnPreferenceClickListener = preferenceClass.getMethod("a", OnPreferenceClickListenerClass);

                    preference_zhiliao = findPreference.invoke(thisObject, "preference_id_zhiliao");
                    setSummary.invoke(preference_zhiliao, "当前版本 " + modRes.getString(R.string.app_version));
                    setOnPreferenceClickListener.invoke(preference_zhiliao, thisObject);
                }
            });

            final Class<?> DebugFragment = XposedHelpers.findClass("com.zhihu.android.app.ui.fragment.DebugFragment", lpparam.classLoader);
            XposedHelpers.findAndHookMethod("com.zhihu.android.app.ui.fragment.preference.SettingsFragment", lpparam.classLoader, "onPreferenceClick", "androidx.preference.Preference", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] == preference_zhiliao) {
                        Object thisObject = param.thisObject;
                        Class<?> intentClass = XposedHelpers.findClass("com.zhihu.android.app.util.gl", lpparam.classLoader);
                        Class<?> PageInfoTypeClass = XposedHelpers.findClass("com.zhihu.android.data.analytics.PageInfoType", lpparam.classLoader);
                        Method a = thisObject.getClass().getMethod("a", intentClass);
                        Object intent = intentClass.getConstructors()[0].newInstance(DebugFragment, null, "SCREEN_NAME_NULL", Array.newInstance(PageInfoTypeClass, 0));
                        a.invoke(thisObject, intent);
                        param.setResult(false);
                    }
                }
            });
            XposedBridge.hookMethod(DebugFragment.getMethod("a", Bundle.class, String.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.thisObject.getClass() == DebugFragment) {
                        Class<?> PreferenceFragmentCompatClass = XposedHelpers.findClass("androidx.preference.g", lpparam.classLoader);
                        Class<?> PreferenceManagerClass = XposedHelpers.findClass("androidx.preference.j", lpparam.classLoader);
                        Method setSharedPreferencesName = PreferenceManagerClass.getMethod("a", String.class);
                        Field[] fields = PreferenceFragmentCompatClass.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getType() == PreferenceManagerClass) {
                                field.setAccessible(true);
                                setSharedPreferencesName.invoke(field.get(param.thisObject), "zhiliao_preferences");
                                return;
                            }
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(DebugFragment, "h", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return null;
                }
            });
            XposedHelpers.findAndHookMethod(DebugFragment, "onPreferenceClick", "androidx.preference.Preference", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });
            XposedHelpers.findAndHookMethod(DebugFragment, "a", "androidx.preference.Preference", Object.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
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
}
