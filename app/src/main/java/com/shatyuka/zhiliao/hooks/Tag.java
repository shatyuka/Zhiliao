package com.shatyuka.zhiliao.hooks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.MainHook;
import com.shatyuka.zhiliao.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class Tag implements IHook {
    static Drawable[] backgrounds;

    static Class<?> BaseTemplateNewFeedHolder;
    static Class<?> TemplateFeed;
    static Class<?> ViewHolder;
    static Class<?> SugarHolder;
    static Class<?> TemplateRoot;

    static Method onBindData;

    static Field ViewHolder_itemView;
    static Field SugarHolder_mData;
    static Field TemplateRoot_unique;

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

        try {
            onBindData = BaseTemplateNewFeedHolder.getMethod("onBindData", Object.class);
        } catch (NoSuchMethodException e) {
            onBindData = BaseTemplateNewFeedHolder.getDeclaredMethod("a", TemplateFeed);
        }
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_tag", false)) {

            XposedBridge.hookMethod(onBindData, new XC_MethodHook() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thisObject = param.thisObject;
                    ViewGroup viewGroup = (ViewGroup) ViewHolder_itemView.get(thisObject);
                    if (viewGroup == null) {
                        return;
                    }
                    TextView title = viewGroup.findViewById(Helper.context.getResources().getIdentifier("title", "id", MainHook.hookPackage));

                    if (title != null) {
                        title.setText("　　 " + title.getText());

                        Object templateFeed = SugarHolder_mData.get(thisObject);
                        Object unique = TemplateRoot_unique.get(templateFeed);
                        String type = (String) Helper.DataUnique_type.get(unique);

                        RelativeLayout relativeLayout = new RelativeLayout(viewGroup.getContext());
                        relativeLayout.setY((float) (Helper.scale * 3 + 0.5));
                        relativeLayout.addView(buildTagView(viewGroup.getContext(), type));

                        ViewGroup parent = (ViewGroup) title.getParent();
                        View author = parent.getChildAt(1);

                        ViewGroup.MarginLayoutParams authorLayoutParams = (ViewGroup.MarginLayoutParams) author.getLayoutParams();
                        // tag对齐
                        if (authorLayoutParams.leftMargin != 0) {
                            relativeLayout.setX(authorLayoutParams.leftMargin);
                        }

                        parent.addView(relativeLayout);
                    }
                }
            });
        }
    }

    private View buildTagView(Context context, String type) {
        TextView tag = new TextView(context);
        tag.setTextColor(-1);
        tag.setText(getType(type));
        tag.setBackground(getBackground(type));

        return tag;
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
