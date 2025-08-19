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
    static Class<?> Card;
    static Class<?> TextElement;
    static Class<?> Line;
    static Class<?> Avatar;
    static Class<?> Video;

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
    static Field Card_elements;
    static Field Card_extra;
    static Field Card_Extra_contentType;
    static Field Element_id;
    static Field Line_elements;
    static Field TextElement_text;

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
        try {
            Card = classLoader.loadClass("com.zhihu.android.ui.shared.sdui.model.Card");
        } catch (ClassNotFoundException ignored) {
        }
        if (Card != null) {
            Card_elements = Card.getDeclaredField("elements");
            Card_elements.setAccessible(true);
            Card_extra = Card.getDeclaredField("extra");
            Card_extra.setAccessible(true);
            Card_Extra_contentType = Card_extra.getType().getDeclaredField("contentType");
            Card_Extra_contentType.setAccessible(true);

            Class<?> Element = classLoader.loadClass("com.zhihu.android.ui.shared.sdui.model.Element");
            Element_id = Element.getDeclaredField("id");
            Element_id.setAccessible(true);
            TextElement = classLoader.loadClass("com.zhihu.android.ui.shared.sdui.model.TextElement");
            TextElement_text = TextElement.getDeclaredField("text");
            TextElement_text.setAccessible(true);
            Line = classLoader.loadClass("com.zhihu.android.ui.shared.sdui.model.Line");
            Line_elements = Line.getDeclaredField("elements");
            Line_elements.setAccessible(true);
            Avatar = classLoader.loadClass("com.zhihu.android.ui.shared.sdui.model.Avatar");
            Video = classLoader.loadClass("com.zhihu.android.ui.shared.sdui.model.Video");
        }

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
                            boolean hasVideo = false;
                            String title = "";
                            String author = "";
                            String content = "";
                            Object feed_content = ApiFeedCard_feed_content.get(ApiTemplateRoot_common_card.get(result));
                            if (feed_content != null) {
                                hasVideo = ApiFeedContent_video.get(feed_content) != null;

                                title = (String) ApiText_panel_text.get(ApiFeedContent_title.get(feed_content));

                                Object sourceLine = ApiFeedContent_sourceLine.get(feed_content);
                                List elements = (List) ApiLine_elements.get(sourceLine);
                                author = (String) ApiText_panel_text.get(ApiElement_text.get(elements.get(1)));

                                content = (String) ApiText_panel_text.get(ApiFeedContent_content.get(feed_content));
                            }

                            if (filter(type, hasVideo, title, author, content)) {
                                param.setResult(null);
                            }
                        } else if (resultClass == Card) {
                            Object extra = Card_extra.get(result);
                            String type = (String) Card_Extra_contentType.get(extra);
                            boolean hasVideo = false;
                            String title = null;
                            String author = null;
                            String content = null;
                            List<?> elements = (List<?>) Card_elements.get(result);
                            for (Object element : elements) {
                                String id = (String) Element_id.get(element);
                                if (id == null) {
                                    continue;
                                }
                                if (title == null && author == null && id.equals("Text")) {
                                    title = (String) TextElement_text.get(element);
                                    continue;
                                }
                                if (author == null && content == null && element.getClass() == Line && id.equals("0")) {
                                    List<?> lineElements = (List<?>) Line_elements.get(element);
                                    if (!lineElements.isEmpty()) {
                                        Object avatarElement = lineElements.get(0);
                                        if (avatarElement.getClass() == Avatar) {
                                            for (Object lineElement : lineElements) {
                                                if (lineElement.getClass() == TextElement) {
                                                    author = (String) TextElement_text.get(lineElement);
                                                    break;
                                                }
                                            }
                                            continue;
                                        }
                                    }
                                }
                                if (content == null && id.endsWith("_summary")) {
                                    content = (String) TextElement_text.get(element);
                                }
                                if (element.getClass() == Video) {
                                    hasVideo = true;
                                }
                            }

                            if (filter(type, hasVideo, title, author, content)) {
                                param.setResult(null);
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

    private boolean filter(String type, boolean hasVideo, String title, String author, String content) {
        if (Helper.prefs.getBoolean("switch_video", false) && (type.equals("zvideo") || type.equals("drama") || hasVideo)) {
            return true;
        }
        if (Helper.prefs.getBoolean("switch_removearticle", false) && ("article".equals(type) || "Post".equals(type))) {
            return true;
        }
        if (Helper.prefs.getBoolean("switch_pin", false) && "pin".equals(type)) {
            return true;
        }
        if (Helper.prefs.getBoolean("switch_feedad", true) && "SvipActivity".equals(type)) {
            return true;
        }

        if (Helper.regex_title != null && title != null && Helper.regex_title.matcher(title).find()) {
            return true;
        }
        if (Helper.regex_author != null && author != null && Helper.regex_author.matcher(author).find()) {
            return true;
        }
        if (Helper.regex_content != null && content != null && Helper.regex_content.matcher(content).find()) {
            return true;
        }

        return false;
    }
}
