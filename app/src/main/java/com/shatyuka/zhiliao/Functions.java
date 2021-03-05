package com.shatyuka.zhiliao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Functions {
    final static boolean DEBUG_WEBVIEW = false;

    static boolean horizontal = false;

    static boolean init(final ClassLoader classLoader) {
        try {
            XposedBridge.hookMethod(Helper.isShowLaunchAd, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true))
                        param.setResult(false);
                }
            });
            XposedHelpers.findAndHookMethod(Helper.AdNetworkManager, "a", int.class, long.class, long.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true)) {
                        param.setResult("");
                    }
                }
            });

            XposedBridge.hookAllMethods(Helper.InnerDeserializer, "deserialize", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)) {
                        Object result = param.getResult();
                        if (result == null)
                            return;
                        Class<?> resultClass = result.getClass();
                        if (result != null) {
                            if (resultClass == Helper.ApiTemplateRoot) {
                                String type = (String) Helper.DataUnique_type.get(Helper.ApiTemplateRoot_extra.get(result));
                                if (Helper.prefs.getBoolean("switch_video", false) && (type.equals("zvideo") || type.equals("drama"))) {
                                    param.setResult(null);
                                } else {
                                    if (Helper.regex_title == null && Helper.regex_author == null && Helper.regex_content == null)
                                        return;
                                    Object feed_content = Helper.ApiFeedCard_feed_content.get(Helper.ApiTemplateRoot_common_card.get(result));
                                    if (feed_content == null)
                                        return;
                                    if (Helper.regex_title != null) {
                                        String title = (String) Helper.ApiText_panel_text.get(Helper.ApiFeedContent_title.get(feed_content));
                                        if (Helper.regex_title.matcher(title).find()) {
                                            param.setResult(null);
                                        }
                                    }
                                    if (Helper.regex_author != null) {
                                        Object sourceLine = Helper.ApiFeedContent_sourceLine.get(feed_content);
                                        List elements = (List) Helper.ApiLine_elements.get(sourceLine);
                                        String author = (String) Helper.ApiText_panel_text.get(Helper.ApiElement_text.get(elements.get(1)));
                                        if (Helper.regex_author.matcher(author).find()) {
                                            param.setResult(null);
                                        }
                                    }
                                    if (Helper.regex_content != null) {
                                        String content = (String) Helper.ApiText_panel_text.get(Helper.ApiFeedContent_content.get(feed_content));
                                        if (Helper.regex_content.matcher(content).find()) {
                                            param.setResult(null);
                                        }
                                    }
                                }
                            } else if (resultClass == Helper.MarketCard) {
                                if (Helper.prefs.getBoolean("switch_marketcard", false)) {
                                    param.setResult(null);
                                }
                            }
                        }
                    }
                }
            });

            XposedBridge.hookAllMethods(Helper.BasePagingFragment, "postRefreshSucceed", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        if (param.args[0] == null)
                            return;
                        List<?> list = (List<?>) Helper.FeedList_data.get(param.args[0]);
                        if (list == null || list.isEmpty())
                            return;
                        for (int i = list.size() - 1; i >= 0; i--) {
                            if (list.get(i).getClass() == Helper.FeedAdvert) {
                                list.remove(i);
                            }
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.BasePagingFragment, "insertDataRangeToList", int.class, List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        if (param.args[1] == null)
                            return;
                        List<?> list = (List<?>) param.args[1];
                        if (list.isEmpty())
                            return;
                        for (int i = list.size() - 1; i >= 0; i--) {
                            if (list.get(i).getClass() == Helper.FeedAdvert) {
                                list.remove(i);
                            }
                        }
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolve", Context.class, Helper.FeedAdvert, boolean.class, Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        param.setResult(false);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.Advert, "isSlidingWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        param.setResult(false);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Helper.Ad, "isFloatAdCard", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveAnswerAdParam", Context.class, "com.zhihu.android.api.model.AnswerListAd", Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                        param.setResult(false);
                    }
                }
            });
            if (Helper.AnswerListWrapper != null) {
                XposedHelpers.findAndHookMethod(Helper.AnswerListWrapper, "insertAdBrandToList", ArrayList.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                            param.setResult(null);
                        }
                    }
                });
            }

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveCommentAdParam", Context.class, "com.zhihu.android.api.model.CommentListAd", Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_commentad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedBridge.hookMethod(Helper.shouldInterceptRequest, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", false))
                        return;
                    WebResourceRequest request = (WebResourceRequest) param.args[1];
                    List<String> segments = request.getUrl().getPathSegments();
                    if (segments.size() > 2 && request.getMethod().equals("GET")
                            && ((Helper.prefs.getBoolean("switch_answerad", true) && (segments.get(segments.size() - 1).equals("recommendations") || (segments.get(2).equals("brand") && segments.get(segments.size() - 1).equals("card"))))
                            || (Helper.prefs.getBoolean("switch_club", false) && segments.get(segments.size() - 1).equals("bind_club"))
                            || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods"))
                            || (Helper.prefs.getBoolean("switch_article", false) && segments.get(segments.size() - 1).equals("recommendation")))) {
                        WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("null\n".getBytes()));
                        response.setStatusCodeAndReasonPhrase(200, "OK");
                        param.setResult(response);
                    }
                }
            });

            XposedBridge.hookMethod(Helper.showShareAd, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_sharead", true))
                        param.setResult(null);
                }
            });

            XposedBridge.hookAllConstructors(Helper.FeedsTabsTopEntranceManager, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_livebutton", false)) {
                        param.args[0] = new FrameLayout(((FrameLayout) param.args[0]).getContext());
                    }
                }
            });

            XposedHelpers.findAndHookMethod(java.io.File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file.getName().equals(".allowXposed")) {
                        param.setResult(true);
                    }
                }
            });

            if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_horizontal", false)) {
                XposedHelpers.findAndHookMethod(Helper.ActionSheetLayout, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
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
                                if (Math.abs(dx) > Helper.width && Math.abs(dy) < Helper.height) {
                                    for (Object callback : (List) Helper.ActionSheetLayout_callbackList.get(param.thisObject)) {
                                        if (callback.getClass() == Helper.NestChildScrollChange) {
                                            Helper.onNestChildScrollRelease.invoke(callback, dx, 5201314);
                                        }
                                    }
                                }
                                break;
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(Helper.VerticalPageTransformer, "transformPage", View.class, float.class, new XC_MethodReplacement() {
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
                XposedHelpers.findAndHookMethod(Helper.NestChildScrollChange, "onNestChildScrollRelease", float.class, int.class, new XC_MethodHook() {
                    XC_MethodHook.Unhook hook_isReadyPageTurning;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if ((int) param.args[1] == 5201314) {
                            hook_isReadyPageTurning = XposedBridge.hookMethod(Helper.isReadyPageTurning, XC_MethodReplacement.returnConstant(true));
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
                XposedHelpers.findAndHookMethod(Helper.NextBtnClickListener, "onClick", View.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        horizontal = false;
                    }
                });
                XposedHelpers.findAndHookMethod(Helper.AnswerContentView, "showNextAnswer", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        horizontal = false;
                    }
                });
            }

            if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_nextanswer", false)) {
                XposedHelpers.findAndHookMethod(Helper.AnswerPagerFragment, "setupNextAnswerBtn", XC_MethodReplacement.returnConstant(null));
            }

            if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_reddot", false)) {
                XposedBridge.hookAllMethods(Helper.FeedsTabsFragment, "onUnReadCountLoaded", XC_MethodReplacement.returnConstant(null));
                XposedBridge.hookAllMethods(Helper.FeedFollowAvatarCommonViewHolder, "b", XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.ZHMainTabLayout, "d", XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.BottomNavMenuItemView, "a", int.class, XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.BottomNavMenuItemViewForIconOnly, "a", int.class, XC_MethodReplacement.returnConstant(null));
                if (Helper.NotiUnreadCountKt != null)
                    XposedHelpers.findAndHookMethod(Helper.NotiUnreadCountKt, "hasUnread", int.class, XC_MethodReplacement.returnConstant(false));
                XposedHelpers.findAndHookMethod(Helper.NotiMsgModel, "getUnreadCount", XC_MethodReplacement.returnConstant(0));
            }

            XposedBridge.hookMethod(Helper.isLinkZhihuWrap, new XC_MethodHook() {
                XC_MethodHook.Unhook hook_isLinkZhihu;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && (Helper.prefs.getBoolean("switch_externlink", false) || Helper.prefs.getBoolean("switch_externlinkex", false))) {
                        String url = (String) param.args[2];
                        if (url.startsWith("https://link.zhihu.com/?target=")) {
                            param.args[2] = URLDecoder.decode(url.substring(31), "utf-8");
                            if (Helper.prefs.getBoolean("switch_externlinkex", false)) {
                                android.util.Log.d("Zhiliao", (String) param.args[2]);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) param.args[2]));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Helper.context.startActivity(intent);
                                param.setResult(true);
                            } else {
                                hook_isLinkZhihu = XposedBridge.hookMethod(Helper.isLinkZhihu, XC_MethodReplacement.returnConstant(true));
                            }
                        }
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (hook_isLinkZhihu != null)
                        hook_isLinkZhihu.unhook();
                }
            });

            if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_vipbanner", false)) {
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "a", Context.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        XmlResourceParser layout_vipentranceview = Helper.modRes.getLayout(R.layout.layout_vipentranceview);
                        LayoutInflater.from((Context) param.args[0]).inflate(layout_vipentranceview, (ViewGroup) param.thisObject);
                        return null;
                    }
                });
                for (Method method : Helper.VipEntranceView.getMethods()) {
                    if (method.getName().equals("setData")) {
                        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(null));
                        break;
                    }
                }
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "onClick", View.class, XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "resetStyle", XC_MethodReplacement.returnConstant(null));
            }

            if (Helper.prefs.getBoolean("switch_mainswitch", false) && (Helper.prefs.getBoolean("switch_vipnav", false) || Helper.prefs.getBoolean("switch_videonav", false))) {
                XposedHelpers.findAndHookMethod(Helper.BottomNavMenuView, "a", Helper.IMenuItem, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (("market".equals(Helper.getMenuName.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_vipnav", false)) ||
                                ("video".equals(Helper.getMenuName.invoke(param.args[0])) && Helper.prefs.getBoolean("switch_videonav", false))) {
                            ((View) Helper.Tab_tabView.get(param.getResult())).setVisibility(View.GONE);
                        }
                    }
                });
            }

            XposedBridge.hookAllMethods(Helper.InternalNotificationManager, "fetchFloatNotification", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_hotbanner", false)) {
                        param.setResult(null);
                    }
                }
            });

            if (Build.VERSION.SDK_INT >= 26) {
                XposedHelpers.findAndHookMethod(Window.class, "setColorMode", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_colormode", false)) {
                            param.setResult(null);
                        }
                    }
                });
            }

            if (Helper.packageInfo.versionCode > 2614) { // after 6.61.0
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_nextanswer", false)) {
                    XposedHelpers.findAndHookMethod(ViewGroup.class, "addView", View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (param.args[0].getClass() == Helper.NextContentAnimationView)
                                ((View) param.args[0]).setVisibility(View.GONE);
                        }
                    });
                }
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_article", false)) {
                    XposedBridge.hookMethod(Helper.getItemCount, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject.getClass() == Helper.ContentMixAdapter && Helper.ContentMixPagerFragment_type.get(Helper.ContentMixAdapter_fragment.get(param.thisObject)) == "article")
                                param.setResult(1);
                        }
                    });
                }
            }

            if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_tag", false)) {
                XposedHelpers.findAndHookMethod(Helper.BaseTemplateNewFeedHolder, "a", Helper.TemplateFeed, new XC_MethodHook() {
                    @SuppressLint("ResourceType")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object thisObject = param.thisObject;
                        ViewGroup view = (ViewGroup) Helper.ViewHolder_itemView.get(thisObject);
                        TextView title = view.findViewById(Helper.id_title);
                        if (title != null) {
                            Object templateFeed = Helper.SugarHolder_mData.get(thisObject);
                            Object unique = Helper.TemplateRoot_unique.get(templateFeed);
                            String type = (String) Helper.DataUnique_type.get(unique);

                            float density = Helper.context.getResources().getDisplayMetrics().density;

                            TextView tag = view.findViewById(0xABCDEF);
                            if (tag == null) {
                                RelativeLayout relativeLayout = new RelativeLayout(view.getContext());
                                tag = new TextView(view.getContext());
                                tag.setId(0xABCDEF);
                                tag.setTextColor(-1);
                                relativeLayout.addView(tag);
                                relativeLayout.setY((int) (density * 5));
                                ((ViewGroup) title.getParent()).addView(relativeLayout);
                            }
                            if (tag.getText() != getType(type)) {
                                tag.setText(getType(type));
                                tag.setBackground(getBackground(type));
                            }

                            /* TODO: Fix this
                            SpannableString spannableString = new SpannableString(title.getText());
                            LeadingMarginSpan.Standard what = new LeadingMarginSpan.Standard(tag.getWidth() + (int) (density * 5 + 0.5), 0);
                            spannableString.setSpan(what, 0, spannableString.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
                            title.setText(spannableString);
                            */
                            if (title.getText().length() < 3 || title.getText().subSequence(0, 3) != "　　 ")
                                title.setText("　　 " + title.getText());
                        }
                    }
                });
            }

            XposedBridge.hookMethod(Helper.convert, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_searchad", true)) {
                        Object result = param.getResult();
                        if (result != null) {
                            switch (result.getClass().getName()) {
                                case "com.zhihu.android.api.model.SearchTopTabsItemList": {
                                    Helper.SearchTopTabsItemList_commercialData.set(result, null);
                                    break;
                                }
                                case "com.zhihu.android.api.model.PresetWords": {
                                    Helper.PresetWords_preset.set(result, null);
                                    break;
                                }
                            }
                            param.setResult(result);
                        }
                    }
                }
            });

            if (DEBUG_WEBVIEW) {
                XposedBridge.hookAllConstructors(WebView.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        XposedHelpers.callStaticMethod(WebView.class, "setWebContentsDebuggingEnabled", true);
                    }
                });
            }

            return true;
        } catch (Exception e) {
            XposedBridge.log("[Zhiliao] " + e.toString());
            return false;
        }
    }

    static String getType(String type) {
        switch (type) {
            case "answer":
                return "问题";
            case "article":
                return "文章";
            case "zvideo":
                return "视频";
            case "drama":
                return "直播";
            default:
                return "其他";
        }
    }

    static Drawable[] backgrounds;

    static Drawable getBackground(String type) {
        if (backgrounds == null) {
            backgrounds = new Drawable[3];
            backgrounds[0] = Helper.modRes.getDrawable(R.drawable.bg_answer);
            backgrounds[1] = Helper.modRes.getDrawable(R.drawable.bg_article);
            backgrounds[2] = Helper.modRes.getDrawable(R.drawable.bg_video);
        }
        switch (type) {
            case "answer":
                return backgrounds[0];
            case "article":
                return backgrounds[1];
            case "zvideo":
            case "drama":
                return backgrounds[2];
            default:
                return backgrounds[0];
        }
    }
}
