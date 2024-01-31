package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


public class CustomTabFilter implements IHook {

    static Class<?> mainPageFragment;

    static Field customTabInfoList;

    static Field tabType;

    @Override
    public String getName() {
        return "自定义首页顶栏Tab";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        mainPageFragment = classLoader.loadClass("com.zhihu.android.app.feed.explore.view.MainPageFragment");

        customTabInfoList = Arrays.stream(mainPageFragment.getDeclaredFields())
                .filter(field -> field.getType() == List.class).findFirst().get();
        customTabInfoList.setAccessible(true);

        tabType = classLoader.loadClass("com.zhihu.android.api.model.CustomTabInfo").getDeclaredField("tab_type");
        tabType.setAccessible(true);
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false)) {
            XposedBridge.hookAllMethods(mainPageFragment, "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    List<Object> tabList = (List<Object>) customTabInfoList.get(param.thisObject);

                    String tabFilter = Helper.prefs.getString("edit_tabfilter", "");
                    if (tabFilter.isEmpty()) {
                        Helper.prefs.edit().putString("edit_tabfilter", encodePattern(tabList)).apply();
                    } else {
                        List<Object> postProcessTabList = postProcessTabList(tabList, decodePattern(tabFilter));
                        customTabInfoList.set(param.thisObject, postProcessTabList);
                    }
                }

            });
        }
    }

    private String encodePattern(List<Object> tabList) {
        if (tabList == null || tabList.isEmpty()) {
            return null;
        }
        return tabList.stream().map(o -> {
                    try {
                        return (String) tabType.get(o);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }).filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining("|"));
    }

    private List<String> decodePattern(String pattern) {
        return Arrays.stream(pattern.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Object> postProcessTabList(List<Object> tabList, List<String> typeFilterList) {
        if (tabList == null || tabList.isEmpty()) {
            return tabList;
        }

        Map<String, Object> typeTabMap = tabList.stream()
                .collect(Collectors.toMap(tab -> {
                    try {
                        return (String) tabType.get(tab);
                    } catch (IllegalAccessException ignore) {
                        return null;
                    }
                }, tab -> tab));

        return typeFilterList.stream()
                .map(typeTabMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
