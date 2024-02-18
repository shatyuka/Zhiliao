package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.Helper.JsonNodeOp;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    static Method feedsHotListFragment2_rankFeedListAutoPage;

    static Field rankFeedList_displayNumField;

    static Class<?> templateCardModel;

    static Field templateCardModel_dataField;

    static Class<?> basePagingFragment;

    static final Pattern QUESTION_URL_PATTERN = Pattern.compile("zhihu\\.com/question/");

    @Override
    public String getName() {
        return "热榜和底部推荐过滤";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        basePagingFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment");
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

        List<Method> retArgTypeQqResponseMethodList = Arrays.stream(feedsHotListFragment2.getDeclaredMethods())
                .filter(method -> method.getReturnType() == response)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == response).collect(Collectors.toList());
        feedsHotListFragment2_rankFeedListAutoPage = retArgTypeQqResponseMethodList.get(retArgTypeQqResponseMethodList.size() - 1);


        rankFeedList_displayNumField = rankFeedList.getDeclaredField("display_num");
        rankFeedList_displayNumField.setAccessible(true);

        templateCardModel = classLoader.loadClass("com.zhihu.android.bean.TemplateCardModel");
        templateCardModel_dataField = templateCardModel.getField("data");
        templateCardModel_dataField.setAccessible(true);

    }


    @Override
    public void hook() throws Throwable {

        XposedBridge.hookAllMethods(feedsHotListFragment2, "postRefreshSucceed", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }

                filterRankFeed(param.args[0]);

            }

        });

        XposedBridge.hookAllMethods(basePagingFragment, "postLoadMoreSucceed", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return;
                }

                if (param.thisObject.getClass() == feedsHotListFragment2) {
                    filterRankFeed(param.args[0]);
                }

            }

        });

        XposedBridge.hookMethod(feedsHotListFragment2_rankFeedListAutoPage, new XC_MethodHook() {
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

                // 热榜全部展示, 不折叠
                rankFeedList_displayNumField.set(rankFeedList, rankFeedListData.size());
            }
        });

    }

    private void filterRankFeed(Object rankFeedListInstance) throws IllegalAccessException {
        if (rankFeedListInstance == null) {
            return;
        }
        List<?> rankListData = (List<?>) ZHObjectListDataField.get(rankFeedListInstance);
        if (rankListData == null || rankListData.isEmpty()) {
            return;
        }

        rankListData.removeIf(feed -> {
            try {
                return preFilter(feed) || isAd(feed) || shouldFilterEveryoneSeeRankFeed(feed);
            } catch (Exception e) {
                XposedBridge.log(e);
                return false;
            }
        });

    }

    private boolean preFilter(Object rankFeedInstance) {
        return rankFeedInstance == null || rankFeedInstance.getClass() == rankFeedModule;
    }

    @SuppressWarnings("all")
    private boolean shouldFilterEveryoneSeeRankFeed(Object rankFeedInstance) throws IllegalAccessException, InvocationTargetException {
        if (rankFeedInstance == null || rankFeedInstance.getClass() != templateCardModel) {
            return false;
        }

        Object data = templateCardModel_dataField.get(rankFeedInstance);
        Object target = JsonNodeOp.JsonNode_get.invoke(data, "target");

        if (Helper.regex_title != null) {
            String title = JsonNodeOp.JsonNode_get.invoke(JsonNodeOp.JsonNode_get.invoke(target, "title_area"), "text").toString();
            if (Helper.regex_title.matcher(title).find()) {
                return true;
            }
        }

        if (Helper.regex_author != null) {
            String author = JsonNodeOp.JsonNode_get.invoke(JsonNodeOp.JsonNode_get.invoke(target, "author_area"), "name").toString();
            if (Helper.regex_author.matcher(author).find()) {
                return true;
            }
        }

        if (Helper.regex_content != null) {
            // not full content
            String excerpt = (String) JsonNodeOp.JsonNode_get.invoke(JsonNodeOp.JsonNode_get.invoke(target, "excerpt_area"), "text").toString();
            if (Helper.regex_content.matcher(excerpt).find()) {
                return true;
            }
        }

        return false;
    }

    private boolean isAd(Object rankFeedInstance) {
        if (rankFeedInstance.getClass() == rankFeed) {
            try {
                Object target = rankFeed_targetField.get(rankFeedInstance);
                Object linkAreaInstance = rankFeedContent_linkAreaField.get(target);

                String url = Optional.ofNullable((String) linkArea_urlField.get(linkAreaInstance)).orElse("");

                return !QUESTION_URL_PATTERN.matcher(url).find();

            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }

        return false;
    }
}