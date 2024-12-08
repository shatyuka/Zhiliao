package com.shatyuka.zhiliao.hooks;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.R;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class VIPBanner implements IHook {
    static Class<?> VipEntranceView;
    static Class<?> MoreVipData;
    static Class<?> NewMoreFragment;

    static Method initView;
    static Method initView_new;

    @Override
    public String getName() {
        return "隐藏会员卡片";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            VipEntranceView = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.widget.VipEntranceView");
            initView = VipEntranceView.getDeclaredMethod("a", Context.class);
        } catch (ClassNotFoundException ignored) {
            VipEntranceView = classLoader.loadClass("com.zhihu.android.premium.view.VipEntranceView");
            initView_new = VipEntranceView.getDeclaredMethod("initView", Context.class);
        }

        try {
            MoreVipData = classLoader.loadClass("com.zhihu.android.api.MoreVipData");
            NewMoreFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.NewMoreFragment");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_vipbanner", false)) {
            if (initView != null) {
                XposedBridge.hookMethod(initView, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        XmlResourceParser layout_vipentranceview = Helper.modRes.getLayout(R.layout.layout_vipentranceview);
                        LayoutInflater.from((Context) param.args[0]).inflate(layout_vipentranceview, (ViewGroup) param.thisObject);
                        return null;
                    }
                });
            }
            if (initView_new != null) {
                XposedBridge.hookMethod(initView_new, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        XmlResourceParser layout_vipentranceview = Helper.modRes.getLayout(R.layout.layout_vipentranceview_new);
                        LayoutInflater.from((Context) param.args[0]).inflate(layout_vipentranceview, (ViewGroup) param.thisObject);
                        return null;
                    }
                });
            }
            for (Method method : VipEntranceView.getMethods()) {
                if (method.getName().equals("setData")) {
                    XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(null));
                    break;
                }
            }
            XposedHelpers.findAndHookMethod(VipEntranceView, "onClick", View.class, XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods(VipEntranceView, "resetStyle", XC_MethodReplacement.returnConstant(null));

            if (MoreVipData != null && NewMoreFragment != null) {
                XposedHelpers.findAndHookMethod(NewMoreFragment, "a", MoreVipData, XC_MethodReplacement.returnConstant(null));
            }

            XposedBridge.hookAllMethods(MoreVipData, "isLegal", XC_MethodReplacement.returnConstant(Boolean.FALSE));
        }
    }
}
