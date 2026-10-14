# Android Coding Standards & Guidelines

This document defines the official engineering standards, architectural patterns, and quality guidelines for the Testpress Android application. All code written, refactored, or reviewed—by both engineers and AI agents—must strictly adhere to these standards.

---

## 1. Architecture & Component Design (MVVM)

### 1.1 Architecture Layers
We enforce a strict **Model-View-ViewModel (MVVM)** architecture to ensure separation of concerns:
- **UI Layer (Activities, Fragments, Compose)**:
  - Strictly responsible for rendering UI, handling user interaction, and observing ViewModel state.
  - **Zero business logic** in Activities, Fragments, Adapters, or Composables.
  - Must not directly access databases (GreenDAO/Room) or perform network calls.
- **ViewModel Layer (`viewmodel/`)**:
  - Exposes state via `LiveData` (or reactive streams) for the UI layer to observe.
  - Coordinates data flow between UI and Repositories.
  - Survives configuration changes and maintains UI state.
- **Data / Repository Layer (`repository/`, `network/`)**:
  - Serves as the single source of truth for domain data.
  - Manages data fetching strategy (e.g., `NetworkBoundResource` for caching DB + remote network).
  - Encapsulates database queries (GreenDAO/Room) and API calls (`RetrofitCall`, `TestpressCallback`).

### 1.2 Dependency Injection
- Use **Dagger** (`AppComponent`, `TestpressModule`, etc.) for providing dependencies.
- Avoid tight coupling or manual singleton instantiation where DI is available.

---

## 2. Memory Safety & Lifecycle Management

### 2.1 Preventing Memory Leaks
- **Context Usage**:
  - **NEVER** pass an `Activity` or `Fragment` context to ViewModels, Repositories, singletons, static utility methods, or background tasks.
  - If a `Context` is required outside the UI layer, use `ApplicationContext` (`context.applicationContext`).
  - Do not hold static references to `View`, `Activity`, `Fragment`, or `Context`.
- **Observer Cleanup & Lifecycle Awareness**:
  - In Fragments, observe `LiveData` using `viewLifecycleOwner` (not `this`) to avoid duplicate callbacks across Fragment view recreations.
  - Clean up listeners, timers, callbacks, and subscriptions in `onDestroyView()` or `onDestroy()`.
  - Avoid leaking ViewBinding or DataBinding references in Fragments.

---

## 3. Concurrency & Threading

### 3.1 Thread Safety
- **Main Thread Isolation**:
  - The UI (Main) thread must remain responsive at all times.
  - **NEVER** run network requests, file I/O, heavy parsing, or database queries on the Main thread.
- **Asynchronous Execution**:
  - Use Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`) or `TestpressCallback` with asynchronous network calls.
  - Use `MutableLiveData.postValue()` when updating LiveData from background threads; use `.setValue()` only when on the Main thread.
- **Cancellation & Exception Handling**:
  - Coroutines launched in ViewModels should be bound to `viewModelScope` so they cancel automatically when the ViewModel clears.
  - Handle exceptions gracefully using `try-catch`, CoroutineExceptionHandler, or `Resource.error` wrappers.

---

## 4. Kotlin & Java Interoperability

### 4.1 Null Safety
- **Kotlin**:
  - Avoid forced unwrapping (`!!`). Use safe calls (`?.`), Elvis operator (`?:`), `let`, or early returns.
  - Define clear nullability in data models and DTOs.
- **Java**:
  - Annotate all method parameters and return types with `@NonNull` or `@Nullable` to ensure seamless Kotlin interoperability.
  - Check for null before accessing fields on nullable Java objects.

### 4.2 Clean Code Principles
- Keep classes and functions focused (Single Responsibility Principle).
- Avoid "God" Activities or Fragments. Split complex UI into smaller, reusable components or custom views.
- Write readable, self-documenting code. Favor descriptive naming over ambiguous abbreviations.

---

## 5. UI, Resources & Localization

### 5.1 Resource Centralization
- **Hardcoded Strings**:
  - **STRICTLY FORBIDDEN**: Never hardcode UI text strings in Kotlin, Java, or XML layout files.
  - All user-facing strings must reside in `res/values/strings.xml` to support localization.
- **Dimensions & Colors**:
  - Reference centralized styles, colors (`colors.xml`), and dimensions (`dimens.xml`) rather than arbitrary inline magic values.

### 5.2 Jetpack Compose Guidelines
- Composable functions must be stateless where possible, with state hoisted to parent callers or ViewModels.
- Avoid allocating objects (e.g., list transformations, paint objects) directly within Composable bodies or during recomposition loops.
- Use `remember` and `derivedStateOf` appropriately to optimize recomposition performance.

---

## 6. Database & Network

### 6.1 Database (GreenDAO & Room)
- Never perform database reads, writes, or schema migrations on the main thread.
- Keep queries indexed and performant; avoid fetching unbounded result sets when pagination is available.
- Schema modifications must be properly tracked in `testpressdaogenerator` with appropriate version bumps.

### 6.2 Network Calls & Error Handling
- Use `Resource<T>` (`Resource.loading`, `Resource.success`, `Resource.error`) to represent network/data states.
- Always implement proper error handling in `TestpressCallback` (`onException`).
- Do not silently swallow exceptions or fail to notify users of critical network failures.

---

## 7. Testing Standards

- **Unit Tests**:
  - Write unit tests for ViewModels, Repositories, and business logic utilities.
  - Use `JUnit4`, `Mockito` / `PowerMock`, and `androidx.arch.core.executor.testing.InstantTaskExecutorRule` for testing LiveData.
  - Mock network responses with `MockWebServer` or mocked service interfaces.
- **Edge Cases**:
  - Test for empty data, network errors, invalid responses, and configuration changes (e.g., screen rotations).

---

## 8. Code Review Severity Levels & Review Rules

When reviewing Pull Requests, all feedback must be categorized into one of the following severity levels:

| Severity | Definition | Examples |
| :--- | :--- | :--- |
| **`[CRITICAL]`** | Showstoppers, crash risks, security/data leaks, breaking changes | Main thread disk/network I/O, security token leaks, unhandled fatal exceptions, critical regression |
| **`[HIGH]`** | Architectural flaws, memory leaks, concurrency bugs | Passing Activity context to ViewModel, missing coroutine error handling, observer leak in Fragment |
| **`[MEDIUM]`** | Actionable design improvements, performance/maintainability issues | SRP violation in Activity, missing `strings.xml` entry, inefficient database query, suboptimal recomposition |
| **`[LOW]`** | Minor non-blocking refactoring or cleanup | Code organization, minor naming improvement, redundant code simplification |

### 8.1 Merge Verdict Rules
- 🔴 **Requires changes**: Applied if there is at least **one** `[CRITICAL]` or `[HIGH]` finding.
- 🟢 **Safe to merge**: Applied when there are zero findings, or all findings are non-blocking `[MEDIUM]` / `[LOW]` suggestions.

### 8.2 Review Guardrails & Scope Controls
- **Diff Awareness**: Put each finding on the smallest relevant changed line. Focus comments primarily on changed lines of code. Do not comment on unmodified lines unless directly impacted.
- **Explicit Citations Only**: Cite the exact `CODING_STANDARDS.md` section and rule for standards findings. For security or testing findings, state the concrete requirement. Skip findings with no specific, verifiable violation.
- **Do Not Comment On**: Stylistic choices, formatting, indentation, or import ordering handled by automated formatters/linters (ktlint, detekt, spotless). Skip shallow compliments or LGTM comments.
- **Avoid Duplication**: Do not repeat the same issue across multiple comments; group them or note all affected locations in a single comment.
- **No Speculation**: Do not raise speculative or low-confidence concerns. If unsure, ask for clarification instead of asserting an issue.
- **No CI/Sandbox Disclaimers**: Do not run Gradle build/test commands or include meta-commentary about sandbox environment limits.

### 8.3 Required Review Templates

#### Inline Comment Template
```markdown
### ⚠️ [CRITICAL | HIGH | MEDIUM | LOW] - <Brief Title>
**Violation**: <CODING_STANDARDS.md section or concrete requirement> — <specific rule>
**Impact**: <Clear, concise description of the problem and its potential impact>
**Suggested Fix**:
<Describe the solution and include a concrete refactored code snippet showing how to implement the fix>
```

#### Top-level Review Summary Template
```markdown
### 📝 Review Summary

<1-2 sentence overall assessment of the PR's quality>

**Verdict**: 🔴 **Requires changes** | 🟢 **Safe to merge**

| Severity | Component / Path | Finding Summary | Recommended Action |
| :--- | :--- | :--- | :--- |
| [CRITICAL | HIGH | MEDIUM | LOW] | `<path/to/file>` | <Brief summary of the issue> | <Recommended action or fix> |
```
*(If no issues are found, skip the table and write a concise, positive review summary).*

