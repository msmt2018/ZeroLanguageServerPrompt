package com.zerostudio.lsp.manager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zerostudio.lsp.api.LspServerState

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
        Column(Modifier.padding(innerPadding).padding(16.dp)) {
            Text(selectedTab.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            when (selectedTab) {
                LspToolWindowTab.Diagnostics -> DiagnosticsPane(state.diagnostics)
                LspToolWindowTab.Outline -> OutlinePane(state.outline)
                LspToolWindowTab.Documentation -> DocumentationPane(state.documentation)
                LspToolWindowTab.Lifecycle -> LifecyclePane(state.lifecycle)
            }
        }
    }
}

@Composable
private fun DiagnosticsPane(items: List<LspDiagnosticItem>) {
    if (items.isEmpty()) {
        EmptyPane("暂无诊断。")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            LspInfoCard(title = item.message, subtitle = "${item.source} · ${item.line}:${item.column}") {
                AssistChip(onClick = {}, label = { Text(item.severity) })
            }
        }
    }
}

@Composable
private fun OutlinePane(items: List<LspOutlineItem>) {
    if (items.isEmpty()) {
        EmptyPane("暂无文档符号。")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            LspInfoCard(title = item.name, subtitle = item.detail.ifBlank { item.kind }) {
                AssistChip(onClick = {}, label = { Text(item.kind) })
            }
        }
    }
}

@Composable
private fun DocumentationPane(documentation: String) {
    EmptyPane(documentation.ifBlank { "暂无悬浮文档或签名帮助。" })
}

@Composable
private fun LifecyclePane(items: List<LspLifecycleItem>) {
    if (items.isEmpty()) {
        EmptyPane("暂无已注册语言服务器。")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
            LspInfoCard(title = item.displayName, subtitle = item.serverId) {
                AssistChip(onClick = {}, label = { Text(item.state.name) })
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
