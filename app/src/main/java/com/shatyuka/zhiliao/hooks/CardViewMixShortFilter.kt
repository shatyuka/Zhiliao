package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import com.shatyuka.zhiliao.Helper.JsonNodeOp
import com.shatyuka.zhiliao.hooks.CardViewFeatureShortFilter.Companion.preProcessShortContent
import com.shatyuka.zhiliao.hooks.CardViewFeatureShortFilter.Companion.shouldRemoveShortContent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.luckypray.dexkit.query.matchers.ClassMatcher
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Arrays

class CardViewMixShortFilter : IHook {

    private lateinit var mixupDataParser_jsonNode2List: Method

    override fun getName(): String {
        return "卡片视图相关过滤"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        mixupDataParser_jsonNode2List = findJsonNode2List(classLoader)
    }

    @Throws(Throwable::class)
    override fun hook() {
        XposedBridge.hookMethod(mixupDataParser_jsonNode2List, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return
                }
                if (Helper.prefs.getBoolean("switch_feedad", false)) {
                    filterShortContent(param.args[0])
                }
            }
        })
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    fun filterShortContent(shortContentListJsonNode: Any?) {
        val dataJsonNode = JsonNodeOp.JsonNode_get.invoke(shortContentListJsonNode, "data")
        if (dataJsonNode == null || !(JsonNodeOp.JsonNode_isArray.invoke(dataJsonNode) as Boolean)) {
            return
        }
        val shortContentIterator =
            JsonNodeOp.JsonNode_iterator.invoke(dataJsonNode) as MutableIterator<*>
        while (shortContentIterator.hasNext()) {
            val shortContentJsonNode = shortContentIterator.next() ?: continue
            if (shouldRemoveShortContent(shortContentJsonNode)) {
                shortContentIterator.remove()
            } else {
                preProcessShortContent(shortContentJsonNode)
            }
        }
    }

    @Throws(NoSuchMethodException::class)
    private fun findJsonNode2List(classLoader: ClassLoader): Method {
        val classMatcher: ClassMatcher = ClassMatcher.create().methods {
            add {
                paramCount = 1
                returnType(List::class.java)
                paramTypes(JsonNodeOp.JsonNode)
                usingStrings("")
            }
            count(4..6)
        }

        val mixupDataParserClass = Helper.findClass(
            listOf("com.zhihu.android.mixshortcontainer"),
            listOf(
                "com.zhihu.android.mixshortcontainer.dataflow.model",
                "com.zhihu.android.mixshortcontainer.config",
                "com.zhihu.android.mixshortcontainer.consecutivescroll",
                "com.zhihu.android.mixshortcontainer.foundation",
                "com.zhihu.android.mixshortcontainer.function",
                "com.zhihu.android.mixshortcontainer.holder",
                "com.zhihu.android.mixshortcontainer.model",
                "com.zhihu.android.mixshortcontainer.nexttodetail",
            ),
            classMatcher, classLoader
        ) ?: throw ClassNotFoundException("com.zhihu.android.mixshortcontainer.dataflow.*.*.MixupDataParser")

        return Arrays.stream(mixupDataParserClass.getDeclaredMethods())
            .filter { method: Method -> method.returnType == List::class.java }
            .filter { method: Method -> method.parameterCount == 1 }
            .filter { method: Method ->
                method.getParameterTypes()[0] == JsonNodeOp.JsonNode
            }
            .findFirst().get()
    }

}
