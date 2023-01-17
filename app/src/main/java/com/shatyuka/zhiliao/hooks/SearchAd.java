package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class SearchAd implements IHook {
    static LinkedList<Method> converts = new LinkedList<>();

    static Field SearchTopTabsItemList_commercialData;
    static Field PresetWords_preset;
    static Field SearchRecommendQuery_content;

    @Override
    public String getName() {
        return "去搜索广告";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        String[] classNames = {
                "com.zhihu.android.net.b.b",
                "com.zhihu.android.net.c.b",
                "retrofit2.b.a.c",
                "j.b.a.c"
        };
        Class<?> JacksonResponseBodyConverter;
        for (String className : classNames) {
            try {
                JacksonResponseBodyConverter = classLoader.loadClass(className);
                converts.add(JacksonResponseBodyConverter.getMethod("convert", Object.class));
            } catch (Throwable ignored) {
            }
        }
        if (converts.isEmpty()) {
            throw new ClassNotFoundException("retrofit2.converter.jackson.JacksonResponseBodyConverter");
        }

        try {
            Class<?> SearchTopTabsItemList = classLoader.loadClass("com.zhihu.android.api.model.SearchTopTabsItemList");
            SearchTopTabsItemList_commercialData = SearchTopTabsItemList.getField("commercialData");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class<?> SearchRecommendQuery = classLoader.loadClass("com.zhihu.android.api.model.SearchRecommendQuery");
            SearchRecommendQuery_content = SearchRecommendQuery.getField("content");
        } catch (ClassNotFoundException | NoSuchFieldException ignored) {
        }

        Class<?> PresetWords = classLoader.loadClass("com.zhihu.android.api.model.PresetWords");
        PresetWords_preset = PresetWords.getField("preset");
    }

    @Override
    public void hook() throws Throwable {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_searchad", true)) {
                    Object result = param.getResult();
                    if (result != null) {
                        switch (result.getClass().getName()) {
                            case "com.zhihu.android.api.model.SearchTopTabsItemList": {
                                if (SearchTopTabsItemList_commercialData != null)
                                    SearchTopTabsItemList_commercialData.set(result, null);
                                break;
                            }
                            case "com.zhihu.android.api.model.PresetWords": {
                                PresetWords_preset.set(result, null);
                                break;
                            }
                            case "com.zhihu.android.api.model.SearchRecommendQuery": {
                                if (SearchRecommendQuery_content != null)
                                    SearchRecommendQuery_content.set(result, null);
                                break;
                            }
                        }
                        param.setResult(result);
                    }
                }
            }
        };
        for (Method convert : converts)
            XposedBridge.hookMethod(convert, hook);
    }
}
