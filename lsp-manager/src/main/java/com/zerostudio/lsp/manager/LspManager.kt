package com.zerostudio.lsp.manager

import com.zerostudio.lsp.api.DefaultLspProtocolApi
import com.zerostudio.lsp.api.LspClientEndpoint
import com.zerostudio.lsp.api.LspEditorAdapter
import com.zerostudio.lsp.api.LspProtocolApi
import com.zerostudio.lsp.api.LspServerState
import com.zerostudio.lsp.api.LspWindowRegistry
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageServer

fun interface LanguageServerFactory {
    fun create(): LanguageServer
}

data class LspServerDescriptor(
    val id: String,
    val displayName: String,
    val languages: Set<String>,
    val factory: LanguageServerFactory,
)

class LspManager(
    private val protocolApi: LspProtocolApi = DefaultLspProtocolApi(),
    private val windows: LspWindowRegistry = NoOpLspWindowRegistry,
) {
    private val descriptors = ConcurrentHashMap<String, LspServerDescriptor>()
    private val running = ConcurrentHashMap<String, LanguageServer>()
    private val editors = ConcurrentHashMap<String, LspEditorAdapter>()

    fun registerServer(descriptor: LspServerDescriptor) {
        descriptors[descriptor.id] = descriptor
        windows.showLifecycle(descriptor.id, LspServerState.Registered)
    }

    fun unregisterServer(id: String) {
        stopServer(id)
        descriptors.remove(id)
    }

    fun attachEditor(editorId: String, adapter: LspEditorAdapter) {
        editors[editorId] = adapter
    }

    fun detachEditor(editorId: String) {
        editors.remove(editorId)
    }

    fun startServer(id: String): CompletableFuture<InitializeResult> {
        val descriptor = requireNotNull(descriptors[id]) { "LSP server '$id' is not registered." }
        windows.showLifecycle(id, LspServerState.Starting)
        val server = descriptor.factory.create()
        running[id] = server
        return protocolApi.connect(server, ManagedLanguageClient(windows))
            .whenComplete { _, error -> windows.showLifecycle(id, if (error == null) LspServerState.Running else LspServerState.Failed) }
    }

    fun stopServer(id: String): CompletableFuture<Any> {
        val server = running.remove(id) ?: return CompletableFuture.completedFuture(Unit)
        windows.showLifecycle(id, LspServerState.Stopping)
        return protocolApi.shutdown(server).whenComplete { _, _ -> windows.showLifecycle(id, LspServerState.Stopped) }
    }
}

private class ManagedLanguageClient(override val windows: LspWindowRegistry) : LspClientEndpoint

object NoOpLspWindowRegistry : LspWindowRegistry {
    override fun showMessage(params: MessageParams) = Unit
    override fun showMessageRequest(params: ShowMessageRequestParams): CompletableFuture<MessageActionItem> = CompletableFuture.completedFuture(null)
    override fun logMessage(params: MessageParams) = Unit
    override fun publishDiagnostics(params: PublishDiagnosticsParams) = Unit
    override fun showSymbols(symbols: List<Either<org.eclipse.lsp4j.SymbolInformation, org.eclipse.lsp4j.DocumentSymbol>>) = Unit
    override fun showDocumentation(markup: org.eclipse.lsp4j.MarkupContent) = Unit
    override fun showLifecycle(serverId: String, state: LspServerState) = Unit
}
