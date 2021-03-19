package com.shatyuka.zhiliao;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    final static String hookPackage = "com.zhihu.android";
    final static String modulePackage = "com.shatyuka.zhiliao";
    static String modulePath;

    private native void initNative();

    private static boolean is64Bit(ClassLoader classLoader) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return android.os.Process.is64Bit();
        }
        try {
            String path = (String)ClassLoader.class.getDeclaredMethod("findLibrary", String.class).invoke(classLoader, "art");
            if (path != null) {
                return path.contains("lib64");
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (modulePackage.equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod("com.shatyuka.zhiliao.MySettingsFragment", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        } else if (hookPackage.equals(lpparam.packageName)) {
            try {
                System.loadLibrary("zhiliao");
                initNative();
            } catch (Throwable ignored) {
                try { // Let's try again
                    System.load(modulePath.substring(0, modulePath.lastIndexOf('/')) + (is64Bit(lpparam.classLoader) ? "/lib/arm64/libzhiliao.so" : "/lib/arm/libzhiliao.so"));
                    initNative();
                } catch (Throwable ignored2) {
                }
            }

            XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.tencent.tinker.loader.app.TinkerApplication", lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = 0;
                }
            });

            XposedHelpers.findAndHookMethod(android.app.Instrumentation.class, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] instanceof Application) {
                        Helper.context = ((Application) param.args[0]).getApplicationContext();
                        Helper.prefs = Helper.context.getSharedPreferences("zhiliao_preferences", Context.MODE_PRIVATE);

                        if (!Helper.init(lpparam.classLoader) || !ZhihuPreference.init(lpparam.classLoader) || !Functions.init(lpparam.classLoader)) {
                            Toast.makeText(Helper.context, "知了初始化失败，可能不支持当前版本知乎: " + Helper.packageInfo.versionName, Toast.LENGTH_SHORT).show();
                        }
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
