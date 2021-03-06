package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.widget.ImageView;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class RedDot implements IHook {
    static Class<?> FeedsTabsFragment;
    static Class<?> FeedFollowAvatarCommonViewHolder;
    static Class<?> ZHMainTabLayout;
    static Class<?> BottomNavMenuItemView;
    static Class<?> BottomNavMenuItemViewForIconOnly;
    static Class<?> NotiUnreadCountKt;
    static Class<?> NotiMsgModel;

    static Field FeedFollowAvatarCommonViewHolder_dot;

    @Override
    public String getName() {
        return "不显示小红点";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        FeedsTabsFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment");
        FeedFollowAvatarCommonViewHolder = classLoader.loadClass("com.zhihu.android.moments.viewholders.FeedFollowAvatarCommonViewHolder");
        ZHMainTabLayout = classLoader.loadClass("com.zhihu.android.app.ui.widget.ZHMainTabLayout");
        BottomNavMenuItemView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemView");
        BottomNavMenuItemViewForIconOnly = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemViewForIconOnly");
        NotiMsgModel = classLoader.loadClass("com.zhihu.android.notification.model.viewmodel.NotiMsgModel");
        try {
            NotiUnreadCountKt = classLoader.loadClass("com.zhihu.android.notification.model.NotiUnreadCountKt");
        } catch (ClassNotFoundException ignored) {
        }

        FeedFollowAvatarCommonViewHolder_dot = FeedFollowAvatarCommonViewHolder.getDeclaredField("f");
        FeedFollowAvatarCommonViewHolder_dot.setAccessible(true);
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_reddot", false)) {
            XposedBridge.hookAllMethods(FeedsTabsFragment, "onUnReadCountLoaded", XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods(FeedFollowAvatarCommonViewHolder, "c", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ImageView dot = (ImageView) FeedFollowAvatarCommonViewHolder_dot.get(param.thisObject);
                    dot.setVisibility(View.GONE);
                }
            });
            XposedHelpers.findAndHookMethod(ZHMainTabLayout, "d", XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod(BottomNavMenuItemView, "a", int.class, XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod(BottomNavMenuItemViewForIconOnly, "a", int.class, XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod(NotiMsgModel, "getUnreadCount", XC_MethodReplacement.returnConstant(0));
            if (NotiUnreadCountKt != null)
                XposedHelpers.findAndHookMethod(NotiUnreadCountKt, "hasUnread", int.class, XC_MethodReplacement.returnConstant(false));
        }
    }
}
