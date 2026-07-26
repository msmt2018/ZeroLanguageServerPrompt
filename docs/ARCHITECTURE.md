# Zero Language Server Prompt Architecture

## 1. Development flow

1. **Protocol first (`:lsp-api`)**: define stable LSP4J-facing contracts for protocol objects, JSON-RPC client/server endpoints, requests, responses, notifications, editor bridges, window bridges and lifecycle states before adding feature implementations.
2. **Editor core second (`:editor`)**: integrate Sora Editor as the default editor core behind `LspEditorAdapter`, keeping all editor-specific APIs isolated from protocol and manager modules.
3. **Management third (`:lsp-manager`)**: coordinate pluggable server registration, runtime lifecycle, editor attachment, capability discovery and UI-window dispatch through interfaces instead of concrete implementations.
4. **Composable UI fourth (`:lsp-manager` UI package)**: ship default Jetpack Compose windows inspired by IntelliJ IDEA and Android Studio while keeping every window replaceable through `LspWindowRegistry`.
5. **Application integration last (`:app`)**: depend on `:lsp-manager`, select concrete server factories, provide project/workspace configuration and install custom UI or editor adapters only when the app needs them.

## 2. Module responsibilities

| Module | Responsibility | Must not contain |
| --- | --- | --- |
| `:lsp-api` | LSP4J protocol facade, client endpoint, editor adapter, window adapter, lifecycle and request/response routing contracts. | Android UI implementation, Sora-specific logic, concrete server process launchers. |
| `:editor` | Sora `CodeEditor` creation and translation points between Sora document state and LSP objects. | Server registry, JSON-RPC transport policy, Compose tool windows. |
| `:lsp-manager` | Server descriptors, hot-pluggable registration, lifecycle orchestration, editor attachment, default no-op and Compose window hosts. | App-specific server selections or project-specific settings. |
| `:app` | Demo/host application composition and final dependency assembly. | Reusable protocol contracts that belong in libraries. |

## 3. Architecture rules

- Dependencies flow downward: `:app -> :lsp-manager -> (:lsp-api, :editor)` and `:editor -> :lsp-api`.
- LSP4J objects remain the canonical protocol DTOs so the project can expose all protocol capabilities without inventing incompatible mirrors.
- Every runtime dependency that may vary by editor or host app is represented by an interface: editor adapters, server factories and window registries.
- UI follows recommended Android architecture: immutable UI state, stateless composables where possible, state hoisting at the host boundary and Material 3 components.
- Default UI implements diagnostics, outline, documentation and lifecycle panes; completion popups and richer request windows are added as specialized composables over the same state model.

## 4. Source production plan

1. Complete protocol coverage by adding typed dispatcher methods for text document, workspace, window, notebook and semantic-token features in `:lsp-api`.
2. Implement Sora document synchronization, edit application, completion triggering and diagnostic rendering in `:editor`.
3. Add process/socket/embedded server launchers, workspace routing and capability negotiation in `:lsp-manager`.
4. Expand Compose windows for completion, hover documentation, go-to symbol, diagnostics tree, server logs and lifecycle controls.
5. Add integration tests and sample server wiring in `:app` after dependency resolution is available in CI.

## 5. LSP window UX rules

- The default tool window follows IDEA/Android Studio interaction patterns: a compact header, server lifecycle actions, tabbed panes, per-row navigation actions and secondary configuration actions.
- Actions are exposed through `LspToolWindowActions` instead of being hard-coded, so host apps can wire Run, Stop, Restart, Clear, Refresh, Jump and Settings to their own command system.
- Diagnostic and outline rows always provide a direct navigation action, while lifecycle rows expose restart controls for quick server recovery.
- The Compose implementation remains a default skin; products can replace it by providing another `LspWindowRegistry` or another composable over `LspToolWindowState`.
