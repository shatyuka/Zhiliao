package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class RedDot implements IHook {
    static Class<?> FeedsTabsFragment;
    static Class<?> FeedFollowAvatarCommonViewHolder;
    static Class<?> NotiMsgModel;
    static Class<?> ViewModel;

    static Method ZHMainTabLayout_updateBadges;
    static Method BottomNavMenuItemView_setUnreadCount;
    static Method BottomNavMenuItemViewForIconOnly_setUnreadCount;
    static Method BaseBottomNavMenuItemView_setNavBadge;
    static Method NotiUnreadCountKt_hasUnread;
    static Method IconWithDotAndCountView_setUnreadCount;
    static Method CountDotView_setUnreadCount;

    static Field FeedFollowAvatarCommonViewHolder_dot;

    @Override
    public String getName() {
        return "不显示小红点";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        FeedsTabsFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment");
        FeedFollowAvatarCommonViewHolder = classLoader.loadClass("com.zhihu.android.moments.viewholders.FeedFollowAvatarCommonViewHolder");
        Class<?> ZHMainTabLayout = classLoader.loadClass("com.zhihu.android.app.ui.widget.ZHMainTabLayout");
        Class<?> BottomNavMenuItemView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemView");
        Class<?> BottomNavMenuItemViewForIconOnly = classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemViewForIconOnly");
        NotiMsgModel = classLoader.loadClass("com.zhihu.android.notification.model.viewmodel.NotiMsgModel");
        try {
            ZHMainTabLayout_updateBadges = ZHMainTabLayout.getDeclaredMethod("d");
        } catch (NoSuchMethodException ignored) {
        }
        try {
            Class<?> NotiUnreadCountKt = classLoader.loadClass("com.zhihu.android.notification.model.NotiUnreadCountKt");
            NotiUnreadCountKt_hasUnread = NotiUnreadCountKt.getMethod("hasUnread", int.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        try {
            ViewModel = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.help.tabhelp.model.ViewModel");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            BottomNavMenuItemView_setUnreadCount = BottomNavMenuItemView.getMethod("a", int.class);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            BottomNavMenuItemViewForIconOnly_setUnreadCount = BottomNavMenuItemViewForIconOnly.getMethod("a", int.class);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            Class<?> BaseBottomNavMenuItemView = classLoader.loadClass("com.zhihu.android.bottomnav.core.BaseBottomNavMenuItemView");
            Class<?> NavBadge = classLoader.loadClass("com.zhihu.android.bottomnav.api.model.NavBadge");
            BaseBottomNavMenuItemView_setNavBadge = BaseBottomNavMenuItemView.getMethod("a", NavBadge);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }

        try {
            Class<?> IconWithDotAndCountView = classLoader.loadClass("com.zhihu.android.community_base.view.icon.IconWithDotAndCountView");
            IconWithDotAndCountView_setUnreadCount = IconWithDotAndCountView.getMethod("a", int.class, boolean.class, int.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }

        try {
            Class<?> CountDotView = classLoader.loadClass("com.zhihu.android.notification.widget.CountDotView");
            CountDotView_setUnreadCount = CountDotView.getMethod("a", int.class, boolean.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
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
            if (ZHMainTabLayout_updateBadges != null)
                XposedBridge.hookMethod(ZHMainTabLayout_updateBadges, XC_MethodReplacement.returnConstant(null));
            if (BottomNavMenuItemView_setUnreadCount != null)
                XposedBridge.hookMethod(BottomNavMenuItemView_setUnreadCount, XC_MethodReplacement.returnConstant(null));
            if (BottomNavMenuItemViewForIconOnly_setUnreadCount != null)
                XposedBridge.hookMethod(BottomNavMenuItemViewForIconOnly_setUnreadCount, XC_MethodReplacement.returnConstant(null));
            if (BaseBottomNavMenuItemView_setNavBadge != null)
                XposedBridge.hookMethod(BaseBottomNavMenuItemView_setNavBadge, XC_MethodReplacement.returnConstant(null));
            XposedHelpers.findAndHookMethod(NotiMsgModel, "getUnreadCount", XC_MethodReplacement.returnConstant(0));
            if (NotiUnreadCountKt_hasUnread != null)
                XposedBridge.hookMethod(NotiUnreadCountKt_hasUnread, XC_MethodReplacement.returnConstant(false));
            if (IconWithDotAndCountView_setUnreadCount != null)
                XposedBridge.hookMethod(IconWithDotAndCountView_setUnreadCount, XC_MethodReplacement.returnConstant(null));
            if (CountDotView_setUnreadCount != null)
                XposedBridge.hookMethod(CountDotView_setUnreadCount, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        View obj = (View) param.thisObject;
                        obj.setVisibility(View.GONE);
                        return null;
                    }
                });
            if (ViewModel != null) {
                XposedHelpers.findAndHookConstructor(ViewModel, View.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            ViewGroup view = (ViewGroup) param.args[0];
                            if (view.getChildCount() == 2) { // red_parent
                                view.setVisibility(View.GONE);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                });
            }
        }
    }
}
