package com.shatyuka.zhiliao.hooks;


import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


/**
 * 卡片视图(FeatureUI)
 */
public class CardViewFeatureShortFilter implements IHook {

    static Method mixupDataParser_jsonNode2Object;

    static Method mixupDataParser_jsonNode2List;

    static Method objectNode_put;

    static Method jsonNode_get;

    static Method jsonNode_size;

    static Method jsonNode_isArray;

    static Method jsonNode_iterator;

    @Override
    public String getName() {
        return "卡片视图相关过滤(FeatureUI)";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

        // todo 工具类
        Class<?> jsonNode = classLoader.loadClass("com.fasterxml.jackson.databind.JsonNode");
        jsonNode_get = jsonNode.getDeclaredMethod("get", String.class);
        jsonNode_size = jsonNode.getDeclaredMethod("size");
        jsonNode_iterator = jsonNode.getDeclaredMethod("iterator");
        jsonNode_isArray = jsonNode.getDeclaredMethod("isArray");

        Class<?> objectNode = classLoader.loadClass("com.fasterxml.jackson.databind.node.ObjectNode");
        objectNode_put = objectNode.getDeclaredMethod("put", String.class, jsonNode);

        Class<?> mixupDataParser = classLoader.loadClass("com.zhihu.android.service.short_container_service.dataflow.repo.c.c");
        mixupDataParser_jsonNode2Object = Arrays.stream(mixupDataParser.getDeclaredMethods())
                .filter(method -> method.getReturnType() == Object.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == jsonNode).findFirst().get();
        mixupDataParser_jsonNode2List = Arrays.stream(mixupDataParser.getDeclaredMethods())
                .filter(method -> method.getReturnType() == List.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == jsonNode).findFirst().get();
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(mixupDataParser_jsonNode2List, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }
                if (Helper.prefs.getBoolean("switch_feedad", true)) {
                    filterShortContent(param.args[0]);
                }
            }
        });

        XposedBridge.hookMethod(mixupDataParser_jsonNode2Object, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }
                preProcessShortContent(param.args[0]);
            }
        });

    }

    private void filterShortContent(Object shortContentListJsonNode) throws InvocationTargetException, IllegalAccessException {
        Object dataJsonNode = jsonNode_get.invoke(shortContentListJsonNode, "data");
        if (dataJsonNode == null || !(boolean) jsonNode_isArray.invoke(dataJsonNode)) {
            return;
        }

        Iterator<?> shortContentIterator = (Iterator<?>) jsonNode_iterator.invoke(dataJsonNode);
        while (shortContentIterator != null && shortContentIterator.hasNext()) {
            Object shortContentJsonNode = shortContentIterator.next();
            if (shortContentJsonNode == null) {
                continue;
            }
            if (shouldRemoveShortContent(shortContentJsonNode)) {
                shortContentIterator.remove();
            }
        }
    }

    private boolean shouldRemoveShortContent(Object shortContentJsonNode) {
        try {
            return isAd(shortContentJsonNode) || hasMoreType(shortContentJsonNode);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        return false;
    }

    private boolean isAd(Object shortContentJsonNode) throws InvocationTargetException, IllegalAccessException {
        if (jsonNode_get.invoke(shortContentJsonNode, "adjson") != null) {
            return true;
        }

        Object adInfo = jsonNode_get.invoke(shortContentJsonNode, "ad_info");
        if (adInfo == null) {
            XposedBridge.log(shortContentJsonNode.toString());
            return false;
        }
        Object adInfoData = jsonNode_get.invoke(adInfo, "data");
        if (adInfoData != null) {
            // "" , "{}"
            return adInfoData.toString().length() > 4;
        }
        return false;
    }

    /**
     * todo: 有多个type的, 不一定全是推广/广告, 有概率被误去除
     */
    private boolean hasMoreType(Object shortContentJsonNode) throws InvocationTargetException, IllegalAccessException {
        Object bizTypeList = jsonNode_get.invoke(shortContentJsonNode, "biz_type_list");
        return (int) jsonNode_size.invoke(bizTypeList) > 1;
    }

    private void preProcessShortContent(Object shortContentJsonNode) {
        try {
            Object searchWordJsonNode = jsonNode_get.invoke(shortContentJsonNode, "search_word");
            if (searchWordJsonNode != null) {
                objectNode_put.invoke(searchWordJsonNode, "queries", null);
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        try {
            objectNode_put.invoke(shortContentJsonNode, "relationship_tips", null);
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        try {
            Object thirdBusiness = jsonNode_get.invoke(shortContentJsonNode, "third_business");
            if (thirdBusiness != null) {
                Object relatedQueries = jsonNode_get.invoke(thirdBusiness, "related_queries");
                if (relatedQueries != null) {
                    objectNode_put.invoke(relatedQueries, "queries", null);
                }
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }

    }
}
