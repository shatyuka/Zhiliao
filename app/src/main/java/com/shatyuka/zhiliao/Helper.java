package com.shatyuka.zhiliao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
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
    public static int versionCode;

    public static Object settingsView;

    public static boolean officialZhihu = true;

    public final static String hookPackage = "com.zhihu.android";
    private final static byte[] signature = new byte[]{(byte) 0xB6, (byte) 0xF9, (byte) 0x97, (byte) 0xE3, (byte) 0x82, 0x7B, (byte) 0xE1, 0x1A, (byte) 0xF2, (byte) 0xFA, 0x4A, 0x15, 0x3F, (byte) 0xEA, 0x3F, (byte) 0xE6, 0x27, 0x68, 0x66, 0x02};

    /** @noinspection RedundantSuppression*/
    @SuppressWarnings("deprecation")
    static boolean init(ClassLoader classLoader) {
        try {
            init_class(classLoader);

            prefs = context.getSharedPreferences("zhiliao_preferences", Context.MODE_PRIVATE);
            packageInfo = context.getPackageManager().getPackageInfo("com.zhihu.android", 0);
            versionCode = packageInfo.versionCode;

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
        try {
            IZhihuWebView = classLoader.loadClass("com.zhihu.android.app.mercury.api.IZhihuWebView");
        } catch (ClassNotFoundException ignore) {
            IZhihuWebView = classLoader.loadClass("com.zhihu.android.app.search.ui.widget.SearchResultLayout").getDeclaredField("c").getType();
        }
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

    public static void doRestart(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent != null) {
                Intent restartIntent = Intent.makeRestartActivityTask(launchIntent.getComponent());
                restartIntent.setPackage(context.getPackageName());
                context.startActivity(restartIntent);
                System.exit(0);
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
            if (i > 26) {
                String classNameNew = beginWith + index2StrNew(i);
                try {
                    Class<?> clazz = classLoader.loadClass(classNameNew);
                    if (check.check(clazz)) {
                        return clazz;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    // a...z, aa...az, ba...bz
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

    // a...z, a0...z0, a1...z1
    private static String index2StrNew(int n) {
        StringBuilder result = new StringBuilder();
        int m = n % 26;
        result.insert(0, (char) ('a' + m));
        int cnt = n / 26;
        if (cnt > 0) {
            result.append(cnt - 1);
        }
        return result.toString();
    }

    public static Method getMethodByParameterTypes(Class<?> clazz, Class<?>... parameterTypes) {
        if (clazz == null) return null;
        for (Method method : clazz.getDeclaredMethods()) {
            Class<?>[] types = method.getParameterTypes();
            if (Arrays.equals(types, parameterTypes)) {
                return method;
            }
        }
        return null;
    }

    public static Method getMethodByParameterTypes(Class<?> clazz, int skip, Class<?>... parameterTypes) {
        if (clazz == null) return null;
        int count = 0;
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (Arrays.equals(types, parameterTypes)) {
                if (count < skip) {
                    count++;
                    continue;
                }
                return method;
            }
        }
        return null;
    }

    public static Field findFieldByType(Class<?> clazz, Class<?> type) {
        Optional<Field> fieldOptional = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getType() == type).findFirst();
        if (!fieldOptional.isPresent())
            return null;

        Field field = fieldOptional.get();
        field.setAccessible(true);
        return field;
    }

    /** @noinspection RedundantSuppression*/
    @SuppressWarnings("deprecation")
    private static Signature[] getSignatures(Context context) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        Signature[] sig;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            android.content.pm.SigningInfo sign = pm.getPackageInfo(hookPackage, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo;
            sig = sign.hasMultipleSigners() ? sign.getApkContentsSigners() : sign.getSigningCertificateHistory();
        } else {
            sig = pm.getPackageInfo(hookPackage, PackageManager.GET_SIGNATURES).signatures;
        }
        return sig;
    }

    public static boolean checkSignature(Context context) {
        try {
            Signature[] sig = getSignatures(context);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            for (Signature s : sig) {
                byte[] dig = md.digest(s.toByteArray());
                if (Arrays.equals(dig, signature)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
