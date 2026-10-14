# AI Agent Guidelines (AGENTS.md)

Welcome to the **Testpress Android Application** repository. This document establishes guidelines and mandatory constraints for all AI coding agents (including Google Antigravity, Claude Code, and GitHub Actions PR review agents) working on this codebase.

---

## 🚨 Mandatory Directive: Follow `CODING_STANDARDS.md`

All code generation, modifications, refactoring, and automated PR reviews **must strictly comply with** [CODING_STANDARDS.md](CODING_STANDARDS.md).

Before writing code or conducting a review:
1. Familiarize yourself with the principles in [CODING_STANDARDS.md](CODING_STANDARDS.md).
2. Ensure any new or modified code satisfies every relevant section:
   - **Architecture**: Enforce MVVM. UI components (Activities/Fragments/Composables) must contain zero business logic.
   - **Memory Safety**: Never pass `Activity` or `Fragment` context to ViewModels, singletons, or background tasks. Use `ApplicationContext` where needed.
   - **Localization**: Never hardcode UI string literals. Always define strings in `res/values/strings.xml`.
   - **Threading**: Perform all network requests and database queries on background threads (Coroutines / `TestpressCallback`). Never block the Main thread.
   - **Nullability**: Annotate Java methods with `@NonNull`/`@Nullable` and avoid Kotlin forced unwrapping (`!!`).
   - **Database**: Use GreenDAO / Room asynchronously; handle schema updates in `testpressdaogenerator`.

---

## 🛠 Project Build & Verification Commands

When testing or validating changes locally:
- **Assemble Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Run Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Run Lint Checks**:
  ```bash
  ./gradlew lintDebug
  ```

---

## 📋 PR Review Agent Guidelines

When operating as a PR review agent (e.g., via `.github/workflows/pr-review.yml`):
- Read [CODING_STANDARDS.md](CODING_STANDARDS.md) as the single source of truth for all architectural, quality, and severity criteria.
- Categorize all findings according to the standard severity levels:
  - `[CRITICAL]`: Crashes, security/token leaks, main-thread blocking operations, severe breaking regressions.
  - `[HIGH]`: Memory leaks (e.g., Activity/Fragment context leaks), coroutine cancellation bugs, state desynchronization.
  - `[MEDIUM]`: Actionable architecture/design improvements (e.g., hardcoded strings, god classes, inefficient DB queries).
  - `[LOW]`: Minor non-blocking code cleanups.
- Limit `[MEDIUM]` and `[LOW]` feedback to actionable, high-value improvements only.
- Adhere strictly to the required inline comment and review summary table templates.

---

## 📝 Commit Conventions

Follow the repository's conventional commit format:
- `<type>: <Subject line in imperative mood>` (e.g., `feat:`, `fix:`, `ref:`, `ci:`, `chore:`, `docs:`)
- Keep lines wrapped under 100 characters.
- Reference issues where applicable (e.g., `Fixes #123`).
