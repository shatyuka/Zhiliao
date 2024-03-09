package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.luckypray.dexkit.query.matchers.ClassMatcher
import java.lang.reflect.Method
import java.util.Arrays
import java.util.stream.Collectors

class PanelBubble : IHook {

    private lateinit var buildBubbleViewMethodList: List<Method>

    override fun getName(): String {
        return "关闭底部气泡通知"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        val bubbleViewNew = classLoader.loadClass("com.zhihu.android.panel.ui.bubble.BubbleViewNew")
        val bubbleView = classLoader.loadClass("com.zhihu.android.panel.ui.bubble.BubbleView")
        val panelBubbleUtilNew = findPanelBubbleUtilNewClass(classLoader, bubbleViewNew, bubbleView)
        buildBubbleViewMethodList = Arrays.stream(panelBubbleUtilNew.getDeclaredMethods())
            .filter { method: Method -> method.parameterCount == 1 }
            .filter { method: Method ->
                (method.returnType == bubbleView || method.returnType == bubbleViewNew)
            }.collect(Collectors.toList())

        if (buildBubbleViewMethodList.isEmpty()) {
            throw NoSuchMethodException("BubbleUtilNewClass#buildBubbleView")
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        for (buildBubbleViewMethod in buildBubbleViewMethodList) {
            XposedBridge.hookMethod(buildBubbleViewMethod, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)
                        && Helper.prefs.getBoolean("switch_panelbubble", true)
                    ) {
                        param.setResult(null)
                    }
                }
            })
        }
    }

    @Throws(ClassNotFoundException::class)
    private fun findPanelBubbleUtilNewClass(
        classLoader: ClassLoader,
        bubbleViewNew: Class<*>,
        bubbleView: Class<*>
    ): Class<*> {

        val matcher: ClassMatcher = ClassMatcher.create().methods {
            add {
                returnType = bubbleViewNew.name
                paramCount = 1
            }
            add {
                returnType = bubbleView.name
                paramCount = 1
            }
        }

        return Helper.findClass(
            listOf("com.zhihu.android.panel.ui.bubble"), null, matcher, classLoader
        ) ?: throw ClassNotFoundException("com.zhihu.android.panel.ui.bubble.BubbleUtilNew")
    }

}