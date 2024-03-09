package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HeadZoneBanner implements IHook {
    static Class<?> feedsHotListFragment2;

    static Field head_zone;

    @Override
    public String getName() {
        return "隐藏热榜顶部置顶";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            Class<?> rankFeedList = classLoader.loadClass("com.zhihu.android.api.model.RankFeedList");
            head_zone = rankFeedList.getDeclaredField("head_zone");
            head_zone.setAccessible(true);
            feedsHotListFragment2 = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsHotListFragment2");
        } catch (ClassNotFoundException | NoSuchFieldException ignore) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (feedsHotListFragment2 != null && head_zone != null) {
            XposedBridge.hookAllMethods(feedsHotListFragment2, "postRefreshSucceed", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws IllegalAccessException {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)) {
                        head_zone.set(param.args[0], null);
                    }
                }
            });
        }
    }
}
