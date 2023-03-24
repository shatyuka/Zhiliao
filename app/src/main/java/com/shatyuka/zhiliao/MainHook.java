package com.shatyuka.zhiliao;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;
import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public final static String hookPackage = "com.zhihu.android";
    private final static byte[] signature = new byte[]{(byte) 0xB6, (byte) 0xF9, (byte) 0x97, (byte) 0xE3, (byte) 0x82, 0x7B, (byte) 0xE1, 0x1A, (byte) 0xF2, (byte) 0xFA, 0x4A, 0x15, 0x3F, (byte) 0xEA, 0x3F, (byte) 0xE6, 0x27, 0x68, 0x66, 0x02};
    static String modulePath;

    private native void initNative();

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private void tryLoadNative(boolean showLog) {
        String path = modulePath.substring(0, modulePath.lastIndexOf('/'));
        String[] libs = {
                path + "/lib/arm64/libzhiliao.so",
                path + "/lib/arm/libzhiliao.so",
                modulePath + "!/lib/arm64-v8a/libzhiliao.so",
                modulePath + "!/lib/armeabi-v7a/libzhiliao.so"
        };

        for (String lib : libs) {
            try {
                System.load(lib);
                initNative();
                return;
            } catch (Throwable ignored) {
            }
        }

        if (showLog)
            XposedBridge.log("[Zhiliao] 知了native模块加载失败");
    }

    @SuppressLint("PackageManagerGetSignatures")
    static boolean checkSignature(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Signature[] sig;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                android.content.pm.SigningInfo sign = pm.getPackageInfo(hookPackage, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo;
                sig = sign.hasMultipleSigners() ? sign.getApkContentsSigners() : sign.getSigningCertificateHistory();
            } else {
                sig = pm.getPackageInfo(hookPackage, PackageManager.GET_SIGNATURES).signatures;
            }
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

    @SuppressLint("PackageManagerGetSignatures")
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (hookPackage.equals(lpparam.packageName)) {
            boolean isMainProcess = hookPackage.equals(lpparam.processName);
            Context systemContext = (Context) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext");
            if (!checkSignature(systemContext)) {
                tryLoadNative(isMainProcess);
            }

            if (!isMainProcess)
                return;

            try {
                XposedBridge.hookAllConstructors(lpparam.classLoader.loadClass("com.tencent.tinker.loader.app.TinkerApplication"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[0] = 0;
                    }
                });
            } catch (ClassNotFoundException ignored) {
            }

            XposedHelpers.findAndHookMethod(android.app.Instrumentation.class, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.args[0] instanceof Application) {
                        Helper.context = ((Application) param.args[0]).getApplicationContext();

                        if (!Helper.init(lpparam.classLoader))
                            Helper.toast("知了初始化失败，可能不支持当前版本知乎: " + Helper.packageInfo.versionName, Toast.LENGTH_SHORT);
                        else {
                            Hooks.init(lpparam.classLoader);
                            if (!Helper.prefs.getBoolean("switch_mainswitch", false))
                                Helper.toast("知了加载成功，请到设置页面开启功能。", Toast.LENGTH_LONG);
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file.getName().equals(".allowXposed")) {
                        param.setResult(true);
                    }
                }
            });
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        modulePath = startupParam.modulePath;
        Helper.modRes = Helper.getModuleRes(startupParam.modulePath);
    }
}
