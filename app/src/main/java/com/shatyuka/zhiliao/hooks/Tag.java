package com.shatyuka.zhiliao.hooks;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.MainHook;
import com.shatyuka.zhiliao.R;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class Tag implements IHook {
    static Drawable[] backgrounds;

    static Class<?> BaseTemplateNewFeedHolder;
    static Class<?> TemplateFeed;
    static Class<?> ViewHolder;
    static Class<?> SugarHolder;
    static Class<?> TemplateRoot;

    static Field ViewHolder_itemView;
    static Field SugarHolder_mData;
    static Field TemplateRoot_unique;

    static int id_title;
    static int id_author;

    @Override
    public String getName() {
        return "显示卡片类别";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        BaseTemplateNewFeedHolder = classLoader.loadClass("com.zhihu.android.app.feed.ui.holder.template.optimal.BaseTemplateNewFeedHolder");
        TemplateFeed = classLoader.loadClass("com.zhihu.android.api.model.template.TemplateFeed");
        ViewHolder = classLoader.loadClass("androidx.recyclerview.widget.RecyclerView$ViewHolder");
        SugarHolder = classLoader.loadClass("com.zhihu.android.sugaradapter.SugarHolder");
        TemplateRoot = classLoader.loadClass("com.zhihu.android.api.model.template.TemplateRoot");

        ViewHolder_itemView = ViewHolder.getField("itemView");
        try {
            SugarHolder_mData = SugarHolder.getDeclaredField("c");
        } catch (NoSuchFieldException e) {
            SugarHolder_mData = SugarHolder.getDeclaredField("mData");
        }
        SugarHolder_mData.setAccessible(true);
        TemplateRoot_unique = TemplateRoot.getField("unique");
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void hook() throws Throwable {
        id_title = Helper.context.getResources().getIdentifier("title", "id", MainHook.hookPackage);
        id_author = Helper.context.getResources().getIdentifier("author", "id", MainHook.hookPackage);

        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_tag", false)) {
            XposedHelpers.findAndHookMethod(BaseTemplateNewFeedHolder, "a", TemplateFeed, new XC_MethodHook() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    ViewGroup view = (ViewGroup) ViewHolder_itemView.get(thisObject);
                    if (view == null)
                        return;
                    TextView title = view.findViewById(id_title);
                    ViewGroup author = view.findViewById(id_author);
                    if (title != null && author != null) {
                        Object templateFeed = SugarHolder_mData.get(thisObject);
                        Object unique = TemplateRoot_unique.get(templateFeed);
                        String type = (String) Helper.DataUnique_type.get(unique);

                        TextView tag = view.findViewById(0xABCDEF);
                        if (tag == null) {
                            RelativeLayout relativeLayout = new RelativeLayout(view.getContext());
                            tag = new TextView(view.getContext());
                            tag.setId(0xABCDEF);
                            tag.setTextColor(-1);
                            relativeLayout.addView(tag);
                            relativeLayout.setY((int) (Helper.scale * 5 + 0.5));
                            ((ViewGroup) title.getParent()).addView(relativeLayout);
                        }
                        assert type != null;
                        if (tag.getText() != getType(type)) {
                            tag.setText(getType(type));
                            tag.setBackground(getBackground(type));
                        }

                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) author.getLayoutParams();
                        if (title.getVisibility() == View.VISIBLE) {
                            if (title.getText().length() < 3 || title.getText().subSequence(0, 3) != "　　 ") {
                                layoutParams.leftMargin = 0;
                                ((RelativeLayout) tag.getParent()).setY((int) (Helper.scale * 5 + 0.5));
                                title.setText("　　 " + title.getText());
                            }
                        } else {
                            layoutParams.leftMargin = (int) (Helper.scale * 40 + 0.5);
                            ((RelativeLayout) tag.getParent()).setY((int) (Helper.scale * 3 + 0.5));
                        }
                        author.setLayoutParams(layoutParams);
                    }
                }
            });
        }
    }

    static String getType(String type) {
        switch (type) {
            case "answer":
            case "Answer":
                return "问题";
            case "article":
            case "Post":
                return "文章";
            case "zvideo":
                return "视频";
            case "drama":
                return "直播";
            case "pin":
                return "想法";
            default:
                return "其他";
        }
    }

    @SuppressWarnings("deprecation")
    static Drawable getBackground(String type) {
        if (backgrounds == null) {
            backgrounds = new Drawable[5];
            backgrounds[0] = Helper.modRes.getDrawable(R.drawable.bg_answer);
            backgrounds[1] = Helper.modRes.getDrawable(R.drawable.bg_article);
            backgrounds[2] = Helper.modRes.getDrawable(R.drawable.bg_video);
            backgrounds[3] = Helper.modRes.getDrawable(R.drawable.bg_pin);
            backgrounds[4] = Helper.modRes.getDrawable(R.drawable.bg_others);
        }
        switch (type) {
            case "answer":
            case "Answer":
                return backgrounds[0];
            case "article":
            case "Post":
                return backgrounds[1];
            case "zvideo":
            case "drama":
                return backgrounds[2];
            case "pin":
                return backgrounds[3];
            default:
                return backgrounds[4];
        }
    }
}
