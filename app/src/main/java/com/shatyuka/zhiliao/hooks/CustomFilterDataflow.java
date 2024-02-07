package com.shatyuka.zhiliao.hooks;


import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    static Field searchWordsField;

    static Field relationShipTipsUINodeField;

    static Class<?> UINodeConvert;

    static Class<?> relatedQueries;

    static Method UINodeConvert_convert;

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

        searchWordsField = shortContent.getDeclaredField("searchWords");
        searchWordsField.setAccessible(true);

        relationShipTipsUINodeField = shortContent.getDeclaredField("relationShipTipsUINode");
        relationShipTipsUINodeField.setAccessible(true);

        UINodeConvert = classLoader.loadClass("com.zhihu.android.service.short_container_service.dataflow.repo.b.a");

        UINodeConvert_convert = Arrays.stream(UINodeConvert.getDeclaredMethods())
                .filter(method -> method.getReturnType() == List.class)
                .filter(method -> method.getParameterCount() == 3)
                .filter(method -> {
                    Class<?>[] types = method.getParameterTypes();
                    return types[0] == shortContent && types[1] == boolean.class && types[2] == boolean.class;
                }).findFirst().get();

        relatedQueries = classLoader.loadClass("com.zhihu.android.api.model.RelatedQueries");
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
                        return;
                    }
                }

                if (param.getResult().getClass() == shortContent) {
                    // 顶部搜索框搜索词(卡片视图 && feature UI)
                    searchWordsField.set(param.getResult(), null);
                    // xxx等人赞同(首个回答另算)
                    relationShipTipsUINodeField.set(param.getResult(), null);
                }

            }
        });

        XposedBridge.hookMethod(UINodeConvert_convert, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }

                List<?> UINodeList = (List<?>) param.getResult();

                if (UINodeList != null && !UINodeList.isEmpty()) {
                    List<?> proceedUINodeList = UINodeList.stream()
                            // 去除相关搜索UINode
                            .filter(node -> node.getClass() != relatedQueries)
                            .collect(Collectors.toList());

                    param.setResult(proceedUINodeList);
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
