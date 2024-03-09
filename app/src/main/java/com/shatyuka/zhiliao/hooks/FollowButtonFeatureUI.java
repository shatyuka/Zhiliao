package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class FollowButtonFeatureUI implements IHook {

    static Class<?> followWithAvatarView;

    static Class<?> bottomReactionViewImpl;

    static Class<?> followButtonViewImpl;

    static Class<?> followPeopleButton;

    static Field followWithAvatarViewFromImplField;

    static Field followPeopleButtonField;

    @Override
    public String getName() {
        return "去关注按钮(FeatureUI)";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

        bottomReactionViewImpl = classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.impl.BottomReactionViewImpl");

        followWithAvatarView = classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowWithAvatarView");
        followWithAvatarViewFromImplField = findFieldByType(bottomReactionViewImpl, followWithAvatarView);

        followButtonViewImpl = classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.impl.FollowButtonViewImpl");
        followPeopleButton = classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowPeopleButton");

        followPeopleButtonField = findFieldByType(followButtonViewImpl, followPeopleButton);
    }

    @Override
    public void hook() throws Throwable {

        XposedBridge.hookAllMethods(bottomReactionViewImpl, "setData", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws IllegalAccessException {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_subscribe", false)) {
                    FrameLayout followWithAvatarViewInstance = (FrameLayout) followWithAvatarViewFromImplField.get(param.thisObject);

                    if (followWithAvatarViewInstance != null) {
                        followWithAvatarViewInstance.setVisibility(View.GONE);
                    }

                }
            }
        });

        XposedBridge.hookAllMethods(followButtonViewImpl, "setData", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws IllegalAccessException {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_subscribe", false)) {
                    ViewGroup followPeopleButtonInstance = (ViewGroup) followPeopleButtonField.get(param.thisObject);

                    if (followPeopleButtonInstance != null) {
                        followPeopleButtonInstance.setVisibility(View.GONE);
                    }

                }
            }
        });

    }

    private Field findFieldByType(Class<?> clazz, Class<?> type) {
        Field field = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getType() == type).findFirst().get();

        field.setAccessible(true);
        return field;
    }
}
