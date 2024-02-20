package com.shatyuka.zhiliao.hooks;


import static com.shatyuka.zhiliao.Helper.JsonNodeOp;
import static com.shatyuka.zhiliao.hooks.CardViewFeatureShortFilter.preProcessShortContent;
import static com.shatyuka.zhiliao.hooks.CardViewFeatureShortFilter.shouldRemoveShortContent;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


public class CardViewMixShortFilter implements IHook {

    static Method mixupDataParser_jsonNode2List;

    @Override
    public String getName() {
        return "卡片视图相关过滤";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Class<?> mixupDataParser = findMixupDataParser(classLoader);

        mixupDataParser_jsonNode2List = Arrays.stream(mixupDataParser.getDeclaredMethods())
                .filter(method -> method.getReturnType() == List.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == JsonNodeOp.JsonNode)
                .findFirst().get();

    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(mixupDataParser_jsonNode2List, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }
                if (Helper.prefs.getBoolean("switch_feedad", false)) {
                    filterShortContent(param.args[0]);
                }
            }
        });
    }

    public void filterShortContent(Object shortContentListJsonNode) throws InvocationTargetException, IllegalAccessException {
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
            } else {
                preProcessShortContent(shortContentJsonNode);
            }
        }
    }

    private Class<?> findMixupDataParser(ClassLoader classLoader) throws ClassNotFoundException {
        try {
            return classLoader.loadClass("com.zhihu.android.mixshortcontainer.t.b.b.b.d");
        } catch (Exception ignore) {
            return classLoader.loadClass("com.zhihu.android.mixshortcontainer.dataflow.b.b.a.d");
        }
    }

}
