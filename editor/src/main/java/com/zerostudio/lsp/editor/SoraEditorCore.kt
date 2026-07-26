package com.zerostudio.lsp.editor

import android.content.Context
import com.zerostudio.lsp.api.LspEditorAdapter
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.TextEdit
import io.github.rosemoe.sora.widget.CodeEditor

/** Sora editor-core factory and adapter entrypoint for consumers embedding the editor module. */
object SoraEditorCore {
    fun create(context: Context): CodeEditor = CodeEditor(context)
}

class SoraLspEditorAdapter(
    val editor: CodeEditor,
    override val documentUri: String,
) : LspEditorAdapter {
    private var diagnostics: PublishDiagnosticsParams? = null

    override fun applyTextEdits(edits: List<TextEdit>) {
        // Central extension point: translate LSP TextEdit ranges to Sora content mutations.
    }

    override fun requestCompletion(position: Position) {
        // Central extension point: trigger Sora completion UI from an LSP completion request.
    }

    override fun publishDiagnostics(params: PublishDiagnosticsParams) {
        diagnostics = params
    }

    fun latestDiagnostics(): PublishDiagnosticsParams? = diagnostics
}
