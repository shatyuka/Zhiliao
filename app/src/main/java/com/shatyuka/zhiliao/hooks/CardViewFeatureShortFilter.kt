package com.shatyuka.zhiliao.hooks

import android.util.Pair
import com.shatyuka.zhiliao.Helper
import com.shatyuka.zhiliao.Helper.JsonNodeOp
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.luckypray.dexkit.query.matchers.ClassMatcher
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Arrays
import java.util.Optional

/**
 * 卡片视图(FeatureUI)
 */
class CardViewFeatureShortFilter : IHook {

    private lateinit var mixupDataParser_jsonNode2Object: Method
    private lateinit var mixupDataParser_jsonNode2List: Method

    override fun getName(): String {
        return "卡片视图相关过滤(FeatureUI)"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        val jsonNode2List_jsonNode2Object = findJsonNode2ListAndJsonNode2ObjectMethod(classLoader)
        mixupDataParser_jsonNode2Object = jsonNode2List_jsonNode2Object.second
        mixupDataParser_jsonNode2List = jsonNode2List_jsonNode2Object.first
    }

    @Throws(Throwable::class)
    override fun hook() {
        XposedBridge.hookMethod(mixupDataParser_jsonNode2List, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return
                }
                if (Helper.prefs.getBoolean("switch_feedad", true)) {
                    filterShortContent(param.args[0])
                }
            }
        })
        XposedBridge.hookMethod(mixupDataParser_jsonNode2Object, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
                    return
                }
                preProcessShortContent(param.args[0])
            }
        })
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    private fun filterShortContent(shortContentListJsonNode: Any) {
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
            }
        }
    }

    private fun findJsonNode2ListAndJsonNode2ObjectMethod(classLoader: ClassLoader): Pair<Method, Method> {
        val classMath: ClassMatcher = ClassMatcher.create().methods {
            add {
                returnType = Object::class.java.name
                paramCount = 1
                paramTypes(JsonNodeOp.JsonNode)
            }
            add {
                returnType = List::class.java.name
                paramCount = 1
                paramTypes(JsonNodeOp.JsonNode)
            }
        }

        val mixupDataParserClass = Helper.findClass(
            listOf("com.zhihu.android.service.short_container_service.dataflow.repo"),
            null,
            classMath,
            classLoader
        )
            ?: throw ClassNotFoundException("com.zhihu.android.service.short_container_service.dataflow.repo.*.MixupDataParser")

        return Pair(
            findJsonNode2ListMethod(mixupDataParserClass).get(),
            findJsonNode2ObjectMethod(mixupDataParserClass).get()
        )
    }

    private fun findJsonNode2ObjectMethod(mixupDataParser: Class<*>): Optional<Method> {
        return Arrays.stream(mixupDataParser.getDeclaredMethods())
            .filter { method: Method -> method.returnType == Object::class.java }
            .filter { method: Method -> method.parameterCount == 1 }
            .filter { method: Method -> method.getParameterTypes()[0] == JsonNodeOp.JsonNode }
            .findFirst()
    }

    private fun findJsonNode2ListMethod(mixupDataParser: Class<*>): Optional<Method> {
        return Arrays.stream(mixupDataParser.getDeclaredMethods())
            .filter { method: Method -> method.returnType == List::class.java }
            .filter { method: Method -> method.parameterCount == 1 }
            .filter { method: Method -> method.getParameterTypes()[0] == JsonNodeOp.JsonNode }
            .findFirst()
    }

    companion object {
        @JvmStatic
        fun shouldRemoveShortContent(shortContentJsonNode: Any): Boolean {
            try {
                return isAd(shortContentJsonNode) || hasMoreType(shortContentJsonNode)
            } catch (e: Exception) {
                XposedBridge.log("[Zhiliao]")
                XposedBridge.log(e)
            }
            return false
        }

        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        private fun isAd(shortContentJsonNode: Any): Boolean {
            if (JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "adjson") != null) {
                return true
            }
            val adInfo = JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "ad_info")
            if (adInfo == null) {
                XposedBridge.log(shortContentJsonNode.toString())
                return false
            }
            val adInfoData = JsonNodeOp.JsonNode_get.invoke(adInfo, "data")
            return if (adInfoData != null) {
                // "" , "{}"
                adInfoData.toString().length > 4
            } else false
        }

        /**
         * todo: 有多个type的, 不一定全是推广/广告, 有概率被误去除
         */
        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        private fun hasMoreType(shortContentJsonNode: Any): Boolean {
            val bizTypeList = JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "biz_type_list")
            return JsonNodeOp.JsonNode_size.invoke(bizTypeList) as Int > 1
        }

        @JvmStatic
        fun preProcessShortContent(shortContentJsonNode: Any?) {
            try {
                val searchWordJsonNode =
                    JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "search_word")
                if (searchWordJsonNode != null) {
                    JsonNodeOp.ObjectNode_put.invoke(searchWordJsonNode, "queries", null)
                }
            } catch (e: Exception) {
                XposedBridge.log("[Zhiliao]")
                XposedBridge.log(e)
            }
            try {
                JsonNodeOp.ObjectNode_put.invoke(shortContentJsonNode, "relationship_tips", null)
            } catch (e: Exception) {
                XposedBridge.log("[Zhiliao]")
                XposedBridge.log(e)
            }
            if (Helper.prefs.getBoolean("switch_related", false)) {
                try {
                    val thirdBusiness =
                        JsonNodeOp.JsonNode_get.invoke(shortContentJsonNode, "third_business")
                    if (thirdBusiness != null) {
                        val relatedQueries =
                            JsonNodeOp.JsonNode_get.invoke(thirdBusiness, "related_queries")
                        if (relatedQueries != null) {
                            JsonNodeOp.ObjectNode_put.invoke(relatedQueries, "queries", null)
                        }
                    }
                } catch (e: Exception) {
                    XposedBridge.log("[Zhiliao]")
                    XposedBridge.log(e)
                }
            }
        }
    }
}
