# Testpress Android App

[![Build Status](https://travis-ci.org/testpress/android.svg?branch=master)](https://travis-ci.org/testpress/android)

---

## 🛠️ Steps to Set Up

### 1. Clone the Repository
Clone the Testpress Android repository to your local machine using **SSH**:
```bash
git clone git@github.com:testpress/android.git
```

### 2. Open the Project in Android Studio
- Download and launch the **latest version of Android Studio**.
- Open the cloned `android` project folder.

### 3. Set Up Environment Variables
Add the following environment variables to your system environment:
- `GITHUB_USERNAME`: Your GitHub username
- `GITHUB_ACCESS_KEY`: Your GitHub classic personal access token

### 4. Restart Your System
Restart your computer to apply the environment variable changes.

### Completion
Once restarted, the setup is complete, and the app is ready for development.

---

## 🏗️ Architecture Overview

| Component | Responsibility / Code Contents |
| :--- | :--- |
| **Android SDK** (`android-sdk`) | Contains core reusable libraries, shared UI widgets, video player engine, API network wrappers, and core data logic shared across Testpress applications. |
| **Android App** (`android`) | Represents the host application container. Contains app-level navigation, white-label client configurations, app-specific UI/screens, dependencies, and manifest declarations. |

---

## 🔄 Development Workflows

### 1. App-Only Fixes
If an issue is isolated to the application layer (`android` repository):
1. Make the fix directly within the `android` repository.
2. Verify locally and raise a PR to `master`.

### 2. SDK-Dependent Fixes
If an issue resides inside the `android-sdk`:
1. **Implement Fix & Merge**: Fix the issue in `android-sdk` and merge the PR into `master`.
2. **Bump SDK Version**: Bump the SDK version in `android-sdk` so the release workflow creates a new Maven artifact.
3. **Publish Release**: Trigger the [release.yml](https://github.com/testpress/android-sdk/actions/workflows/release.yml) workflow in `android-sdk` to publish the new version.
4. **Update App Dependency**: Update the `testpressSdk` version variable in the `android` repository.
5. **Verify & Merge**: Test locally, raise a PR in `android`, and merge to `master`.

---

## 🌿 Special Client Branches Workflow

Certain institutes maintain dedicated long-running branches for specific customizations (e.g., `brilliantpalalms`, `brilliantpalaelearn`).

### Case 1: Institute-Specific Fixes ONLY
- Perform code changes directly on the special branch (e.g., `brilliantpalalms`).
- **Do NOT merge these changes into `master`**.
- Verify locally and push directly to remote.

### Case 2: Incorporating `master` Changes into Special Branch
1. **Merge fix to `master`**: Ensure all general fixes are committed and merged into `master`.
2. **Switch & Fetch**:
   ```bash
   git checkout brilliantpalalms
   git fetch origin
   ```
3. **Merge `master` into Special Branch**:
   ```bash
   git merge origin/master
   ```
4. **Clean Build & Verify**:
   ```bash
   ./gradlew clean
   ```
   Launch the app locally and test with test credentials (`698547` / `workbook@123`).
5. **Push Updated Branch**:
   ```bash
   git push origin brilliantpalalms
   ```

---

## 🚀 Build & Release Workflows (GitHub Actions)

### Available Workflows

| Workflow Name | Action Link | Purpose |
| :--- | :--- | :--- |
| **Generate Debug APK** | [generate_debug_apk.yml](https://github.com/testpress/android/actions/workflows/generate_debug_apk.yml) | Generates an unsigned/debug APK for rapid developer testing and validation. |
| **Build App** | [build_app.yml](https://github.com/testpress/android/actions/workflows/build_app.yml) | Compiles and generates the release build (APK/AAB) for `master` or a special branch. |
| **Release Update** | [release_update.yml](https://github.com/testpress/android/actions/workflows/release_update.yml) | Publishes the app update to Google Play Console for target institute clients. |

### Release Procedure

Follow this exact sequence for creating builds and releasing updates for both standard releases and special client releases:

1. **Step 1: Generate Release Build APK (`build_app.yml`)**
   - Open the [build_app.yml Workflow](https://github.com/testpress/android/actions/workflows/build_app.yml).
   - Click **Run workflow** and select the target branch:
     - **Normal Flow**: Leave branch as `master`.
     - **Special Client Branch Flow**: Change branch name to the specific institute branch (e.g., `brilliantpalalms` or `brilliantpalaelearn`).
   - Click **Run workflow** and wait for completion.
   - Download the generated build APK artifact.

2. **Step 2: Client Verification via Support Team**
   - If the update is client-specific, take the generated APK or copy the `build_app.yml` action run link.
   - Share the APK/link with the Support Team and ask them to verify the build with the client team.
   - ⚠️ **CRITICAL RULE**: Do **NOT** trigger the release action until explicit confirmation/approval is received from the Support Team / Client.

3. **Step 3: Trigger Release Update Action (`release_update.yml`)**
   - Open the [release_update.yml Workflow](https://github.com/testpress/android/actions/workflows/release_update.yml).
   - Click **Run workflow** and configure the parameters:
     - **Branch**:
       - **Normal Flow**: Use `master`.
       - **Special Client Branch Flow**: Use the special branch name (e.g., `brilliantpalalms` or `brilliantpalaelearn`).
     - **Institute URL**: Enter the target client/institute URL.
     - **Release Mode**:
       - **Draft**: Uploads the release package to Google Play Console as a draft release without auto-publishing immediately (allows manual staging/review).
       - **Complete**: Uploads and publishes the update directly on Google Play Console for the institute's users.
   - Click **Run workflow** to publish the app update.



