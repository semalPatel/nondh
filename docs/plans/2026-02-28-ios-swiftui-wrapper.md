# iOS SwiftUI Wrapper Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Provide a minimal iOS SwiftUI app that hosts the shared Compose UI via a `UIViewControllerRepresentable` wrapper.

**Architecture:** Export the shared KMP module as an `XCFramework`, then create a SwiftUI app target that imports the framework and embeds `NotesViewController()`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, SwiftUI, Xcode.

---

### Task 1: Export shared as XCFramework

**Files:**
- Modify: `mobile/shared/build.gradle.kts`

**Step 1: Write the failing test**
- Not applicable: build configuration change.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :shared:assembleXCFramework`
- Expected: FAIL if XCFramework configuration missing.

**Step 3: Write minimal implementation**
- Configure `shared` KMP module to produce an `XCFramework` named `shared` for iOS targets.
- Ensure it includes `iosX64`, `iosArm64`, `iosSimulatorArm64` binaries.

**Step 4: Run tests to verify it passes**
- Run: `./gradlew :shared:assembleXCFramework`
- Expected: SUCCESS, framework appears at `mobile/shared/build/XCFrameworks/debug/shared.xcframework`.

**Step 5: Commit**
```bash
git add mobile/shared/build.gradle.kts
git commit -m "build(ios): export shared xcframework"
```

### Task 2: Create SwiftUI wrapper app

**Files:**
- Create: `mobile/iosApp/iosApp/App.swift`
- Create: `mobile/iosApp/iosApp/ComposeView.swift`
- Modify: `mobile/iosApp/iosApp/ContentView.swift`

**Step 1: Write the failing test**
- Not applicable.

**Step 2: Run test to verify it fails**
- Manual build in Xcode will fail until framework is linked.

**Step 3: Write minimal implementation**
- `App.swift`: minimal `@main` SwiftUI app using `ComposeView()`.
- `ComposeView.swift`: `UIViewControllerRepresentable` that returns `NotesViewController()` from the shared framework.
- `ContentView.swift`: keep minimal or redirect to `ComposeView`.

**Step 4: Run tests to verify it passes**
- Manual: build and run iOS app after linking framework.

**Step 5: Commit**
```bash
git add mobile/iosApp/iosApp/App.swift mobile/iosApp/iosApp/ComposeView.swift mobile/iosApp/iosApp/ContentView.swift
git commit -m "feat(ios): add swiftui wrapper"
```

### Task 3: Add Xcode project wiring for framework

**Files:**
- Create: `mobile/iosApp/iosApp.xcodeproj/...` (Xcode project files)
- Create: `mobile/iosApp/Frameworks/shared.xcframework` (build phase copy or script)
- Create: `mobile/iosApp/build.sh` (optional script to sync framework)

**Step 1: Write the failing test**
- Not applicable.

**Step 2: Run test to verify it fails**
- Manual build in Xcode should fail before linking framework.

**Step 3: Write minimal implementation**
- Create an Xcode project with one app target.
- Add a build phase script to run `./gradlew :shared:assembleXCFramework` and copy the resulting `shared.xcframework` into the app bundle or a local `Frameworks` directory.
- Link `shared.xcframework` in the target’s Frameworks, Libraries, and Embedded Content.

**Step 4: Run tests to verify it passes**
- Manual: build + run in iOS simulator.

**Step 5: Commit**
```bash
git add mobile/iosApp
git commit -m "feat(ios): add xcode project and framework wiring"
```

