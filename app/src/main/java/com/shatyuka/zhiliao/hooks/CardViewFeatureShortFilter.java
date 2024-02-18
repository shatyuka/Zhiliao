package com.shatyuka.zhiliao.hooks;


import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.Helper.JsonNodeOp;

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

    @Override
    public String getName() {
        return "卡片视图相关过滤(FeatureUI)";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

        Class<?> mixupDataParser = classLoader.loadClass("com.zhihu.android.service.short_container_service.dataflow.repo.c.c");
        mixupDataParser_jsonNode2Object = Arrays.stream(mixupDataParser.getDeclaredMethods())
                .filter(method -> method.getReturnType() == Object.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == Helper.JsonNodeOp.JsonNode).findFirst().get();
        mixupDataParser_jsonNode2List = Arrays.stream(mixupDataParser.getDeclaredMethods())
                .filter(method -> method.getReturnType() == List.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == Helper.JsonNodeOp.JsonNode).findFirst().get();
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
        Object dataJsonNode = JsonNodeOp.JsonNode_get.invoke(shortContentListJsonNode, "data");
        if (dataJsonNode == null || !(boolean) JsonNodeOp.JsonNode_isArray.invoke(dataJsonNode)) {
            return;
        }

        Iterator<?> shortContentIterator = (Iterator<?>) JsonNodeOp.JsonNode_iterator.invoke(dataJsonNode);
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

    public static boolean shouldRemoveShortContent(Object shortContentJsonNode) {
        try {
            return isAd(shortContentJsonNode) || hasMoreType(shortContentJsonNode);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        return false;
    }

    private static boolean isAd(Object shortContentJsonNode) throws InvocationTargetException, IllegalAccessException {
        if (JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "adjson") != null) {
            return true;
        }

        Object adInfo = JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "ad_info");
        if (adInfo == null) {
            XposedBridge.log(shortContentJsonNode.toString());
            return false;
        }
        Object adInfoData = JsonNodeOp.JsonNode_get.invoke(adInfo, "data");
        if (adInfoData != null) {
            // "" , "{}"
            return adInfoData.toString().length() > 4;
        }
        return false;
    }

    /**
     * todo: 有多个type的, 不一定全是推广/广告, 有概率被误去除
     */
    private static boolean hasMoreType(Object shortContentJsonNode) throws InvocationTargetException, IllegalAccessException {
        Object bizTypeList = JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "biz_type_list");
        return (int) JsonNodeOp.JsonNode_size.invoke(bizTypeList) > 1;
    }

    public static void preProcessShortContent(Object shortContentJsonNode) {
        try {
            Object searchWordJsonNode = JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "search_word");
            if (searchWordJsonNode != null) {
                JsonNodeOp.ObjectNode_put.invoke(searchWordJsonNode, "queries", null);
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        try {
            JsonNodeOp.ObjectNode_put.invoke(shortContentJsonNode, "relationship_tips", null);
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        if (Helper.prefs.getBoolean("switch_related", false)) {
            try {
                Object thirdBusiness = JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "third_business");
                if (thirdBusiness != null) {
                    Object relatedQueries = JsonNodeOp.JsonNode_get.invoke(thirdBusiness, "related_queries");
                    if (relatedQueries != null) {
                        JsonNodeOp.ObjectNode_put.invoke(relatedQueries, "queries", null);
                    }
                }
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }

    }
}
