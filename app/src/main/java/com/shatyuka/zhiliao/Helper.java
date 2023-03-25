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
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.robv.android.xposed.XposedBridge;

public class Helper {
    public static Class<?> MorphAdHelper;
    public static Class<?> AnswerPagerFragment;
    public static Class<?> IZhihuWebView;
    public static Class<?> WebViewClientWrapper;

    public static Field DataUnique_type;

    public static Pattern regex_title;
    public static Pattern regex_author;
    public static Pattern regex_content;

    public static float scale;
    public static int sensitivity;

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static SharedPreferences prefs;
    public static Resources modRes;
    public static PackageInfo packageInfo;

    public static Object settingsView;

    static boolean init(ClassLoader classLoader) {
        try {
            init_class(classLoader);

            prefs = context.getSharedPreferences("zhiliao_preferences", Context.MODE_PRIVATE);
            packageInfo = context.getPackageManager().getPackageInfo("com.zhihu.android", 0);

            regex_title = compileRegex(prefs.getString("edit_title", ""));
            regex_author = compileRegex(prefs.getString("edit_author", ""));
            regex_content = compileRegex(prefs.getString("edit_content", ""));

            scale = context.getResources().getDisplayMetrics().density;
            sensitivity = 10 - prefs.getInt("seekbar_sensitivity", 5);

            return true;
        } catch (Exception e) {
            XposedBridge.log("[Zhiliao] " + e);
            return false;
        }
    }

    public static void init_class(ClassLoader classLoader) throws Exception {
        MorphAdHelper = classLoader.loadClass("com.zhihu.android.morph.ad.utils.MorphAdHelper");
        AnswerPagerFragment = classLoader.loadClass("com.zhihu.android.answer.module.pager.AnswerPagerFragment");
        IZhihuWebView = classLoader.loadClass("com.zhihu.android.app.search.ui.widget.SearchResultLayout").getDeclaredField("c").getType();
        WebViewClientWrapper = findClass(classLoader, "com.zhihu.android.app.mercury.web.", 0, 2,
                (Class<?> clazz) -> clazz.getSuperclass() == WebViewClient.class);
        if (WebViewClientWrapper == null)
            throw new ClassNotFoundException("com.zhihu.android.app.mercury.web.WebViewClientWrapper");

        DataUnique_type = classLoader.loadClass("com.zhihu.android.api.model.template.DataUnique").getField("type");
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

    @SuppressWarnings({"JavaReflectionMemberAccess", "deprecation"})
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
                        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean deleteDirectory(File dirFile) {
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

    public static boolean deleteDirectory(String filePath) {
        return deleteDirectory(new File(filePath));
    }

    public static void toast(CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, "", duration);
        toast.setText("知了：" + text);
        toast.show();
    }

    public interface IClassCheck {
        boolean check(Class<?> clazz) throws Exception;
    }

    public static Class<?> findClass(ClassLoader classLoader, String beginWith, IClassCheck check) {
        return findClass(classLoader, beginWith, 0, 1, check);
    }

    public static Class<?> findClass(ClassLoader classLoader, String beginWith, int cycleStart, int cycleRound, IClassCheck check) {
        int count = (cycleStart + cycleRound) * 26;
        for (int i = cycleStart * 26; i < count; i++) {
            String className = beginWith + index2Str(i);
            try {
                Class<?> clazz = classLoader.loadClass(className);
                if (check.check(clazz)) {
                    return clazz;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String index2Str(int n) {
        StringBuilder result = new StringBuilder();
        while (n != 0) {
            int m = n % 26;
            if (m == 0) {
                result.insert(0, 'z');
                n = n / 26 - 1;
            } else {
                result.insert(0, (char) ('a' + m - 1));
                n /= 26;
            }
        }
        return result.toString();
    }
}
