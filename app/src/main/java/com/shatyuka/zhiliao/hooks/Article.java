package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class Article implements IHook {
    static Class<?> ContentMixAdapter;
    static Class<?> ContentMixPagerFragment;

    static Method getItemCount;

    static Field ContentMixAdapter_fragment;
    static Field ContentMixPagerFragment_type;

    @Override
    public String getName() {
        return "精简文章页面";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        if (Helper.versionCode > 2614) {
            try {
                ContentMixAdapter = classLoader.loadClass("com.zhihu.android.mix.a.a");
                getItemCount = ContentMixAdapter.getMethod("getItemCount");
            } catch (Throwable e) {
                try {
                    ContentMixAdapter = classLoader.loadClass("com.zhihu.android.mix.b.a");
                    getItemCount = ContentMixAdapter.getMethod("getItemCount");
                } catch (Throwable e2) {
                    try {
                        ContentMixAdapter = classLoader.loadClass("com.zhihu.android.mix.adapter.a");
                        getItemCount = ContentMixAdapter.getMethod("getItemCount");
                    } catch (Throwable e3) {
                        ContentMixAdapter = classLoader.loadClass("com.zhihu.android.mix.adapter.ContentMixAdapter");
                        getItemCount = ContentMixAdapter.getMethod("getItemCount");
                    }
                }
            }
            ContentMixPagerFragment = classLoader.loadClass("com.zhihu.android.mix.fragment.ContentMixPagerFragment");
            String[] fragment_names = new String[]{"f", "g", "h", "B"};
            for (String name : fragment_names) {
                try {
                    Field field = ContentMixAdapter.getDeclaredField(name);
                    if (field.getType().getName().equals("androidx.fragment.app.Fragment")) {
                        ContentMixAdapter_fragment = field;
                        break;
                    }
                } catch (NoSuchFieldException ignore) {
                }
            }
            if (ContentMixAdapter_fragment == null) {
                throw new NoSuchFieldException("fragment");
            }
            ContentMixAdapter_fragment.setAccessible(true);
            try {
                ContentMixPagerFragment_type = ContentMixPagerFragment.getField("c");
                if (ContentMixPagerFragment_type.getType() != String.class)
                    throw new NoSuchFieldException("type");
            } catch (NoSuchFieldException e) {
                ContentMixPagerFragment_type = ContentMixPagerFragment.getField("t");
                if (ContentMixPagerFragment_type.getType() != String.class)
                    throw new NoSuchFieldException("type");
            }
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.versionCode > 2614) { // after 6.61.0
            if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_article", false)) {
                XposedBridge.hookMethod(getItemCount, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.thisObject.getClass() == ContentMixAdapter && ContentMixPagerFragment_type.get(ContentMixAdapter_fragment.get(param.thisObject)) == "article")
                            param.setResult(1);
                    }
                });
            }
        }
    }
}
