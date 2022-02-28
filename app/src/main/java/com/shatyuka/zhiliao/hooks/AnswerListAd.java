package com.shatyuka.zhiliao.hooks;

import android.content.Context;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class AnswerListAd implements IHook {
    static Class<?> AnswerListWrapper;
    static Class<?> AnswerListAd;

    @Override
    public String getName() {
        return "去回答列表广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            AnswerListWrapper = classLoader.loadClass("com.zhihu.android.question.api.model.AnswerListWrapper");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            AnswerListAd = classLoader.loadClass("com.zhihu.android.api.model.AnswerListAd");
        } catch (ClassNotFoundException e) {
            AnswerListAd = classLoader.loadClass("com.zhihu.android.adbase.model.AnswerListAd");
        }
    }

    @Override
    public void hook() throws Throwable {
        XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveAnswerAdParam", Context.class, AnswerListAd, Boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                    param.setResult(false);
                }
            }
        });
        if (AnswerListWrapper != null) {
            XposedBridge.hookAllMethods(AnswerListWrapper, "insertAdBrandToList", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                        param.setResult(null);
                    }
                }
            });
        }
    }
}
