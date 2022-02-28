package com.shatyuka.zhiliao.hooks;

import android.content.Context;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class CommentAd implements IHook {
    static Class<?> CommentListAd;

    @Override
    public String getName() {
        return "去评论广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            CommentListAd = classLoader.loadClass("com.zhihu.android.api.model.CommentListAd");
        } catch (ClassNotFoundException e) {
            CommentListAd = classLoader.loadClass("com.zhihu.android.adbase.model.CommentListAd");
        }
    }

    @Override
    public void hook() throws Throwable {
        try {
            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveCommentAdParam", Context.class, CommentListAd, Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_commentad", true)) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
