package com.shatyuka.zhiliao.hooks;

import android.view.MotionEvent;
import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Horizontal implements IHook {
    static boolean horizontal = false;

    static Class<?> ActionSheetLayout;
    static Class<?> NestChildScrollChange;
    static Class<?> AnswerContentView;
    static Class<?> NextBtnClickListener;
    static Class<?> VerticalPageTransformer;
    static Class<?> MixPagerContainer;

    static Method onNestChildScrollRelease;
    static Method isReadyPageTurning;
    static Method nextAnswer;
    static Method lastAnswer;

    static Field ActionSheetLayout_callbackList;
    static Field UserAction_DRAG_UP;
    static Field UserAction_DRAG_DOWN;

    static float width;
    static float height;

    @Override
    public String getName() {
        return "左右划切换回答";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        ActionSheetLayout = classLoader.loadClass("com.zhihu.android.content.widget.ActionSheetLayout");
        NestChildScrollChange = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView$mNestChildScrollChange$1");
        AnswerContentView = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView");
        NextBtnClickListener = classLoader.loadClass("com.zhihu.android.answer.module.content.AnswerContentView$mNextBtnClickListener$1");
        Class<?> DirectionBoundView = classLoader.loadClass("com.zhihu.android.answer.widget.DirectionBoundView");
        try {
            VerticalPageTransformer = classLoader.loadClass("com.zhihu.android.answer.pager.VerticalViewPager$VerticalPageTransformer");
        } catch (ClassNotFoundException e) {
            VerticalPageTransformer = classLoader.loadClass("com.zhihu.android.answer.widget.VerticalViewPager$VerticalPageTransformer");
        }

        onNestChildScrollRelease = NestChildScrollChange.getMethod("onNestChildScrollRelease", float.class, int.class);
        isReadyPageTurning = DirectionBoundView.getMethod("isReadyPageTurning");

        ActionSheetLayout_callbackList = ActionSheetLayout.getDeclaredField("z");
        ActionSheetLayout_callbackList.setAccessible(true);

        if (Helper.packageInfo.versionCode > 2614) {
            MixPagerContainer = classLoader.loadClass("com.zhihu.android.mix.widget.MixPagerContainer");
            Class<?> VerticalPagerContainer = classLoader.loadClass("com.zhihu.android.bootstrap.vertical_pager.VerticalPagerContainer");
            Class<?> UserAction = classLoader.loadClass("com.zhihu.android.bootstrap.vertical_pager.e");
            if (!UserAction.isEnum()) {
                UserAction = classLoader.loadClass("com.zhihu.android.bootstrap.vertical_pager.f");
                if (!UserAction.isEnum())
                    throw new ClassNotFoundException("com.zhihu.android.bootstrap.vertical_pager.UserAction");
            }

            nextAnswer = VerticalPagerContainer.getMethod("a", UserAction);
            lastAnswer = VerticalPagerContainer.getMethod("b", UserAction);

            UserAction_DRAG_UP = UserAction.getField("DRAG_UP");
            UserAction_DRAG_DOWN = UserAction.getField("DRAG_DOWN");
        }

        height = Helper.scale * 160 / 5;
        width = height / 2;
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_horizontal", false)) {
            XposedHelpers.findAndHookMethod(ActionSheetLayout, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
                float old_x = 0;
                float old_y = 0;
                long time = 0;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    MotionEvent e = (MotionEvent) param.args[0];
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            old_x = e.getX();
                            old_y = e.getY();
                            time = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                            float dx = e.getX() - old_x;
                            float dy = e.getY() - old_y;
                            if (Math.abs(dx) > width * Helper.sensitivity && Math.abs(dy) < height * Helper.sensitivity && (System.currentTimeMillis() - time) < 500) {
                                for (Object callback : (List) ActionSheetLayout_callbackList.get(param.thisObject)) {
                                    if (callback.getClass() == NestChildScrollChange) {
                                        onNestChildScrollRelease.invoke(callback, dx, 5201314);
                                    }
                                }
                            }
                            break;
                    }
                }
            });
            XposedHelpers.findAndHookMethod(VerticalPageTransformer, "transformPage", View.class, float.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    View view = (View) param.args[0];
                    float position = (float) param.args[1];
                    if (position < -1) {
                        view.setAlpha(0);
                    } else if (position <= 1) {
                        view.setAlpha(1);
                        view.setTranslationX(horizontal ? 0 : view.getWidth() * -position);
                        view.setTranslationY(horizontal ? 0 : view.getHeight() * position);
                    } else {
                        view.setAlpha(0);
                    }
                    return null;
                }
            });
            XposedHelpers.findAndHookMethod(NestChildScrollChange, "onNestChildScrollRelease", float.class, int.class, new XC_MethodHook() {
                Unhook hook_isReadyPageTurning;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if ((int) param.args[1] == 5201314) {
                        hook_isReadyPageTurning = XposedBridge.hookMethod(isReadyPageTurning, XC_MethodReplacement.returnConstant(true));
                        horizontal = true;
                    } else {
                        horizontal = false;
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (hook_isReadyPageTurning != null) {
                        hook_isReadyPageTurning.unhook();
                    }
                }
            });
            XposedHelpers.findAndHookMethod(NextBtnClickListener, "onClick", View.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    horizontal = false;
                }
            });
            XposedHelpers.findAndHookMethod(AnswerContentView, "showNextAnswer", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    horizontal = false;
                }
            });

            if (Helper.packageInfo.versionCode > 2614) {
                XposedHelpers.findAndHookMethod(MixPagerContainer, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                    float old_x = 0;
                    float old_y = 0;
                    long time = 0;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        MotionEvent e = (MotionEvent) param.args[0];
                        switch (e.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                old_x = e.getX();
                                old_y = e.getY();
                                time = System.currentTimeMillis();
                                break;
                            case MotionEvent.ACTION_UP:
                                float dx = e.getX() - old_x;
                                float dy = e.getY() - old_y;
                                if (Math.abs(dx) > width * Helper.sensitivity && Math.abs(dy) < height * Helper.sensitivity && (System.currentTimeMillis() - time) < 500) {
                                    if (dx < 0)
                                        nextAnswer.invoke(param.thisObject, UserAction_DRAG_UP.get(null));
                                    else
                                        lastAnswer.invoke(param.thisObject, UserAction_DRAG_DOWN.get(null));
                                }
                                break;
                        }
                    }
                });
            }
        }
    }
}
