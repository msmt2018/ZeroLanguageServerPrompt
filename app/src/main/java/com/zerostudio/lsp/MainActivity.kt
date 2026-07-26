package com.zerostudio.lsp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zerostudio.lsp.api.LspServerState
import com.zerostudio.lsp.manager.ui.LspDiagnosticItem
import com.zerostudio.lsp.manager.ui.LspLifecycleItem
import com.zerostudio.lsp.manager.ui.LspOutlineItem
import com.zerostudio.lsp.manager.ui.LspToolWindowHost
import com.zerostudio.lsp.manager.ui.LspToolWindowState
import com.zerostudio.lsp.ui.theme.ComposeEmptyActivityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeEmptyActivityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LspConsoleScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LspConsoleScreen(modifier: Modifier = Modifier) {
    LspToolWindowHost(
        state = sampleLspToolWindowState(),
        modifier = modifier.fillMaxSize(),
    )
}

private fun sampleLspToolWindowState() = LspToolWindowState(
    diagnostics = listOf(
        LspDiagnosticItem(
            source = "kotlin-language-server",
            message = "未解析的符号会在这里聚合展示。",
            severity = "Warning",
            line = 24,
            column = 12,
        ),
    ),
    outline = listOf(
        LspOutlineItem(name = "LspManager", kind = "Class", detail = "统一调度语言服务器生命周期"),
        LspOutlineItem(name = "LspToolWindowHost", kind = "Composable", detail = "IDE 风格 LSP 工具窗口"),
    ),
    documentation = "Hover、签名帮助和 Markdown 文档会通过可插拔窗口渲染到这里。",
    lifecycle = listOf(
        LspLifecycleItem(
            serverId = "kotlin",
            displayName = "Kotlin Language Server",
            state = LspServerState.Registered,
        ),
    ),
)

@Preview(showBackground = true)
@Composable
fun LspConsolePreview() {
    ComposeEmptyActivityTheme {
        LspConsoleScreen()
    }
}
