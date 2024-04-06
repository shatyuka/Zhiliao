package com.shatyuka.zhiliao;

import com.shatyuka.zhiliao.hooks.AnswerAd;
import com.shatyuka.zhiliao.hooks.AnswerListAd;
import com.shatyuka.zhiliao.hooks.Article;
import com.shatyuka.zhiliao.hooks.CustomFilter;
import com.shatyuka.zhiliao.hooks.ExternLink;
import com.shatyuka.zhiliao.hooks.FeedAd;
import com.shatyuka.zhiliao.hooks.FeedTopHotBanner;
import com.shatyuka.zhiliao.hooks.FollowButton;
import com.shatyuka.zhiliao.hooks.FullScreen;
import com.shatyuka.zhiliao.hooks.HeadZoneBanner;
import com.shatyuka.zhiliao.hooks.Horizontal;
import com.shatyuka.zhiliao.hooks.HotBanner;
import com.shatyuka.zhiliao.hooks.IHook;
import com.shatyuka.zhiliao.hooks.LaunchAd;
import com.shatyuka.zhiliao.hooks.LiveButton;
import com.shatyuka.zhiliao.hooks.MineHybridView;
import com.shatyuka.zhiliao.hooks.NavButton;
import com.shatyuka.zhiliao.hooks.NavRes;
import com.shatyuka.zhiliao.hooks.NextAnswer;
import com.shatyuka.zhiliao.hooks.RedDot;
import com.shatyuka.zhiliao.hooks.SearchAd;
import com.shatyuka.zhiliao.hooks.ShareAd;
import com.shatyuka.zhiliao.hooks.StatusBar;
import com.shatyuka.zhiliao.hooks.Tag;
import com.shatyuka.zhiliao.hooks.ThirdPartyLogin;
import com.shatyuka.zhiliao.hooks.VIPBanner;
import com.shatyuka.zhiliao.hooks.ZhihuPreference;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

import static org.junit.Assert.fail;

public class HookTest {
    static class PackageInfo {
        String name;
        int versionCode;
        ClassLoader classLoader;
    }

    static LinkedList<PackageInfo> packageInfos = new LinkedList<>();

    static {
        System.out.println((new File("")).getAbsolutePath());
        File path = new File("test");
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                int index1 = fileName.lastIndexOf(".");
                int index2 = fileName.lastIndexOf(" ");
                if (!"jar".equalsIgnoreCase(fileName.substring(index1 + 1)))
                    continue;
                try {
                    PackageInfo packageInfo = new PackageInfo();
                    packageInfo.name = fileName.substring(0, index2);
                    packageInfo.versionCode = Integer.parseInt(fileName.substring(index2 + 1, index1));
                    packageInfo.classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
                    packageInfos.add(packageInfo);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void checkHook(IHook hook) {
        for (PackageInfo packageInfo : packageInfos) {
            try {
                Helper.packageInfo = new android.content.pm.PackageInfo();
                Helper.packageInfo.versionCode = packageInfo.versionCode;
                Helper.init_class(packageInfo.classLoader);
                hook.init(packageInfo.classLoader);
            } catch (Throwable e) {
                fail(hook.getName() + ", " + packageInfo.name);
            }
        }
    }

    @Test
    public void zhihuPreferenceTest() {
        checkHook(new ZhihuPreference());
    }

    @Test
    public void launchAdTest() {
        checkHook(new LaunchAd());
    }

    @Test
    public void customFilterTest() {
        checkHook(new CustomFilter());
    }

    @Test
    public void feedAdTest() {
        checkHook(new FeedAd());
    }

    @Test
    public void answerListAdTest() {
        checkHook(new AnswerListAd());
    }

    @Test
    public void answerAdTest() {
        checkHook(new AnswerAd());
    }

    @Test
    public void shareAdTest() {
        checkHook(new ShareAd());
    }

    @Test
    public void liveButtonTest() {
        checkHook(new LiveButton());
    }

    @Test
    public void horizontalTest() {
        checkHook(new Horizontal());
    }

    @Test
    public void nextAnswerTest() {
        checkHook(new NextAnswer());
    }

    @Test
    public void redDotTest() {
        checkHook(new RedDot());
    }

    @Test
    public void externLinkTest() {
        checkHook(new ExternLink());
    }

    @Test
    public void vipBannerTest() {
        checkHook(new VIPBanner());
    }

    @Test
    public void navButtonTest() {
        checkHook(new NavButton());
    }

    @Test
    public void hotBannerTest() {
        checkHook(new HotBanner());
    }

    @Test
    public void articleTest() {
        checkHook(new Article());
    }

    @Test
    public void tagTest() {
        checkHook(new Tag());
    }

    @Test
    public void searchAdTest() {
        checkHook(new SearchAd());
    }

    @Test
    public void statusBarTest() {
        checkHook(new StatusBar());
    }

    @Test
    public void thirdPartyLoginTest() {
        checkHook(new ThirdPartyLogin());
    }

    @Test
    public void navResTest() {
        checkHook(new NavRes());
    }

    @Test
    public void feedTopHotBannerTest() {
        checkHook(new FeedTopHotBanner());
    }

    @Test
    public void headZoneBannerTest() {
        checkHook(new HeadZoneBanner());
    }

    @Test
    public void mineHybridViewTest() {
        checkHook(new MineHybridView());
    }

    @Test
    public void followButtonTest() {
        checkHook(new FollowButton());
    }

    @Test
    public void fullScreen() {
        checkHook(new FullScreen());
    }
}
