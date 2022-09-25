package com.shatyuka.zhiliao.hooks;

import android.content.Context;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class FeedAd implements IHook {
    static Class<?> BasePagingFragment;
    static Class<?> FeedAdvert;
    static Class<?> ListAd;
    static Class<?> Advert;
    static Class<?> Ad;

    static Field FeedList_data;

    @Override
    public String getName() {
        return "去信息流广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        BasePagingFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment");
        try {
            FeedAdvert = classLoader.loadClass("com.zhihu.android.api.model.FeedAdvert");
            ListAd = classLoader.loadClass("com.zhihu.android.api.model.ListAd");
            Advert = classLoader.loadClass("com.zhihu.android.api.model.Advert");
            Ad = classLoader.loadClass("com.zhihu.android.api.model.Ad");
        } catch (ClassNotFoundException e) {
            FeedAdvert = classLoader.loadClass("com.zhihu.android.adbase.model.FeedAdvert");
            ListAd = classLoader.loadClass("com.zhihu.android.adbase.model.ListAd");
            Advert = classLoader.loadClass("com.zhihu.android.adbase.model.Advert");
            Ad = classLoader.loadClass("com.zhihu.android.adbase.model.Ad");
        }

        FeedList_data = classLoader.loadClass("com.zhihu.android.api.model.FeedList").getField("data");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllMethods(BasePagingFragment, "postRefreshSucceed", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    if (param.args[0] == null)
                        return;
                    List<?> list = (List<?>) FeedList_data.get(param.args[0]);
                    if (list == null || list.isEmpty())
                        return;
                    for (int i = list.size() - 1; i >= 0; i--) {
                        if (list.get(i).getClass() == FeedAdvert) {
                            list.remove(i);
                        }
                    }
                }
            }
        });
        XposedHelpers.findAndHookMethod(BasePagingFragment, "insertDataRangeToList", int.class, List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    if (param.args[1] == null)
                        return;
                    List<?> list = (List<?>) param.args[1];
                    if (list.isEmpty())
                        return;
                    for (int i = list.size() - 1; i >= 0; i--) {
                        if (list.get(i).getClass() == FeedAdvert) {
                            list.remove(i);
                        }
                    }
                }
            }
        });
        XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolve", Context.class, FeedAdvert, boolean.class, Boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });
        XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolve", Context.class, ListAd, Boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });
        XposedHelpers.findAndHookMethod(Advert, "isSlidingWindow", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });
        XposedHelpers.findAndHookMethod(Ad, "isFloatAdCard", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                    param.setResult(false);
                }
            }
        });
    }
}
