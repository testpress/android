# Testpress Android Coding Standards

---

## 1. Architecture (MVVM)

- **UI Layer (Activities, Fragments, Compose)**:
  - Passive rendering and user interaction only.
  - **Zero business logic**: Delegate all state and events to ViewModels.
  - Route all database (GreenDAO/Room) queries and network calls through ViewModels and Repositories; never execute from UI components.
- **ViewModel Layer (`viewmodel/`)**:
  - Expose UI state via `LiveData` or `StateFlow`.
  - Coordinate with Repositories. Use `SavedStateHandle` when process recreation state restoration is required.
- **Repository Layer (`repository/`, `network/`)**:
  - Single source of truth. Encapsulate DB operations and API calls (`RetrofitCall`, `TestpressCallback`).
  - Use `NetworkBoundResource` for cache-first DB + network synchronization.
- **Dependency Injection**: Inject dependencies with Dagger (`AppComponent`, `TestpressModule`). Avoid ad-hoc singletons.

---

## 2. Memory Safety & Lifecycle

- **Context Leaks**:
  - Pass `context.applicationContext` when a Context reference is required in ViewModels, Repositories, singletons, static methods, or background tasks. NEVER pass `Activity` or `Fragment` context.
  - Retain only `Application` context or weak references; never hold static references to `View`, `Activity`, `Fragment`, or `Context`.
- **Lifecycle & Observers**:
  - In Fragments, observe `LiveData` using `viewLifecycleOwner` (never `this`).
  - Clear listeners, timers, callbacks, and ViewBinding/DataBinding references in `onDestroyView()` or `onDestroy()`.

---

## 3. Concurrency & Threading

- **Main Thread Safety**: Dispatch network calls, file I/O, heavy computation, and database queries to `Dispatchers.IO` or background workers; reserve the Main thread strictly for UI updates.
- **Coroutines & Callbacks**:
  - Launch ViewModel background work in `viewModelScope` with `Dispatchers.IO` to ensure cancellation on clearance.
  - Update `LiveData` from background threads using `.postValue()`, never `.setValue()`.
  - Handle exceptions explicitly via `try-catch`, `CoroutineExceptionHandler`, or `Resource.error`.

---

## 4. Kotlin & Java Interoperability

- **Null Safety**:
  - **Kotlin**: Use safe calls (`?.`), Elvis operator (`?:`), or early returns; never use `!!` forced unwrapping.
  - **Java**: Annotate every parameter and return type with `@NonNull` or `@Nullable`.

---

## 5. UI, Resources & Jetpack Compose

- **Strings & Localization**: Define all user-facing text in `res/values/strings.xml`. Zero hardcoded UI strings.
- **Dimensions & Colors**: Reference `colors.xml` and `dimens.xml`; eliminate hardcoded density and color values.
- **Jetpack Compose**:
  - Keep composables stateless; hoist state and callbacks to ViewModels or parents.
  - Retain heavy objects (lists, formatters, paints) across recompositions using `remember` or `derivedStateOf`.

---

## 6. Database & Network

- **GreenDAO & Room**:
  - Execute all queries on background dispatchers.
  - Define and migrate database schemas through `testpressdaogenerator`.
- **Network States**: Wrap API responses in `Resource<T>` (`loading`, `success`, `error`). Implement `onException` in `TestpressCallback`.

---

## 7. Testing Standards

- **Unit Tests**: Test ViewModels, Repositories, and business utilities using `JUnit4`, `Mockito`, and `InstantTaskExecutorRule`.
- **Mandatory Scenarios**: Network error/timeout responses, empty/null data payloads, and lifecycle configuration changes.

---

## 8. Code Review Standards & Formatting

### 8.1 Severity Levels
- **`[CRITICAL]`**: Fatal crashes, security/token leaks, main-thread I/O, breaking regressions.
- **`[HIGH]`**: Context memory leaks, coroutine cancellation bugs, state desynchronization.
- **`[MEDIUM]`**: Actionable architectural improvements, missing `strings.xml`, unoptimized queries.
- **`[LOW]`**: Non-blocking cleanups and minor refactoring.

### 8.2 Merge Verdict
- 🔴 **Requires changes**: At least one `[CRITICAL]` or `[HIGH]` finding.
- 🟢 **Safe to merge**: Zero findings, or only non-blocking `[MEDIUM]` / `[LOW]` suggestions.

### 8.3 Review Guardrails
- **Diff Awareness**: Attach comments to the smallest relevant changed line. Review only modified code.
- **Explicit Citations**: Cite exact standards: `Violation: Section X.X — Rule`. Skip unverified or speculative issues.
- **Scope & Tone**: Defer formatting and style to automated linters. Provide actionable technical critique only; omit compliments and conversational filler.

### 8.4 Review Templates

#### Inline Comment
```markdown
### ⚠️ [CRITICAL | HIGH | MEDIUM | LOW] - <Brief Title>
**Violation**: <CODING_STANDARDS.md section or concrete requirement> — <specific rule>
**Impact**: <Clear description of the problem and potential impact>
**Suggested Fix**:
<Actionable solution with concrete code snippet>
```

#### Top-level Summary
```markdown
### 📝 Review Summary

<1-2 sentence overall assessment of PR quality>

**Verdict**: 🔴 **Requires changes** | 🟢 **Safe to merge**

| Severity | Component / Path | Finding Summary | Recommended Action |
| :--- | :--- | :--- | :--- |
| [CRITICAL | HIGH | MEDIUM | LOW] | `<path/to/file>` | <Brief summary> | <Recommended action> |
```
*(If no issues are found, skip the table and provide a concise summary).*
