package com.shatyuka.zhiliao;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.Toast;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    final String hookPackage = "com.zhihu.android";
    final String modulePackage = "com.shatyuka.zhiliao";
    private Context context;
    private SharedPreferences prefs;

    private Resources modRes;
    private Object preference_zhiliao;

    private static int version_click = 0;
    private static int author_click = 0;

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
        if (modulePackage.equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod("com.shatyuka.zhiliao.MySettingsFragment", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        } else if (hookPackage.equals(lpparam.packageName)) {
            if (!Helper.initClassHelper(lpparam.classLoader))
                return;

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
                        modRes = context.getPackageManager().getResourcesForApplication(modulePackage);
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

            XposedHelpers.findAndHookMethod("androidx.preference.i", lpparam.classLoader, "a", int.class, Helper.PreferenceGroup, new XC_MethodHook() {
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
                        Method inflate = param.thisObject.getClass().getMethod("a", XmlPullParser, Helper.PreferenceGroup);
                        param.setResult(inflate.invoke(param.thisObject, parser, param.args[1]));
                    } finally {
                        parser.close();
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.SettingsFragment, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    preference_zhiliao = Helper.findPreference.invoke(thisObject, "preference_id_zhiliao");
                    Helper.setSummary.invoke(preference_zhiliao, "当前版本 " + modRes.getString(R.string.app_version));
                    Helper.setOnPreferenceClickListener.invoke(preference_zhiliao, thisObject);
                }
            });

            XposedHelpers.findAndHookMethod(Helper.SettingsFragment, "onPreferenceClick", Helper.Preference, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] == preference_zhiliao) {
                        Object thisObject = param.thisObject;
                        Method a = thisObject.getClass().getMethod("a", Helper.ZHIntent);
                        Object intent = Helper.ZHIntent.getConstructors()[0].newInstance(Helper.DebugFragment, null, "SCREEN_NAME_NULL", Array.newInstance(Helper.PageInfoType, 0));
                        a.invoke(thisObject, intent);
                        param.setResult(false);
                    }
                }
            });
            XposedBridge.hookMethod(Helper.DebugFragment.getMethod("a", Bundle.class, String.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.thisObject.getClass() == Helper.DebugFragment) {
                        Field[] fields = Helper.PreferenceFragmentCompat.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getType() == Helper.PreferenceManager) {
                                field.setAccessible(true);
                                Helper.setSharedPreferencesName.invoke(field.get(param.thisObject), "zhiliao_preferences");
                                return;
                            }
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.BasePreferenceFragment, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.thisObject.getClass() == Helper.DebugFragment) {
                        Field[] fields = Helper.BasePreferenceFragment.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getType().getName().equals("com.zhihu.android.app.ui.widget.SystemBar")) {
                                field.setAccessible(true);
                                Object systemBar = field.get(param.thisObject);
                                Object toolbar = systemBar.getClass().getMethod("getToolbar").invoke(systemBar);
                                toolbar.getClass().getMethod("setTitle", CharSequence.class).invoke(toolbar, "知了");
                                break;
                            }
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.DebugFragment, "h", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    Object preference_version = Helper.findPreference.invoke(thisObject, "preference_version");
                    Object preference_author = Helper.findPreference.invoke(thisObject, "preference_author");
                    Object preference_telegram = Helper.findPreference.invoke(thisObject, "preference_telegram");
                    Object preference_donate = Helper.findPreference.invoke(thisObject, "preference_donate");
                    Object preference_status = Helper.findPreference.invoke(thisObject, "preference_status");

                    String real_version = context.getPackageManager().getResourcesForApplication(modulePackage).getString(R.string.app_version);
                    String loaded_version = modRes.getString(R.string.app_version);
                    Helper.setSummary.invoke(preference_version, loaded_version);
                    if (loaded_version.equals(real_version))
                        preference_status.getClass().getMethod("c", boolean.class).invoke(preference_status, false);
                    else
                        Helper.setOnPreferenceClickListener.invoke(preference_status, thisObject);

                    Helper.setIcon.invoke(preference_status, modRes.getDrawable(R.drawable.ic_refresh));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_mainswitch"), modRes.getDrawable(R.drawable.ic_toggle_on));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_launchad"), modRes.getDrawable(R.drawable.ic_ad_units));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_feedad"), modRes.getDrawable(R.drawable.ic_table_rows));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_marketcard"), modRes.getDrawable(R.drawable.ic_vip));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_answerlistad"), modRes.getDrawable(R.drawable.ic_format_list));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_sharead"), modRes.getDrawable(R.drawable.ic_share));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_answerad"), modRes.getDrawable(R.drawable.ic_notes));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_club"), modRes.getDrawable(R.drawable.ic_group));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_goods"), modRes.getDrawable(R.drawable.ic_local_mall));
                    Helper.setIcon.invoke(preference_version, modRes.getDrawable(R.drawable.ic_info));
                    Helper.setIcon.invoke(preference_author, modRes.getDrawable(R.drawable.ic_person));
                    Helper.setIcon.invoke(preference_telegram, modRes.getDrawable(R.drawable.ic_telegram));
                    Helper.setIcon.invoke(preference_donate, modRes.getDrawable(R.drawable.ic_monetization));

                    Helper.setOnPreferenceChangeListener.invoke(Helper.findPreference.invoke(thisObject, "accept_eula"), thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_version, thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_author, thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_telegram, thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_donate, thisObject);

                    if (prefs.getBoolean("accept_eula", false)) {
                        Object category_eula = Helper.findPreference.invoke(thisObject, "category_eula");
                        category_eula.getClass().getMethod("c", boolean.class).invoke(category_eula, false);
                    } else {
                        Object switch_main = Helper.findPreference.invoke(param.thisObject, "switch_mainswitch");
                        switch_main.getClass().getMethod("g", boolean.class).invoke(switch_main, false);
                    }
                    return null;
                }
            });
            XposedHelpers.findAndHookMethod(Helper.DebugFragment, "onPreferenceClick", "androidx.preference.Preference", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    Object preference = param.args[0];
                    String key = (String) preference.getClass().getMethod("C").invoke(preference);
                    switch (key) {
                        case "preference_status":
                            System.exit(0);
                            break;
                        case "preference_version":
                            version_click++;
                            if (version_click == 5) {
                                Toast.makeText(context, "点我次数再多，更新也不会变快哦", Toast.LENGTH_SHORT).show();
                                version_click = 0;
                            }
                            break;
                        case "preference_author":
                            author_click++;
                            if (author_click == 5) {
                                Toast.makeText(context, modRes.getStringArray(R.array.click_author)[new Random().nextInt(4)], Toast.LENGTH_LONG).show();
                                author_click = 0;
                            }
                            break;
                        case "preference_telegram":
                            Uri uri = Uri.parse("https://t.me/joinchat/OibCWxbdCMkJ2fG8J1DpQQ");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            ((Context) Helper.getContext.invoke(param.thisObject)).startActivity(intent);
                            break;
                        case "preference_donate":
                            Intent donate_intent = new Intent();
                            donate_intent.setAction(Intent.ACTION_MAIN);
                            donate_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            donate_intent.putExtra("zhiliao_donate", true);
                            donate_intent.setClassName(modulePackage, "com.shatyuka.zhiliao.MainActivity");
                            ((Context) Helper.getContext.invoke(param.thisObject)).startActivity(donate_intent);
                            break;
                    }
                    return false;
                }
            });
            XposedHelpers.findAndHookMethod(Helper.DebugFragment, "a", "androidx.preference.Preference", Object.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    if ((boolean) param.args[1]) {
                        Object switch_main = Helper.findPreference.invoke(param.thisObject, "switch_mainswitch");
                        switch_main.getClass().getMethod("g", boolean.class).invoke(switch_main, true);
                        Object category_eula = Helper.findPreference.invoke(param.thisObject, "category_eula");
                        category_eula.getClass().getMethod("c", boolean.class).invoke(category_eula, false);
                    }
                    return true;
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MainActivity, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    Intent intent = ((Activity) thisObject).getIntent();
                    if (intent.hasExtra("zhiliao_settings")) {
                        Helper.addFragmentToOverlay.invoke(thisObject, Helper.ZHIntent.getConstructors()[0].newInstance(Helper.DebugFragment, null, "SCREEN_NAME_NULL", Array.newInstance(Helper.PageInfoType, 0)));
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.MainActivity, "onNewIntent", Intent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    Intent intent = (Intent) param.args[0];
                    if (intent.hasExtra("zhiliao_settings")) {
                        Helper.addFragmentToOverlay.invoke(thisObject, Helper.ZHIntent.getConstructors()[0].newInstance(Helper.DebugFragment, null, "SCREEN_NAME_NULL", Array.newInstance(Helper.PageInfoType, 0)));
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
        }
    }
}
