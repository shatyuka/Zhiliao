package com.shatyuka.zhiliao.hooks;

import android.content.Context;

import com.shatyuka.zhiliao.Helper;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class AnswerListAd implements IHook {
    static Class<?> AnswerListWrapper;

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
    }

    @Override
    public void hook() throws Throwable {
        XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveAnswerAdParam", Context.class, "com.zhihu.android.api.model.AnswerListAd", Boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                    param.setResult(false);
                }
            }
        });
        if (AnswerListWrapper != null) {
            XposedHelpers.findAndHookMethod(AnswerListWrapper, "insertAdBrandToList", ArrayList.class, new XC_MethodHook() {
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
