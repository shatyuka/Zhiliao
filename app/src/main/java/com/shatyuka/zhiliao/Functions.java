package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URLDecoder;
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
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_launchad", true))
                        param.setResult(false);
                }
            });

            XposedBridge.hookAllMethods(Helper.InnerDeserializer, "deserialize", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true)) {
                        Object result = param.getResult();
                        if (result == null)
                            return;
                        Class<?> resultClass = result.getClass();
                        if (result != null) {
                            if (resultClass == Helper.ApiTemplateRoot) {
                                Object extra = Helper.ApiTemplateRoot.getField("extra").get(result);
                                String type = (String) Helper.DataUnique.getField("type").get(extra);
                                if (Helper.prefs.getBoolean("switch_video", false) && (type.equals("zvideo") || type.equals("drama"))) {
                                    param.setResult(null);
                                } else {
                                    if (Helper.regex_title == null && Helper.regex_author == null && Helper.regex_content == null)
                                        return;
                                    Object common_card = resultClass.getField("common_card").get(result);
                                    Object feed_content = common_card.getClass().getField("feed_content").get(common_card);
                                    if (feed_content == null)
                                        return;
                                    Object title = Helper.ApiFeedContent.getField("title").get(feed_content);
                                    Object content = Helper.ApiFeedContent.getField("content").get(feed_content);
                                    Object sourceLine = Helper.ApiFeedContent.getField("sourceLine").get(feed_content);
                                    List elements = (List) sourceLine.getClass().getField("elements").get(sourceLine);
                                    Object author = elements.get(1).getClass().getField("text").get(elements.get(1));
                                    String title_str = (String) Helper.panel_text.get(title);
                                    String author_str = (String) Helper.panel_text.get(author);
                                    String content_str = (String) Helper.panel_text.get(content);
                                    if ((Helper.regex_title != null && Helper.regex_title.matcher(title_str).find()) ||
                                            (Helper.regex_author != null && Helper.regex_author.matcher(author_str).find()) ||
                                            (Helper.regex_content != null && Helper.regex_content.matcher(content_str).find())) {
                                        param.setResult(null);
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

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolve", Context.class, "com.zhihu.android.api.model.FeedAdvert", boolean.class, Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_feedad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveAnswerAdParam", Context.class, "com.zhihu.android.api.model.AnswerListAd", Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_answerlistad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.MorphAdHelper, "resolveCommentAdParam", Context.class, "com.zhihu.android.api.model.CommentListAd", Boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_commentad", true)) {
                        param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Helper.BaseAppView, "a", Helper.IZhihuWebView, WebResourceRequest.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!Helper.prefs.getBoolean("switch_mainswitch", true))
                        return;
                    WebResourceRequest request = (WebResourceRequest) param.args[1];
                    List<String> segments = request.getUrl().getPathSegments();
                    if (segments.size() > 2 && request.getMethod().equals("GET")
                            && ((Helper.prefs.getBoolean("switch_answerad", true) && segments.get(segments.size() - 1).equals("recommendations"))
                            || (Helper.prefs.getBoolean("switch_club", false) && segments.get(segments.size() - 1).equals("bind_club"))
                            || (Helper.prefs.getBoolean("switch_goods", false) && segments.get(segments.size() - 2).equals("goods")))) {
                        WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream("null\n".getBytes()));
                        response.setStatusCodeAndReasonPhrase(200, "OK");
                        param.setResult(response);
                    }
                }
            });

            XposedBridge.hookMethod(Helper.showShareAd, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_sharead", true))
                        param.setResult(null);
                }
            });

            XposedBridge.hookAllConstructors(Helper.FeedsTabsTopEntranceManager, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_livebutton", false)) {
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

            if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_horizontal", false)) {
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
                                if (Math.abs(dx) > 300 && Math.abs(dy) < 100) {
                                    Field List = param.thisObject.getClass().getDeclaredField("z");
                                    List.setAccessible(true);
                                    for (Object callback : (List) List.get(param.thisObject)) {
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
                        if (horizontal) {
                            if (position < -1) {
                                view.setAlpha(0);
                            } else if (position <= 1) {
                                view.setAlpha(1);
                                view.setTranslationX(0);
                                view.setTranslationY(0);
                            } else {
                                view.setAlpha(0);
                            }
                        } else {
                            int width = view.getWidth();
                            int height = view.getHeight();
                            if (position < -1) {
                                view.setAlpha(0);
                            } else if (position <= 1) {
                                view.setAlpha(1);
                                view.setTranslationX(width * -position);
                                view.setTranslationY(height * position);
                            } else {
                                view.setAlpha(0);
                            }
                        }
                        return null;
                    }
                });
                XposedHelpers.findAndHookMethod(Helper.NestChildScrollChange, "onNestChildScrollRelease", float.class, int.class, new XC_MethodHook() {
                    XC_MethodHook.Unhook hook_isReadyPageTurning;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if ((int) param.args[1] == 5201314) {
                            hook_isReadyPageTurning = XposedHelpers.findAndHookMethod(Helper.DirectionBoundView, "isReadyPageTurning", XC_MethodReplacement.returnConstant(true));
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

            if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_nextanswer", false)) {
                XposedHelpers.findAndHookMethod(Helper.AnswerPagerFragment, "setupNextAnswerBtn", XC_MethodReplacement.returnConstant(null));
            }

            if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_reddot", false)) {
                XposedBridge.hookAllMethods(Helper.FeedsTabsFragment, "onUnReadCountLoaded", XC_MethodReplacement.returnConstant(null));
                XposedBridge.hookAllMethods(Helper.FeedFollowAvatarCommonViewHolder, "b", XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.ZHMainTabLayout, "d", XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.BottomNavMenuItemView, "a", int.class, XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.BottomNavMenuItemViewForIconOnly, "a", int.class, XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.NotiUnreadCountKt, "hasUnread", int.class, XC_MethodReplacement.returnConstant(false));
                XposedHelpers.findAndHookMethod(Helper.NotiMsgModel, "getUnreadCount", XC_MethodReplacement.returnConstant(0));
            }

            XposedHelpers.findAndHookMethod(Helper.LinkZhihuHelper, "a", "com.zhihu.android.app.mercury.api.c", "com.zhihu.android.app.mercury.api.IZhihuWebView", String.class, new XC_MethodHook() {
                XC_MethodHook.Unhook hook_isLinkZhihu;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_externlink", false)) {
                        String url = (String) param.args[2];
                        if (url.startsWith("https://link.zhihu.com/?target=")) {
                            param.args[2] = URLDecoder.decode(url.substring(31), "utf-8");
                            hook_isLinkZhihu = XposedBridge.hookMethod(Helper.isLinkZhihu, XC_MethodReplacement.returnConstant(true));
                        }
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (hook_isLinkZhihu != null)
                        hook_isLinkZhihu.unhook();
                }
            });

            if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_vipbanner", false)) {
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "a", Context.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        XmlResourceParser layout_vipentranceview = Helper.modRes.getLayout(R.layout.layout_vipentranceview);
                        LayoutInflater.from((Context) param.args[0]).inflate(layout_vipentranceview, (ViewGroup) param.thisObject);
                        return null;
                    }
                });
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "setData", "com.zhihu.android.api.model.VipInfo", XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "onClick", View.class, XC_MethodReplacement.returnConstant(null));
                XposedHelpers.findAndHookMethod(Helper.VipEntranceView, "resetStyle", XC_MethodReplacement.returnConstant(null));
            }

            if (Helper.prefs.getBoolean("switch_mainswitch", true) && Helper.prefs.getBoolean("switch_vipnav", false)) {
                XposedHelpers.findAndHookMethod(Helper.BottomNavDelegation, "r", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ((List) Helper.navList.get(param.thisObject)).remove(1);
                    }
                });
            }

            if (DEBUG_WEBVIEW) {
                XposedBridge.hookAllConstructors(WebView.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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
}
