package com.shatyuka.zhiliao.hooks;


import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


/**
 * FeatureUI with dataflow
 */
public class CustomFilterDataflow implements IHook {

    static Class<?> mixupDataParser;

    static Class<?> jsonNode;

    static Class<?> feedAdvert;

    static Class<?> shortContent;

    static Method mixupDataParser_jsonNodeConvert;

    static Method jsonNode_get;

    static Field adInfoField;

    @Override
    public String getName() {
        return "自定义过滤(Dataflow)";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

        feedAdvert = classLoader.loadClass("com.zhihu.android.adbase.model.FeedAdvert");

        shortContent = classLoader.loadClass("com.zhihu.android.service.short_container_service.dataflow.model.ShortContent");

        // todo
        mixupDataParser = classLoader.loadClass("com.zhihu.android.service.short_container_service.dataflow.repo.c.c");

        jsonNode = classLoader.loadClass("com.fasterxml.jackson.databind.JsonNode");

        jsonNode_get = jsonNode.getDeclaredMethod("get", String.class);

        mixupDataParser_jsonNodeConvert = Arrays.stream(mixupDataParser.getDeclaredMethods())
                .filter(method -> method.getReturnType() == Object.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == jsonNode).findFirst().get();

        adInfoField = shortContent.getDeclaredField("adInfo");
        adInfoField.setAccessible(true);

    }

    @Override
    public void hook() throws Throwable {

        XposedBridge.hookMethod(mixupDataParser_jsonNodeConvert, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) {
                    return;
                }
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }

                if (Helper.prefs.getBoolean("switch_feedad", true)) {
                    if (isAd(param.getResult())) {
                        param.setResult(null);
                    }
                }

            }
        });

    }

    private boolean isAd(Object shortContentInstance) throws Exception {
        if (shortContentInstance.getClass() == feedAdvert) {
            return true;
        } else if (shortContentInstance.getClass() == shortContent) {
            Object adInfo = adInfoField.get(shortContentInstance);
            if (adInfo == null) {
                return false;
            }

            Object adInfoData = jsonNode_get.invoke(adInfo, "data");
            if (adInfoData != null) {
                // "" , "{}"
                return adInfoData.toString().length() > 4;
            }
        }

        return false;
    }
}
