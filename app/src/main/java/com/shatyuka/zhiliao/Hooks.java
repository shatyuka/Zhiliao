package com.shatyuka.zhiliao;

import android.widget.Toast;

import com.shatyuka.zhiliao.hooks.AnswerAd;
import com.shatyuka.zhiliao.hooks.AnswerListAd;
import com.shatyuka.zhiliao.hooks.Article;
import com.shatyuka.zhiliao.hooks.ColorMode;
import com.shatyuka.zhiliao.hooks.CommentAd;
import com.shatyuka.zhiliao.hooks.CustomFilter;
import com.shatyuka.zhiliao.hooks.ExternLink;
import com.shatyuka.zhiliao.hooks.FeedAd;
import com.shatyuka.zhiliao.hooks.Horizontal;
import com.shatyuka.zhiliao.hooks.HotBanner;
import com.shatyuka.zhiliao.hooks.IHook;
import com.shatyuka.zhiliao.hooks.LaunchAd;
import com.shatyuka.zhiliao.hooks.LiveButton;
import com.shatyuka.zhiliao.hooks.NavButton;
import com.shatyuka.zhiliao.hooks.NextAnswer;
import com.shatyuka.zhiliao.hooks.RedDot;
import com.shatyuka.zhiliao.hooks.SearchAd;
import com.shatyuka.zhiliao.hooks.ShareAd;
import com.shatyuka.zhiliao.hooks.StatusBar;
import com.shatyuka.zhiliao.hooks.Tag;
import com.shatyuka.zhiliao.hooks.VIPBanner;
import com.shatyuka.zhiliao.hooks.WebView;
import com.shatyuka.zhiliao.hooks.ZhihuPreference;

import de.robv.android.xposed.XposedBridge;

public class Hooks {
    static final Class<?>[] classes = {
            ZhihuPreference.class,
            LaunchAd.class,
            CustomFilter.class,
            FeedAd.class,
            AnswerListAd.class,
            CommentAd.class,
            AnswerAd.class,
            ShareAd.class,
            LiveButton.class,
            Horizontal.class,
            NextAnswer.class,
            RedDot.class,
            ExternLink.class,
            VIPBanner.class,
            NavButton.class,
            HotBanner.class,
            ColorMode.class,
            Article.class,
            Tag.class,
            SearchAd.class,
            StatusBar.class
            //,WebView.class
    };

    public static void init(final ClassLoader classLoader) {
        for (Class<?> clazz : classes) {
            String hookName = "";
            try {
                IHook hook = ((Class<IHook>)clazz).newInstance();
                hookName = hook.getName();
                hook.init(classLoader);
                hook.hook();
            } catch (Throwable e) {
                Toast.makeText(Helper.context,  hookName + "功能加载失败", Toast.LENGTH_SHORT).show();
                XposedBridge.log("[Zhiliao] " + e.toString());
            }
        }
    }
}
