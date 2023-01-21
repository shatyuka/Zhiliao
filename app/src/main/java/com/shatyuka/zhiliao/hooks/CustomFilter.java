package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class CustomFilter implements IHook {
    static Class<?> InnerDeserializer;
    static Class<?> ApiTemplateRoot;
    static Class<?> ApiFeedCard;
    static Class<?> MarketCard;
    static Class<?> ApiFeedContent;
    static Class<?> ApiText;
    static Class<?> ApiLine;
    static Class<?> ApiElement;

    static Field ApiTemplateRoot_extra;
    static Field ApiTemplateRoot_common_card;
    static Field ApiFeedCard_feed_content;
    static Field ApiFeedContent_title;
    static Field ApiFeedContent_content;
    static Field ApiFeedContent_sourceLine;
    static Field ApiFeedContent_video;
    static Field ApiText_panel_text;
    static Field ApiLine_elements;
    static Field ApiElement_text;

    @Override
    public String getName() {
        return "自定义过滤";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        InnerDeserializer = classLoader.loadClass("com.zhihu.android.api.util.ZHObjectRegistryCenter$InnerDeserializer");
        ApiTemplateRoot = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiTemplateRoot");
        ApiFeedCard = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiFeedCard");
        MarketCard = classLoader.loadClass("com.zhihu.android.api.model.MarketCard");
        ApiFeedContent = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiFeedContent");
        ApiText = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiText");
        ApiLine = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiLine");
        ApiElement = classLoader.loadClass("com.zhihu.android.api.model.template.api.ApiElement");

        ApiTemplateRoot_extra = ApiTemplateRoot.getField("extra");
        ApiTemplateRoot_common_card = ApiTemplateRoot.getField("common_card");
        ApiFeedCard_feed_content = ApiFeedCard.getField("feed_content");
        ApiFeedContent_title = ApiFeedContent.getField("title");
        ApiFeedContent_content = ApiFeedContent.getField("content");
        ApiFeedContent_sourceLine = ApiFeedContent.getField("sourceLine");
        ApiFeedContent_video = ApiFeedContent.getField("video");
        ApiText_panel_text = ApiText.getField("panel_text");
        ApiLine_elements = ApiLine.getField("elements");
        ApiElement_text = ApiElement.getField("text");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllMethods(InnerDeserializer, "deserialize", new XC_MethodHook() {
            @SuppressWarnings("rawtypes")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    Object result = param.getResult();
                    if (result == null)
                        return;
                    Class<?> resultClass = result.getClass();
                    if (result != null) {
                        if (resultClass == ApiTemplateRoot) {
                            String type = (String) Helper.DataUnique_type.get(ApiTemplateRoot_extra.get(result));
                            if (Helper.prefs.getBoolean("switch_video", false) && (type.equals("zvideo") || type.equals("drama"))) {
                                param.setResult(null);
                                return;
                            }
                            if (Helper.prefs.getBoolean("switch_removearticle", false) && ("article".equals(type) || "Post".equals(type))) {
                                param.setResult(null);
                                return;
                            }
                            if (Helper.prefs.getBoolean("switch_pin", false) && "pin".equals(type)) {
                                param.setResult(null);
                                return;
                            }
                            if (Helper.prefs.getBoolean("switch_feedad", true) && "SvipActivity".equals(type)) {
                                param.setResult(null);
                                return;
                            }
                            Object feed_content = ApiFeedCard_feed_content.get(ApiTemplateRoot_common_card.get(result));
                            if (feed_content == null)
                                return;
                            Object video = ApiFeedContent_video.get(feed_content);
                            if (Helper.prefs.getBoolean("switch_video", false) && video != null) {
                                param.setResult(null);
                            } else {
                                if (Helper.regex_title == null && Helper.regex_author == null && Helper.regex_content == null)
                                    return;
                                if (Helper.regex_title != null) {
                                    String title = (String) ApiText_panel_text.get(ApiFeedContent_title.get(feed_content));
                                    if (Helper.regex_title.matcher(title).find()) {
                                        param.setResult(null);
                                    }
                                }
                                if (Helper.regex_author != null) {
                                    Object sourceLine = ApiFeedContent_sourceLine.get(feed_content);
                                    List elements = (List) ApiLine_elements.get(sourceLine);
                                    String author = (String) ApiText_panel_text.get(ApiElement_text.get(elements.get(1)));
                                    if (Helper.regex_author.matcher(author).find()) {
                                        param.setResult(null);
                                    }
                                }
                                if (Helper.regex_content != null) {
                                    String content = (String) ApiText_panel_text.get(ApiFeedContent_content.get(feed_content));
                                    if (Helper.regex_content.matcher(content).find()) {
                                        param.setResult(null);
                                    }
                                }
                            }
                        } else if (resultClass == MarketCard) {
                            if (Helper.prefs.getBoolean("switch_marketcard", false)) {
                                param.setResult(null);
                            }
                        }
                    }
                }
            }
        });
    }
}
