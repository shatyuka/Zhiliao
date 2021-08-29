package com.shatyuka.zhiliao;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.robv.android.xposed.XposedBridge;

public class Helper {
    public static Class<?> MorphAdHelper;
    public static Class<?> AnswerPagerFragment;

    public static Field DataUnique_type;

    public static Pattern regex_title;
    public static Pattern regex_author;
    public static Pattern regex_content;

    public static float scale;
    public static int sensitivity;

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @SuppressLint("StaticFieldLeak")
    public static Context modContext;
    public static SharedPreferences prefs;
    public static Resources modRes;
    public static PackageInfo packageInfo;

    static boolean init(ClassLoader classLoader) {
        try {
            try {
                modContext = context.createPackageContext("com.shatyuka.zhiliao", 0);
            } catch (Throwable ignored) {
                modContext = context;
            }

            prefs = context.getSharedPreferences("zhiliao_preferences", Context.MODE_PRIVATE);
            packageInfo = context.getPackageManager().getPackageInfo("com.zhihu.android", 0);

            MorphAdHelper = classLoader.loadClass("com.zhihu.android.morph.ad.utils.MorphAdHelper");
            AnswerPagerFragment = classLoader.loadClass("com.zhihu.android.answer.module.pager.AnswerPagerFragment");

            DataUnique_type = classLoader.loadClass("com.zhihu.android.api.model.template.DataUnique").getField("type");

            regex_title = compileRegex(prefs.getString("edit_title", ""));
            regex_author = compileRegex(prefs.getString("edit_author", ""));
            regex_content = compileRegex(prefs.getString("edit_content", ""));

            scale = context.getResources().getDisplayMetrics().density;
            sensitivity = 10 - prefs.getInt("seekbar_sensitivity", 5);

            return true;
        } catch (Exception e) {
            XposedBridge.log("[Zhiliao] " + e.toString());
            return false;
        }
    }

    public static Pattern compileRegex(String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        } else try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException ignore) {
            return null;
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    static Resources getModuleRes(String path) throws Throwable {
        AssetManager assetManager = AssetManager.class.newInstance();
        AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(assetManager, path);
        return new Resources(assetManager, null, null);
    }

    public static boolean getDarkMode() {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static void doRestart(Context context) {
        try {
            if (context != null) {
                PackageManager pm = context.getPackageManager();
                if (pm != null) {
                    Intent mStartActivity = pm.getLaunchIntentForPackage(context.getPackageName());
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean deleteDirectory(String filePath) {
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean result;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    result = new File(file.getAbsolutePath()).delete();
                    if (!result) return false;
                } else if (file.isDirectory()) {
                    result = deleteDirectory(file.getAbsolutePath());
                    if (!result) return false;
                }
            }
        }
        return dirFile.delete();
    }
}
