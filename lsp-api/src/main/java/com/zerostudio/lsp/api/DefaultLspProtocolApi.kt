package com.zerostudio.lsp.api

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer

class DefaultLspProtocolApi(
    override val clientCapabilities: ClientCapabilities = ClientCapabilities(),
    override val serverCapabilities: ServerCapabilities = ServerCapabilities(),
) : LspProtocolApi {
    override fun createInitializeParams(rootUri: String?, workspaceFolders: List<WorkspaceFolder>): InitializeParams =
        InitializeParams().apply {
            this.rootUri = rootUri
            this.workspaceFolders = workspaceFolders
            this.capabilities = clientCapabilities
        }

    override fun connect(server: LanguageServer, client: LanguageClient): CompletableFuture<InitializeResult> {
        server.connect(client)
        return server.initialize(createInitializeParams(null))
    }

    override fun shutdown(server: LanguageServer): CompletableFuture<Any> = server.shutdown()
}
