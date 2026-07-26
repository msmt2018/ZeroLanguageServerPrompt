package com.zerostudio.lsp.api

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.RegistrationParams
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.UnregistrationParams
import org.eclipse.lsp4j.WorkDoneProgressCreateParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer

/** Standard API façade for routing every LSP4J protocol object through editor, JSON-RPC, windows, clients and servers. */
interface LspProtocolApi {
    val clientCapabilities: ClientCapabilities
    val serverCapabilities: ServerCapabilities

    fun createInitializeParams(rootUri: String?, workspaceFolders: List<WorkspaceFolder> = emptyList()): InitializeParams
    fun connect(server: LanguageServer, client: LanguageClient): CompletableFuture<InitializeResult>
    fun shutdown(server: LanguageServer): CompletableFuture<Any>
}

/** Pluggable client-side protocol surface, including windows, diagnostics, dynamic registration and telemetry. */
interface LspClientEndpoint : LanguageClient {
    val windows: LspWindowRegistry

    override fun telemetryEvent(`object`: Any?) = Unit
    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        windows.publishDiagnostics(diagnostics)
    }

    override fun showMessage(messageParams: MessageParams) {
        windows.showMessage(messageParams)
    }

    override fun showMessageRequest(requestParams: ShowMessageRequestParams): CompletableFuture<MessageActionItem> =
        windows.showMessageRequest(requestParams)

    override fun registerCapability(params: RegistrationParams): CompletableFuture<Void> = CompletableFuture.completedFuture(null)
    override fun unregisterCapability(params: UnregistrationParams): CompletableFuture<Void> = CompletableFuture.completedFuture(null)
    override fun createProgress(params: WorkDoneProgressCreateParams): CompletableFuture<Void> = CompletableFuture.completedFuture(null)
    override fun notifyProgress(params: org.eclipse.lsp4j.ProgressParams) = Unit
    override fun logMessage(message: MessageParams) {
        windows.logMessage(message)
    }
}

/** Editor bridge used by lsp-manager so Sora or another editor core can be swapped without changing protocol code. */
interface LspEditorAdapter {
    val documentUri: String
    fun currentDocument(): TextDocumentIdentifier = TextDocumentIdentifier(documentUri)
    fun applyTextEdits(edits: List<org.eclipse.lsp4j.TextEdit>)
    fun requestCompletion(position: org.eclipse.lsp4j.Position)
    fun publishDiagnostics(params: PublishDiagnosticsParams)
}

/** Pluggable LSP tool-window/UI contract inspired by IDE diagnostics, completion, outline, docs and lifecycle panes. */
interface LspWindowRegistry {
    fun showMessage(params: MessageParams)
    fun showMessageRequest(params: ShowMessageRequestParams): CompletableFuture<MessageActionItem>
    fun logMessage(params: MessageParams)
    fun publishDiagnostics(params: PublishDiagnosticsParams)
    fun showSymbols(symbols: List<Either<org.eclipse.lsp4j.SymbolInformation, org.eclipse.lsp4j.DocumentSymbol>>)
    fun showDocumentation(markup: org.eclipse.lsp4j.MarkupContent)
    fun showLifecycle(serverId: String, state: LspServerState)
}

enum class LspServerState { Registered, Starting, Running, Stopping, Stopped, Failed }
