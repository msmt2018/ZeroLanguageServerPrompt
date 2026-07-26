package com.zerostudio.lsp.manager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zerostudio.lsp.api.LspServerState
import com.zerostudio.lsp.api.LspWindowRegistry
import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.jsonrpc.messages.Either

/** Compose-friendly window registry that converts LSP callbacks into immutable UI state. */
class ComposeLspWindowRegistry : LspWindowRegistry {
    var state by mutableStateOf(LspToolWindowState())
        private set

    override fun showMessage(params: MessageParams) {
        appendDocumentation("${params.type}: ${params.message}")
    }

    override fun showMessageRequest(params: ShowMessageRequestParams): CompletableFuture<MessageActionItem> {
        appendDocumentation("${params.type}: ${params.message}")
        return CompletableFuture.completedFuture(params.actions?.firstOrNull())
    }

    override fun logMessage(params: MessageParams) {
        appendDocumentation("Log ${params.type}: ${params.message}")
    }

    override fun publishDiagnostics(params: PublishDiagnosticsParams) {
        state = state.copy(
            diagnostics = params.diagnostics.map { diagnostic ->
                LspDiagnosticItem(
                    source = diagnostic.source ?: params.uri,
                    message = diagnostic.message,
                    severity = diagnostic.severity.toLabel(),
                    line = diagnostic.range.start.line + 1,
                    column = diagnostic.range.start.character + 1,
                )
            },
        )
    }

    override fun showSymbols(symbols: List<Either<SymbolInformation, DocumentSymbol>>) {
        state = state.copy(
            outline = symbols.map { symbol ->
                if (symbol.isLeft) {
                    val value = symbol.left
                    LspOutlineItem(name = value.name, kind = value.kind.name, detail = value.location.uri)
                } else {
                    val value = symbol.right
                    LspOutlineItem(name = value.name, kind = value.kind.name, detail = value.detail.orEmpty())
                }
            },
        )
    }

    override fun showDocumentation(markup: MarkupContent) {
        state = state.copy(documentation = markup.value)
    }

    override fun showLifecycle(serverId: String, state: LspServerState) {
        val current = this.state.lifecycle.filterNot { it.serverId == serverId }
        this.state = this.state.copy(
            lifecycle = current + LspLifecycleItem(
                serverId = serverId,
                displayName = serverId,
                state = state,
            ),
        )
    }

    private fun appendDocumentation(message: String) {
        state = state.copy(documentation = listOf(state.documentation, message).filter { it.isNotBlank() }.joinToString("\n"))
    }
}

private fun DiagnosticSeverity?.toLabel(): String = when (this) {
    DiagnosticSeverity.Error -> "Error"
    DiagnosticSeverity.Warning -> "Warning"
    DiagnosticSeverity.Information -> "Info"
    DiagnosticSeverity.Hint -> "Hint"
    null -> "Unknown"
}
