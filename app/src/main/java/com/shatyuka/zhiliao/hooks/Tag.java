package com.shatyuka.zhiliao.hooks;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shatyuka.zhiliao.Helper;
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

    private final int TAG_ID = 0xABCDEF;

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

                    TextView title = viewGroup.findViewById(Helper.context.getResources().getIdentifier("title", "id", Helper.hookPackage));
                    View author = viewGroup.findViewById(Helper.context.getResources().getIdentifier("author", "id", Helper.hookPackage));
                    if (title == null) {
                        return;
                    }

                    Object templateFeed = SugarHolder_mData.get(thisObject);
                    Object unique = TemplateRoot_unique.get(templateFeed);
                    String type = (String) Helper.DataUnique_type.get(unique);

                    RelativeLayout tagLayout;
                    TextView tag = viewGroup.findViewById(TAG_ID);
                    if (tag == null) {
                        tag = new TextView(viewGroup.getContext());
                        tag.setId(TAG_ID);

                        tagLayout = new RelativeLayout(viewGroup.getContext());
                        tagLayout.addView(tag);
                        ((ViewGroup) title.getParent()).addView(tagLayout);
                    } else {
                        tagLayout = (RelativeLayout) tag.getParent();
                    }

                    int baseX = ((ViewGroup.MarginLayoutParams) title.getLayoutParams()).leftMargin;
                    if (baseX != 0) {
                        ((ViewGroup.MarginLayoutParams) author.getLayoutParams()).leftMargin = baseX;
                    }

                    postProcessTag(tagLayout, tag, type, baseX, title.getVisibility() == View.VISIBLE);

                    // 有标题
                    if (title.getVisibility() == View.VISIBLE) {
                        title.setText("　　 " + title.getText());
                    } else {
                        ((ViewGroup.MarginLayoutParams) author.getLayoutParams()).leftMargin = (int) (Helper.scale * 40 + 0.5 + baseX);
                    }
                }
            });
        }
    }

    private void postProcessTag(RelativeLayout relativeLayout, TextView tag, String type, int baseX, boolean hasTitle) {
        tag.setTextColor(-1);
        tag.setText(getType(type));
        tag.setBackground(getBackground(type));

        if (baseX != 0) {
            relativeLayout.setX(baseX);
        }

        if (hasTitle) {
            relativeLayout.setY((float) (Helper.scale * 3 + 0.5));
        } else {
            relativeLayout.setY(0);
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
