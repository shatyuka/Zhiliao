package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.view.ViewGroup;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class NextAnswer implements IHook {
    static Class<?> NextContentAnimationView;
    static Class<?> NextContentAnimationView_short;
    static Class<?> MixShortContainerFragment;

    static Method initView;
    static Method initLayout;

    static Field nextButton;

    @Override
    public String getName() {
        return "移除下一个回答按钮";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        if (Helper.versionCode > 2614) {
            NextContentAnimationView = classLoader.loadClass("com.zhihu.android.mix.widget.NextContentAnimationView");
            try {
                NextContentAnimationView_short = classLoader.loadClass("com.zhihu.android.mixshortcontainer.function.next.NextContentAnimationView");
                MixShortContainerFragment = classLoader.loadClass("com.zhihu.android.mixshortcontainer.MixShortContainerFragment");
            } catch (Throwable ignored) {
            }

            initView = Helper.getMethodByParameterTypes(MixShortContainerFragment, 0, View.class);
            initLayout = Helper.getMethodByParameterTypes(MixShortContainerFragment, 1, View.class);

            if (MixShortContainerFragment != null) {
                String[] nextButton_names = new String[]{"t", "f", "e", "M"};
                for (String name : nextButton_names) {
                    try {
                        Field field = MixShortContainerFragment.getDeclaredField(name);
                        if (field.getType().getName().equals("com.zhihu.android.base.widget.ZHFrameLayout")) {
                            nextButton = field;
                            break;
                        }
                    } catch (NoSuchFieldException ignore) {
                    }
                }
                if (nextButton == null) {
                    throw new NoSuchFieldException("nextButton");
                }
                nextButton.setAccessible(true);
            }
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_nextanswer", false)) {
            XposedHelpers.findAndHookMethod(Helper.AnswerPagerFragment, "setupNextAnswerBtn", XC_MethodReplacement.returnConstant(null));
            if (initView != null) {
                XposedBridge.hookMethod(initView, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        nextButton.set(param.thisObject, null);
                    }
                });
            }
            if (initLayout != null) {
                XposedBridge.hookMethod(initLayout, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        nextButton.set(param.thisObject, null);
                    }
                });
            }

            if (Helper.versionCode > 2614) {
                XposedHelpers.findAndHookMethod(ViewGroup.class, "addView", View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (NextContentAnimationView.isAssignableFrom(param.args[0].getClass()) || (NextContentAnimationView_short != null && NextContentAnimationView_short.isAssignableFrom(param.args[0].getClass())))
                            ((View) param.args[0]).setVisibility(View.GONE);
                    }
                });

                XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (NextContentAnimationView.isAssignableFrom(param.thisObject.getClass()) || (NextContentAnimationView_short != null && NextContentAnimationView_short.isAssignableFrom(param.args[0].getClass())))
                            param.args[0] = View.GONE;
                    }
                });
            }
        }
    }
}
