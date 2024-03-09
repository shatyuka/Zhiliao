package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class FeedTopHotBanner implements IHook {
    static Class<?> feedTopHotAutoJacksonDeserializer;

    @Override
    public String getName() {
        return "隐藏推荐页置顶热门";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            feedTopHotAutoJacksonDeserializer = classLoader.loadClass("com.zhihu.android.api.model.FeedTopHotAutoJacksonDeserializer");
        } catch (ClassNotFoundException ignore) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (feedTopHotAutoJacksonDeserializer != null) {
            XposedBridge.hookAllMethods(feedTopHotAutoJacksonDeserializer, "deserialize", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedtophot", false)) {
                        param.setResult(null);
                    }
                }
            });
        }
    }
}
