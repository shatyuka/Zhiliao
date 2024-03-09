package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * 卡片视图去除关注按钮
 * todo: 整合web去除关注
 */
public class FollowButton implements IHook {
    static Class<?> followWithAvatarView;
    static Class<?> bottomReactionView;
    static Class<?> followPeopleButton;
    static Class<?> zHAuthorInfoView;

    static Field followWithAvatarViewField;
    static Field followPeopleButtonField;

    @Override
    public String getName() {
        return "去关注按钮";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        followWithAvatarView = classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowWithAvatarView");
        bottomReactionView = classLoader.loadClass("com.zhihu.android.mixshortcontainer.function.mixup.view.BottomReactionView");

        followPeopleButton = classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowPeopleButton");
        zHAuthorInfoView = classLoader.loadClass("com.zhihu.android.mixshortcontainer.function.mixup.author.ZHAuthorInfoView");

        followWithAvatarViewField = findFieldByType(bottomReactionView, followWithAvatarView);
        followPeopleButtonField = findFieldByType(zHAuthorInfoView, followPeopleButton);
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllMethods(bottomReactionView, "setData", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_subscribe", false)) {
                    // 去除底部关注(带头像)
                    FrameLayout followWithAvatarViewInstance = (FrameLayout) followWithAvatarViewField.get(param.thisObject);
                    followWithAvatarViewInstance.setVisibility(View.GONE);
                }
            }
        });

        XposedBridge.hookAllMethods(zHAuthorInfoView, "setData", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_subscribe", false)) {
                    // 去除顶部关注
                    ViewGroup followPeopleButtonFieldInstance = (ViewGroup) followPeopleButtonField.get(param.thisObject);
                    followPeopleButtonFieldInstance.setVisibility(View.GONE);
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
