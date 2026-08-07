package com.shatyuka.zhiliao

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var preferences: SharedPreferences
    private var hookValues by mutableStateOf<Map<String, Boolean>>(emptyMap())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE)
        reloadHookValues()
        ModulePreferences.connect(this) { remote ->
            runOnUiThread {
                preferences = remote
                reloadHookValues()
            }
        }
        enableEdgeToEdge()
        setContent { ZhiliaoApp() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ZhiliaoApp() {
        val background = colorResource(R.color.module_background)
        val surface = colorResource(R.color.module_surface)
        val primary = colorResource(R.color.module_primary)
        val onSurface = colorResource(R.color.module_on_surface)
        val onSurfaceVariant = colorResource(R.color.module_on_surface_variant)
        val scheme = if (isSystemInDarkTheme()) {
            darkColorScheme(
                primary = primary,
                background = background,
                surface = surface,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
            )
        } else {
            lightColorScheme(
                primary = primary,
                background = background,
                surface = surface,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
            )
        }
        MaterialTheme(colorScheme = scheme) {
            Scaffold(
                containerColor = background,
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold) },
                        actions = {
                            IconButton(onClick = { openUrl(GITHUB_URL) }) {
                                Icon(
                                    painterResource(R.drawable.ic_github),
                                    contentDescription = stringResource(R.string.github_homepage),
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = background),
                    )
                },
            ) { insets ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(insets),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item { HeroCard() }
                    item { SectionTitle(stringResource(R.string.module_settings)) }
                    item { HookSettingsCard() }
                    item { SectionTitle(stringResource(R.string.about)) }
                    item { AboutCard() }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }

    @Composable
    private fun HookSettingsCard() {
        val master = hookValues[KEY_MASTER] ?: false
        SettingsCard {
            SettingSwitch(
                title = stringResource(R.string.enable_module),
                summary = stringResource(R.string.enable_module_summary),
                checked = master,
            ) { setHookValue(KEY_MASTER, it) }
            HOOK_OPTIONS.forEach { option ->
                HorizontalDivider()
                SettingSwitch(
                    title = stringResource(option.title),
                    summary = stringResource(option.summary),
                    checked = hookValues[option.key] ?: option.default,
                    enabled = master,
                ) { setHookValue(option.key, it) }
            }
        }
    }

    @Composable
    private fun HeroCard() {
        Card(
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.module_hero)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_cicada_outline),
                    contentDescription = null,
                    tint = colorResource(R.color.module_primary),
                    modifier = Modifier.size(48.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.version_format, currentVersionName()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(title: String) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
    }

    @Composable
    private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) { Column(content = content) }
    }

    @Composable
    private fun SettingSwitch(
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChecked: (Boolean) -> Unit,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, enabled = enabled, onCheckedChange = onChecked)
        }
    }

    private fun reloadHookValues() {
        hookValues = buildMap {
            put(KEY_MASTER, preferences.getBoolean(KEY_MASTER, false))
            HOOK_OPTIONS.forEach { put(it.key, preferences.getBoolean(it.key, it.default)) }
        }
    }

    private fun setHookValue(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
        hookValues = hookValues + (key to value)
    }

    @Composable
    private fun AboutCard() {
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(stringResource(R.string.lsposed_entry_title), fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.lsposed_entry_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_github), contentDescription = null, modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(stringResource(R.string.github_homepage), fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.open_source_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { openUrl(GITHUB_URL) }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = stringResource(R.string.open_link),
                    )
                }
            }
        }
    }

    private fun currentVersionName(): String = getString(R.string.app_version)

    private fun openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    private data class HookOption(
        val key: String,
        val title: Int,
        val summary: Int,
        val default: Boolean = false,
    )

    companion object {
        private const val PREFS = "zhiliao_preferences"
        private const val KEY_MASTER = "switch_mainswitch"
        private const val GITHUB_URL = "https://github.com/shatyuka/Zhiliao"

        private val HOOK_OPTIONS = listOf(
            HookOption("switch_launchad", R.string.remove_launch_ads, R.string.remove_launch_ads_summary, true),
            HookOption("switch_launch_optimize", R.string.optimize_splash, R.string.optimize_splash_summary),
            HookOption("switch_feedad", R.string.remove_feed_ads, R.string.remove_feed_ads_summary, true),
            HookOption("switch_answerlistad", R.string.remove_answer_list_ads, R.string.remove_answer_list_ads_summary, true),
            HookOption("switch_commentad", R.string.remove_comment_ads, R.string.remove_comment_ads_summary, true),
            HookOption("switch_sharead", R.string.remove_share_ads, R.string.remove_share_ads_summary, true),
            HookOption("switch_answerad", R.string.remove_answer_ads, R.string.remove_answer_ads_summary, true),
            HookOption("switch_searchad", R.string.remove_search_ads, R.string.remove_search_ads_summary, true),
            HookOption("switch_video", R.string.filter_video, R.string.filter_video_summary),
            HookOption("switch_removearticle", R.string.filter_article, R.string.filter_article_summary),
            HookOption("switch_pin", R.string.filter_pin, R.string.filter_pin_summary),
            HookOption("switch_externlinkex", R.string.open_external_links, R.string.open_external_links_summary),
            HookOption("switch_autorefresh", R.string.prevent_auto_refresh, R.string.prevent_auto_refresh_summary),
            HookOption("switch_livebutton", R.string.hide_live_button, R.string.hide_live_button_summary),
            HookOption("switch_reddot", R.string.hide_red_dots, R.string.hide_red_dots_summary),
            HookOption("switch_vipbanner", R.string.hide_vip_banner, R.string.hide_vip_banner_summary),
            HookOption("switch_hotbanner", R.string.hide_hot_banner, R.string.hide_hot_banner_summary),
            HookOption("switch_feedtophot", R.string.hide_feed_top_hot, R.string.hide_feed_top_hot_summary),
            HookOption("switch_minehybrid", R.string.hide_mine_cards, R.string.hide_mine_cards_summary),
            HookOption("switch_subscribe", R.string.hide_follow_button, R.string.hide_follow_button_summary),
            HookOption("switch_vipnav", R.string.hide_vip_nav, R.string.hide_nav_summary),
            HookOption("switch_videonav", R.string.hide_video_nav, R.string.hide_nav_summary),
            HookOption("switch_friendnav", R.string.hide_follow_nav, R.string.hide_nav_summary),
            HookOption("switch_panelnav", R.string.hide_publish_nav, R.string.hide_nav_summary),
            HookOption("switch_findnav", R.string.hide_discover_nav, R.string.hide_nav_summary),
            HookOption("switch_navres", R.string.disable_nav_theme, R.string.disable_nav_theme_summary),
            HookOption("switch_nipple", R.string.flatten_bottom_nav, R.string.flatten_bottom_nav_summary),
            HookOption("switch_horizontal", R.string.horizontal_answers, R.string.horizontal_answers_summary),
            HookOption("switch_nextanswer", R.string.hide_next_answer, R.string.hide_next_answer_summary),
            HookOption("switch_watermark", R.string.remove_web_watermark, R.string.remove_web_watermark_summary),
        )
    }
}
