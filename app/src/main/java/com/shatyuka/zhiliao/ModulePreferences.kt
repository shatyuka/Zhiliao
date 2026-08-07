package com.shatyuka.zhiliao

import android.content.Context
import android.content.SharedPreferences
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

/** Bridges the module app to LSPosed remote preferences and keeps its scope exact. */
object ModulePreferences {
    private const val GROUP = "zhiliao_preferences"
    private const val TARGET = "com.zhihu.android"
    private var registered = false
    private var service: XposedService? = null
    private var preferences: SharedPreferences? = null
    private val listeners = mutableSetOf<(SharedPreferences) -> Unit>()

    @Synchronized
    fun connect(context: Context, listener: (SharedPreferences) -> Unit) {
        listeners += listener
        preferences?.let(listener)
        if (registered) return
        registered = true
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(bound: XposedService) {
                service = bound
                val remote = bound.getRemotePreferences(GROUP)
                preferences = remote
                syncScope(bound)
                migrateLocal(context.applicationContext, remote)
                synchronized(this@ModulePreferences) { listeners.toList() }.forEach { it(remote) }
            }

            override fun onServiceDied(dead: XposedService) {
                if (service === dead) service = null
            }
        })
    }

    private fun syncScope(service: XposedService) {
        runCatching {
            val scope = service.scope
            val extra = scope.filterNot { it == TARGET }
            if (extra.isNotEmpty()) service.removeScope(extra)
            if (TARGET !in scope) {
                service.requestScope(listOf(TARGET), object : XposedService.OnScopeEventListener {})
            }
        }
    }

    private fun migrateLocal(context: Context, remote: SharedPreferences) {
        val local = context.getSharedPreferences(GROUP, Context.MODE_PRIVATE)
        if (local.all.isEmpty()) return
        val editor = remote.edit()
        local.all.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> @Suppress("UNCHECKED_CAST") editor.putStringSet(key, value as Set<String>)
            }
        }
        if (editor.commit()) local.edit().clear().apply()
    }
}
