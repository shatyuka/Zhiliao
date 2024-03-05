package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.luckypray.dexkit.query.matchers.MethodMatcher
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Arrays

class AutoRefresh : IHook {

    private lateinit var feedAutoRefreshManager_shouldRefresh: Method

    private lateinit var feedHotRefreshAbConfig_shouldRefresh: Method

    private lateinit var mainPageFragment_getFragment: Method

    override fun getName(): String {
        return "关闭首页自动刷新"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        feedAutoRefreshManager_shouldRefresh =
            findFeedAutoRefreshManagerShouldRefreshMethod(classLoader)
        feedHotRefreshAbConfig_shouldRefresh =
            findFeedHotRefreshAbConfigShouldRefreshMethod(classLoader)

        val mainPageFragment =
            classLoader.loadClass("com.zhihu.android.app.feed.explore.view.MainPageFragment")
        val fragment = classLoader.loadClass("androidx.fragment.app.Fragment")
        mainPageFragment_getFragment = Arrays.stream(mainPageFragment.getDeclaredMethods())
            .filter { method: Method -> Modifier.isFinal(method.modifiers) }
            .filter { method: Method -> method.returnType == fragment }
            .filter { method: Method -> method.parameterCount == 0 }.findFirst().get()
    }

    @Throws(Throwable::class)
    override fun hook() {
        XposedBridge.hookMethod(feedAutoRefreshManager_shouldRefresh, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                    && Helper.prefs.getBoolean("switch_autorefresh", true)
                ) {
                    param.args[0] = 0
                }
            }
        })
        XposedBridge.hookMethod(feedHotRefreshAbConfig_shouldRefresh, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                    && Helper.prefs.getBoolean("switch_autorefresh", true)
                ) {
                    param.setResult(false)
                }
            }
        })
        XposedBridge.hookMethod(mainPageFragment_getFragment, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                    && Helper.prefs.getBoolean("switch_autorefresh", true)
                ) {
                    param.setResult(null)
                }
            }
        })
    }

    @Throws(NoSuchMethodException::class)
    private fun findFeedHotRefreshAbConfigShouldRefreshMethod(classLoader: ClassLoader): Method {
        val matcher: MethodMatcher = MethodMatcher.create()
            .returnType(Boolean::class.javaPrimitiveType as Class<*>)
            .paramCount(1)
            .paramTypes(Long::class.javaPrimitiveType)

        val methodList = Helper.findMethodList(
            listOf("com.zhihu.android.app.feed.util"),
            null,
            matcher,
            classLoader
        )
        if (methodList.isEmpty()) {
            throw NoSuchMethodException("com.zhihu.android.app.feed.util.FeedHotRefreshAbConfig#shouldRefresh")
        }
        if (methodList.size > 1) {
            throw Exception("multi methods have bool(long)")
        }

        return methodList[0]
    }

    @Throws(NoSuchMethodException::class)
    private fun findFeedAutoRefreshManagerShouldRefreshMethod(classLoader: ClassLoader): Method {
        val matcher: MethodMatcher = MethodMatcher.create()
            .returnType(Void::class.javaPrimitiveType as Class<*>)
            .paramCount(4)
            .paramTypes(Long::class.javaPrimitiveType, Int::class.javaPrimitiveType, null, null)

        val methodList = Helper.findMethodList(
            listOf("com.zhihu.android.app.feed.util"),
            null,
            matcher,
            classLoader
        )
        if (methodList.isEmpty()) {
            throw NoSuchMethodException("com.zhihu.android.app.feed.util.FeedAutoRefreshManager#shouldRefresh")
        }
        if (methodList.size > 1) {
            throw Exception("multi methods have void(long,int,object,object)")
        }

        return methodList[0]
    }

}
