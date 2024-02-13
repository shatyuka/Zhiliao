package com.shatyuka.zhiliao.hooks;


import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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

    static Method jsonNode_size;

    static Field adInfoField;

    static Field searchWordsField;

    static Field relationShipTipsUINodeField;

    static Class<?> UINodeConvert;

    static Class<?> relatedQueries;

    static Method UINodeConvert_convert;

    static Field bizTypeField;

    static Class<?> shortContainerPagingFragment;

    static boolean firstLoaded = true;

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
        jsonNode_size = jsonNode.getDeclaredMethod("size");

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

        bizTypeField = shortContent.getDeclaredField("bizType");
        bizTypeField.setAccessible(true);

        UINodeConvert = classLoader.loadClass("com.zhihu.android.service.short_container_service.dataflow.repo.b.a");

        UINodeConvert_convert = Arrays.stream(UINodeConvert.getDeclaredMethods())
                .filter(method -> method.getReturnType() == List.class)
                .filter(method -> method.getParameterCount() == 3)
                .filter(method -> {
                    Class<?>[] types = method.getParameterTypes();
                    return types[0] == shortContent && types[1] == boolean.class && types[2] == boolean.class;
                }).findFirst().get();

        relatedQueries = classLoader.loadClass("com.zhihu.android.api.model.RelatedQueries");

        shortContainerPagingFragment = classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.fragment.ShortContainerPagingFragment");
    }

    @Override
    public void hook() throws Throwable {

        XposedBridge.hookAllMethods(shortContainerPagingFragment, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                firstLoaded = true;
            }
        });

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
                    if (shouldFilterShortContent(param.getResult())) {
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
                if (UINodeList == null || UINodeList.isEmpty()) {
                    return;
                }
                // 首个回答下相关搜索
                UINodeList.removeIf(node -> relatedQueries == node.getClass());
            }
        });

    }

    private boolean shouldFilterShortContent(Object shortContentInstance) {
        // 避免从推荐页点击广告回答/文章显示空白
        if (firstLoaded) {
            firstLoaded = false;
            return false;
        }
        if (shortContentInstance == null) {
            return true;
        }
        try {
            return isAd(shortContentInstance) || hasMoreBizType(shortContentInstance);
        } catch (Exception e) {
            XposedBridge.log(e);
            return false;
        }
    }

    private boolean hasMoreBizType(Object shortContentInstance) throws IllegalAccessException, InvocationTargetException {
        Object bizTypeInstance = bizTypeField.get(shortContentInstance);
        return (int) jsonNode_size.invoke(bizTypeInstance) > 1;
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
