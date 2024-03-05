package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class PanelBubble implements IHook {

    static List<Method> buildBubbleViewMethodList;

    @Override
    public String getName() {
        return "关闭底部气泡通知";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Class<?> bubbleViewNew = classLoader.loadClass("com.zhihu.android.panel.ui.bubble.BubbleViewNew");
        Class<?> bubbleView = classLoader.loadClass("com.zhihu.android.panel.ui.bubble.BubbleView");

        Class<?> panelBubbleUtilNew = classLoader.loadClass("com.zhihu.android.panel.ui.bubble.n");

        buildBubbleViewMethodList = Arrays.stream(panelBubbleUtilNew.getDeclaredMethods())
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getReturnType() == bubbleView
                        || method.getReturnType() == bubbleViewNew).collect(Collectors.toList());

    }

    @Override
    public void hook() throws Throwable {
        for (Method buildBubbleView : buildBubbleViewMethodList) {
            XposedBridge.hookMethod(buildBubbleView, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) &&
                            Helper.prefs.getBoolean("switch_panelbubble", true)) {
                        param.setResult(null);
                    }
                }
            });
        }
    }

}