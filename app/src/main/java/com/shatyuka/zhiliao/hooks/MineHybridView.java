package com.shatyuka.zhiliao.hooks;

import android.view.View;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class MineHybridView implements IHook {
    static Class<?> mineTabFragment;
    static Class<?> mineHybridView;

    static Field mineHybridViewField;

    @Override
    public String getName() {
        return "隐藏「我的」底部混合卡片";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        mineTabFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.mine.MineTabFragment");
        mineHybridView = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.mine.widget.MineHybridView");

        mineHybridViewField = Arrays.stream(mineTabFragment.getDeclaredFields()).filter(field -> field.getType() == mineHybridView).findFirst().get();
        mineHybridViewField.setAccessible(true);
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllMethods(mineTabFragment, "onCreateView", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_minehybrid", false)) {
                    View mineHybridView = (View) mineHybridViewField.get(param.thisObject);
                    if (mineHybridView != null) {
                        mineHybridView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
