package com.shatyuka.zhiliao;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.shatyuka.zhiliao.xposed.XC_MethodHook;
import com.shatyuka.zhiliao.xposed.XposedBridge;
import com.shatyuka.zhiliao.xposed.XposedHelpers;

import java.io.File;
import java.util.Map;
import java.util.Set;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/** Modern libxposed API 102 entry point. */
public final class MainHook extends XposedModule {
    private String processName;
    private boolean initialized;

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        processName = param.getProcessName();
        XposedBridge.attach(this);
    }

    @Override
    public void onPackageReady(XposedModuleInterface.PackageReadyParam param) {
        if (!Helper.hookPackage.equals(param.getPackageName())
                || !Helper.hookPackage.equals(processName)
                || !param.isFirstPackage()) return;

        ClassLoader classLoader = param.getClassLoader();
        Helper.hostApkPath = param.getApplicationInfo().sourceDir;
        try {
            Helper.modRes = Helper.getModuleRes(getModuleApplicationInfo().sourceDir);
        } catch (Throwable throwable) {
            XposedBridge.log("[Zhiliao] Cannot load module resources: " + throwable);
        }

        disableTinkerSafeMode(classLoader);
        hookApplicationCreate(classLoader);
        hookAllowXposedMarker();
    }

    private void disableTinkerSafeMode(ClassLoader classLoader) {
        try {
            XposedBridge.hookAllConstructors(
                    classLoader.loadClass("com.tencent.tinker.loader.app.TinkerApplication"),
                    new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) {
                            if (param.args.length > 0) param.args[0] = 0;
                        }
                    });
        } catch (Throwable ignored) {
        }
    }

    private void hookApplicationCreate(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(android.app.Instrumentation.class,
                "callApplicationOnCreate", Application.class, new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        if (initialized || !(param.args[0] instanceof Application)) return;
                        initialized = true;
                        Application application = (Application) param.args[0];
                        Helper.context = application.getApplicationContext();
                        Helper.prefs = getRemotePreferences("zhiliao_preferences");
                        migrateLegacyPreferences(Helper.prefs);
                        try {
                            if (!Helper.init(classLoader, Helper.prefs)) {
                                Helper.toast("初始化失败，当前知乎版本可能暂不受支持："
                                        + Helper.packageInfo.versionName, Toast.LENGTH_SHORT);
                            } else {
                                Hooks.init(classLoader);
                            }
                        } finally {
                            DexResolver.close();
                        }
                    }
                });
    }

    private void migrateLegacyPreferences(SharedPreferences remote) {
        SharedPreferences legacy = Helper.context.getSharedPreferences(
                "zhiliao_preferences", android.content.Context.MODE_PRIVATE);
        Map<String, ?> legacyValues = legacy.getAll();
        if (legacyValues.isEmpty()) return;

        Map<String, ?> remoteValues = remote.getAll();
        SharedPreferences.Editor editor = remote.edit();
        boolean changed = false;
        for (Map.Entry<String, ?> entry : legacyValues.entrySet()) {
            if (remoteValues.containsKey(entry.getKey())) continue;
            Object value = entry.getValue();
            if (value instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) value);
            else if (value instanceof Integer) editor.putInt(entry.getKey(), (Integer) value);
            else if (value instanceof Long) editor.putLong(entry.getKey(), (Long) value);
            else if (value instanceof Float) editor.putFloat(entry.getKey(), (Float) value);
            else if (value instanceof String) editor.putString(entry.getKey(), (String) value);
            else if (value instanceof Set<?>) {
                @SuppressWarnings("unchecked")
                Set<String> strings = (Set<String>) value;
                editor.putStringSet(entry.getKey(), strings);
            } else continue;
            changed = true;
        }
        if (changed) editor.apply();
    }

    private void hookAllowXposedMarker() {
        XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                File file = (File) param.thisObject;
                if (".allowXposed".equals(file.getName())) param.setResult(true);
            }
        });
    }
}
