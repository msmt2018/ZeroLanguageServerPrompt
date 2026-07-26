package com.zerostudio.lsp.manager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zerostudio.lsp.api.LspServerState

@Immutable
data class LspToolWindowActions(
    val onStartServer: () -> Unit = {},
    val onStopServer: () -> Unit = {},
    val onRestartServer: () -> Unit = {},
    val onClearDiagnostics: () -> Unit = {},
    val onRefreshOutline: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onNavigateDiagnostic: (LspDiagnosticItem) -> Unit = {},
    val onNavigateSymbol: (LspOutlineItem) -> Unit = {},
)

@Immutable
data class LspToolWindowState(
    val diagnostics: List<LspDiagnosticItem> = emptyList(),
    val outline: List<LspOutlineItem> = emptyList(),
    val documentation: String = "",
    val lifecycle: List<LspLifecycleItem> = emptyList(),
)

@Immutable
data class LspDiagnosticItem(
    val source: String,
    val message: String,
    val severity: String,
    val line: Int,
    val column: Int,
)

@Immutable
data class LspOutlineItem(
    val name: String,
    val kind: String,
    val detail: String = "",
)

@Immutable
data class LspLifecycleItem(
    val serverId: String,
    val displayName: String,
    val state: LspServerState,
)

enum class LspToolWindowTab(val title: String) {
    Diagnostics("诊断"),
    Outline("大纲"),
    Documentation("文档"),
    Lifecycle("生命周期"),
}

@Composable
fun LspToolWindowHost(
    state: LspToolWindowState,
    modifier: Modifier = Modifier,
    actions: LspToolWindowActions = LspToolWindowActions(),
    initialTab: LspToolWindowTab = LspToolWindowTab.Diagnostics,
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                LspToolWindowTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {},
                        label = { Text(tab.title) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(12.dp)) {
            ToolWindowHeader(
                selectedTab = selectedTab,
                state = state,
                actions = actions,
            )
            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            when (selectedTab) {
                LspToolWindowTab.Diagnostics -> DiagnosticsPane(state.diagnostics, actions)
                LspToolWindowTab.Outline -> OutlinePane(state.outline, actions)
                LspToolWindowTab.Documentation -> DocumentationPane(state.documentation)
                LspToolWindowTab.Lifecycle -> LifecyclePane(state.lifecycle, actions)
            }
        }
    }
}

@Composable
private fun ToolWindowHeader(
    selectedTab: LspToolWindowTab,
    state: LspToolWindowState,
    actions: LspToolWindowActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Language Servers · ${selectedTab.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Diagnostics ${state.diagnostics.size} · Symbols ${state.outline.size} · Servers ${state.lifecycle.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = actions.onOpenSettings) { Text("⚙") }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(onClick = actions.onStartServer) { Text("▶ Run") }
            OutlinedButton(onClick = actions.onStopServer) { Text("■ Stop") }
            OutlinedButton(onClick = actions.onRestartServer) { Text("↻ Restart") }
            when (selectedTab) {
                LspToolWindowTab.Diagnostics -> TextButton(onClick = actions.onClearDiagnostics) { Text("Clear") }
                LspToolWindowTab.Outline -> TextButton(onClick = actions.onRefreshOutline) { Text("Refresh") }
                LspToolWindowTab.Documentation -> TextButton(onClick = actions.onOpenSettings) { Text("Open Docs Settings") }
                LspToolWindowTab.Lifecycle -> TextButton(onClick = actions.onOpenSettings) { Text("Configure Servers") }
            }
        }
    }
}

@Composable
private fun DiagnosticsPane(items: List<LspDiagnosticItem>, actions: LspToolWindowActions) {
    if (items.isEmpty()) {
        EmptyPane("暂无诊断。")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            LspInfoCard(title = item.message, subtitle = "${item.source} · ${item.line}:${item.column}") {
                Column {
                    AssistChip(onClick = {}, label = { Text(item.severity) })
                    TextButton(onClick = { actions.onNavigateDiagnostic(item) }) { Text("Jump") }
                }
            }
        }
    }
}

@Composable
private fun OutlinePane(items: List<LspOutlineItem>, actions: LspToolWindowActions) {
    if (items.isEmpty()) {
        EmptyPane("暂无文档符号。")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            LspInfoCard(title = item.name, subtitle = item.detail.ifBlank { item.kind }) {
                Column {
                    AssistChip(onClick = {}, label = { Text(item.kind) })
                    TextButton(onClick = { actions.onNavigateSymbol(item) }) { Text("Navigate") }
                }
            }
        }
    }
}

@Composable
private fun DocumentationPane(documentation: String) {
    EmptyPane(documentation.ifBlank { "暂无悬浮文档或签名帮助。" })
}

@Composable
private fun LifecyclePane(items: List<LspLifecycleItem>, actions: LspToolWindowActions) {
    if (items.isEmpty()) {
        EmptyPane("暂无已注册语言服务器。")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            LspInfoCard(title = item.displayName, subtitle = item.serverId) {
                Column {
                    AssistChip(onClick = {}, label = { Text(item.state.name) })
                    TextButton(onClick = actions.onRestartServer) { Text("Restart") }
                }
            }
        }
    }
}

@Composable
private fun LspInfoCard(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            trailing()
        }
    }
}

@Composable
private fun EmptyPane(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
