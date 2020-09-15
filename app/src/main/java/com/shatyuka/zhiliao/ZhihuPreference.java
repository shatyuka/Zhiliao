package com.shatyuka.zhiliao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ZhihuPreference {
    final static String modulePackage = "com.shatyuka.zhiliao";

    private static Object preference_zhiliao;

    private static int version_click = 0;
    private static int author_click = 0;

    static boolean init(final ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("androidx.preference.i", classLoader, "a", int.class, Helper.PreferenceGroup, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XmlResourceParser parser;
                    int id = (int) param.args[0];
                    InputStream inputStream = Helper.context.getResources().openRawResource(id);
                    if (inputStream.available() > 4000 && inputStream.available() < 5000)
                        parser = Helper.modRes.getXml(R.xml.settings);
                    else if (inputStream.available() > 5000)
                        parser = Helper.modRes.getXml(R.xml.preferences_zhihu);
                    else
                        return;
                    try {
                        Class<?> XmlPullParser = XposedHelpers.findClass("org.xmlpull.v1.XmlPullParser", classLoader);
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
                    Helper.setSummary.invoke(preference_zhiliao, "当前版本 " + Helper.modRes.getString(R.string.app_version));
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

                    String real_version = Helper.context.getPackageManager().getResourcesForApplication(modulePackage).getString(R.string.app_version);
                    String loaded_version = Helper.modRes.getString(R.string.app_version);
                    Helper.setSummary.invoke(preference_version, loaded_version);
                    if (loaded_version.equals(real_version))
                        preference_status.getClass().getMethod("c", boolean.class).invoke(preference_status, false);
                    else
                        Helper.setOnPreferenceClickListener.invoke(preference_status, thisObject);

                    Helper.setIcon.invoke(preference_status, Helper.modRes.getDrawable(R.drawable.ic_refresh));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_mainswitch"), Helper.modRes.getDrawable(R.drawable.ic_toggle_on));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_launchad"), Helper.modRes.getDrawable(R.drawable.ic_ad_units));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_feedad"), Helper.modRes.getDrawable(R.drawable.ic_table_rows));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_answerlistad"), Helper.modRes.getDrawable(R.drawable.ic_format_list));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_commentad"), Helper.modRes.getDrawable(R.drawable.ic_comment));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_sharead"), Helper.modRes.getDrawable(R.drawable.ic_share));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_answerad"), Helper.modRes.getDrawable(R.drawable.ic_notes));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_video"), Helper.modRes.getDrawable(R.drawable.ic_play_circle));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_marketcard"), Helper.modRes.getDrawable(R.drawable.ic_vip));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_club"), Helper.modRes.getDrawable(R.drawable.ic_group));
                    Helper.setIcon.invoke(Helper.findPreference.invoke(thisObject, "switch_goods"), Helper.modRes.getDrawable(R.drawable.ic_local_mall));
                    Helper.setIcon.invoke(preference_version, Helper.modRes.getDrawable(R.drawable.ic_info));
                    Helper.setIcon.invoke(preference_author, Helper.modRes.getDrawable(R.drawable.ic_person));
                    Helper.setIcon.invoke(preference_telegram, Helper.modRes.getDrawable(R.drawable.ic_telegram));
                    Helper.setIcon.invoke(preference_donate, Helper.modRes.getDrawable(R.drawable.ic_monetization));

                    Helper.setOnPreferenceChangeListener.invoke(Helper.findPreference.invoke(thisObject, "accept_eula"), thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_version, thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_author, thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_telegram, thisObject);
                    Helper.setOnPreferenceClickListener.invoke(preference_donate, thisObject);

                    if (Helper.prefs.getBoolean("accept_eula", false)) {
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
                                Toast.makeText(Helper.context, "点我次数再多，更新也不会变快哦", Toast.LENGTH_SHORT).show();
                                version_click = 0;
                            }
                            break;
                        case "preference_author":
                            author_click++;
                            if (author_click == 5) {
                                Toast.makeText(Helper.context, Helper.modRes.getStringArray(R.array.click_author)[new Random().nextInt(4)], Toast.LENGTH_LONG).show();
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
            return true;
        } catch (NoSuchMethodException e) {
            XposedBridge.log(e.getMessage());
            return false;
        }
    }
}
