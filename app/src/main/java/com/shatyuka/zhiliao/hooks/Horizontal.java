package com.shatyuka.zhiliao.hooks;

import android.view.MotionEvent;
import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Constructor;
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
    static Class<?> DirectionBoundView;
    static Class<?> VerticalPageTransformer;
    static Class<?> AnswerRouterDispatcher;

    static Method onNestChildScrollRelease;
    static Method isReadyPageTurning;

    static Field ActionSheetLayout_callbackList;
    static Field MatchResult_url;
    static Field MatchResult_bundle;
    static Field MatchResult_module;

    static Constructor<?> MatchResult;

    static int width;
    static int height;

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
        DirectionBoundView = classLoader.loadClass("com.zhihu.android.answer.widget.DirectionBoundView");
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
            AnswerRouterDispatcher = classLoader.loadClass("com.zhihu.android.answer.entrance.AnswerRouterDispatcher");
            Method[] methods = AnswerRouterDispatcher.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("buildNormal")) {
                    Class<?> matchResultClass = method.getReturnType();
                    MatchResult = matchResultClass.getConstructors()[0];
                    MatchResult_url = method.getReturnType().getField("a");
                    MatchResult_bundle = method.getReturnType().getField("b");
                    MatchResult_module = method.getReturnType().getField("d");
                    break;
                }
            }
            if (MatchResult == null)
                throw new ClassNotFoundException("com.zhihu.router.MatchResult");
        }

        height = (int) (Helper.context.getResources().getDisplayMetrics().density * 160);
        width = height / 2;
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_horizontal", false)) {
            XposedHelpers.findAndHookMethod(ActionSheetLayout, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
                float old_x = 0;
                float old_y = 0;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    MotionEvent e = (MotionEvent) param.args[0];
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            old_x = e.getX();
                            old_y = e.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            float dx = e.getX() - old_x;
                            float dy = e.getY() - old_y;
                            if (Math.abs(dx) > width && Math.abs(dy) < height) {
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
                XC_MethodHook.Unhook hook_isReadyPageTurning;

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
                // Force use old AnswerPagerFragment
                XposedBridge.hookAllMethods(AnswerRouterDispatcher, "buildNormal", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Object arg = param.args[0];
                        Object url = MatchResult_url.get(arg);
                        Object bundle = MatchResult_bundle.get(arg);
                        Object module = MatchResult_module.get(arg);
                        return MatchResult.newInstance(url, bundle, Helper.AnswerPagerFragment, module);
                    }
                });
            }
        }
    }
}
