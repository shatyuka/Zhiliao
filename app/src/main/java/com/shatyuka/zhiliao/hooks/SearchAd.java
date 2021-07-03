package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class SearchAd implements IHook {
    static Method convert;

    static Field SearchTopTabsItemList_commercialData;
    static Field PresetWords_preset;
    static Field SearchRecommendQuery_content;

    @Override
    public String getName() {
        return "去搜索广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Class<?> JacksonResponseBodyConverter;
        try {
            JacksonResponseBodyConverter = classLoader.loadClass("com.zhihu.android.net.b.b");
            convert = JacksonResponseBodyConverter.getMethod("convert", Object.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            try {
                JacksonResponseBodyConverter = classLoader.loadClass("retrofit2.b.a.c");
                convert = JacksonResponseBodyConverter.getMethod("convert", Object.class);
            } catch (ClassNotFoundException | NoSuchMethodException e2) {
                JacksonResponseBodyConverter = classLoader.loadClass("j.b.a.c");
                convert = JacksonResponseBodyConverter.getMethod("convert", Object.class);
            }
        }
        Class<?> SearchTopTabsItemList = classLoader.loadClass("com.zhihu.android.api.model.SearchTopTabsItemList");
        Class<?> PresetWords = classLoader.loadClass("com.zhihu.android.api.model.PresetWords");
        try {
            Class<?> SearchRecommendQuery = classLoader.loadClass("com.zhihu.android.api.model.SearchRecommendQuery");
            SearchRecommendQuery_content = SearchRecommendQuery.getField("content");
        } catch (ClassNotFoundException ignored) {
        }

        SearchTopTabsItemList_commercialData = SearchTopTabsItemList.getField("commercialData");
        PresetWords_preset = PresetWords.getField("preset");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(convert, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_searchad", true)) {
                    Object result = param.getResult();
                    if (result != null) {
                        switch (result.getClass().getName()) {
                            case "com.zhihu.android.api.model.SearchTopTabsItemList": {
                                SearchTopTabsItemList_commercialData.set(result, null);
                                break;
                            }
                            case "com.zhihu.android.api.model.PresetWords": {
                                PresetWords_preset.set(result, null);
                                break;
                            }
                            case "com.zhihu.android.api.model.SearchRecommendQuery": {
                                SearchRecommendQuery_content.set(result, null);
                                break;
                            }
                        }
                        param.setResult(result);
                    }
                }
            }
        });
    }
}
