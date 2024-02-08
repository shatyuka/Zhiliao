package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HotListFilter implements IHook {

    static Class<?> feedsHotListFragment2;

    static Class<?> rankFeedList;

    static Class<?> ZHObjectList;

    static Field ZHObjectListDataField;

    static Class<?> rankFeedModule;

    static Class<?> hotLoadMore;

    static Class<?> rankFeed;

    static Class<?> rankFeedContent;

    static Class<?> linkArea;

    static Field rankFeed_targetField;

    static Field rankFeedContent_linkAreaField;

    static Field linkArea_urlField;

    static Class<?> response;

    static Field response_bodyField;

    static Method feedsHotListFragment2_processRankFeedList;

    static Field rankFeedList_displayNumField;

    static final Pattern QUESTION_URL_PATTERN = Pattern.compile("zhihu\\.com/question/");

    @Override
    public String getName() {
        return "自定义热榜过滤";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        feedsHotListFragment2 = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsHotListFragment2");

        rankFeedList = classLoader.loadClass("com.zhihu.android.api.model.RankFeedList");
        ZHObjectList = classLoader.loadClass("com.zhihu.android.api.model.ZHObjectList");

        ZHObjectListDataField = ZHObjectList.getDeclaredField("data");
        ZHObjectListDataField.setAccessible(true);

        rankFeedModule = classLoader.loadClass("com.zhihu.android.api.model.RankFeedModule");

        hotLoadMore = classLoader.loadClass("com.zhihu.android.app.feed.ui.holder.hot.HotLoadMore");

        rankFeed = classLoader.loadClass("com.zhihu.android.api.model.RankFeed");
        rankFeedContent = classLoader.loadClass("com.zhihu.android.api.model.RankFeedContent");

        linkArea = classLoader.loadClass("com.zhihu.android.api.model.RankFeedContent$LinkArea");


        rankFeed_targetField = rankFeed.getDeclaredField("target");
        rankFeed_targetField.setAccessible(true);

        rankFeedContent_linkAreaField = rankFeedContent.getDeclaredField("linkArea");
        rankFeedContent_linkAreaField.setAccessible(true);

        linkArea_urlField = linkArea.getDeclaredField("url");
        linkArea_urlField.setAccessible(true);

        response = classLoader.loadClass("retrofit2.Response");

        response_bodyField = Arrays.stream(response.getDeclaredFields())
                .filter(field -> field.getType() == Object.class).findFirst().get();
        response_bodyField.setAccessible(true);

        feedsHotListFragment2_processRankFeedList = Arrays.stream(feedsHotListFragment2.getDeclaredMethods())
                .filter(method -> method.getReturnType() == response)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == response).findFirst().get();

        rankFeedList_displayNumField = rankFeedList.getDeclaredField("display_num");
        rankFeedList_displayNumField.setAccessible(true);
    }


    @Override
    public void hook() throws Throwable {

        XposedBridge.hookAllMethods(feedsHotListFragment2, "postRefreshSucceed", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }

                List<?> rankListData = (List<?>) ZHObjectListDataField.get(param.args[0]);
                if (rankListData == null || rankListData.isEmpty()) {
                    return;
                }

                rankListData.removeIf(hot -> hot.getClass() == rankFeedModule);
            }

        });

        XposedBridge.hookMethod(feedsHotListFragment2_processRankFeedList, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }

                Object rankFeedList = response_bodyField.get(param.args[0]);
                if (rankFeedList == null) {
                    return;
                }
                List<?> rankFeedListData = (List<?>) ZHObjectListDataField.get(rankFeedList);
                if (rankFeedListData == null || rankFeedListData.isEmpty()) {
                    return;
                }

                rankFeedListData.removeIf(HotListFilter::shouldRemove);

                // 热榜全部展示, 不折叠
                rankFeedList_displayNumField.set(rankFeedList, rankFeedListData.size());

            }
        });

    }

    private static boolean shouldRemove(Object rankFeedInstance) {
        return isAd(rankFeedInstance);
    }

    private static boolean isAd(Object rankFeedInstance) {
        if (rankFeedInstance.getClass() == rankFeed) {
            try {
                Object target = rankFeed_targetField.get(rankFeedInstance);
                Object linkAreaInstance = rankFeedContent_linkAreaField.get(target);

                String url = Optional.ofNullable((String) linkArea_urlField.get(linkAreaInstance)).orElse("");

                return !QUESTION_URL_PATTERN.matcher(url).find();

            } catch (Exception ignore) {
            }
        }

        return false;
    }
}