# Feature `build.gradle.kts` Template

Canonical Gradle setup for `feature/{featurename}/build.gradle.kts`, matching the project's current AGP 9 + Kotlin Multiplatform DSL.

**Placeholders:**
- `{featurename}` — lowercase feature name (matches directory and `xcfName`)
- `{PKG_PREFIX}` — package prefix detected in Phase 0 (e.g. `thisissadeghi`)

## Module Plugins

Every feature module applies these plugins. `kover` and `mokkery` are mandatory when the feature will ship tests (`feature-test` skill assumes them present):

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.mokkery)
}
```

> Note: SDK versions, JVM target (21), and the `androidResources.enable = true` flag are configured **once** in the root `build.gradle.kts` for any module that applies `com.android.kotlin.multiplatform.library`. Do NOT redeclare `compileSdk`, `minSdk`, `compileOptions`, or `jvmTarget` per-feature.

## Targets

```kotlin
kotlin {
    android {
        namespace = "{PKG_PREFIX}.{featurename}"
    }
    jvm("desktop")

    val xcfName = "{featurename}"

    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

    sourceSets {
        commonMain {
            dependencies {
                // ... see "Standard commonMain deps" below
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.testing.common)
                implementation(libs.compose.ui.test)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
```

## Standard `commonMain` deps

Use the dependency set the existing features already use; the test-generation skill expects these to be present.

**Always include:**
```kotlin
implementation(libs.compose.foundation)
implementation(libs.compose.ui)
implementation(libs.compose.ui.util)
implementation(libs.compose.material.icons.extended)
implementation(libs.compose.material3)
implementation(libs.compose.components.resources)
implementation(libs.compose.ui.tooling.preview)   // enables @Preview in commonMain (CMP 1.11.0+)

implementation(libs.kotlinCollection)
implementation(libs.kotlinxSerialization)
implementation(libs.koin.compose)
implementation(libs.koin.compose.viewmodel)
api(libs.koin.core)
implementation(libs.jetbrains.compose.navigation)

implementation(project(":core:designsystem"))
implementation(project(":core:common"))
```

**Add only if the feature talks to an API** (uses Ktor Resources + `ApiClient`):
```kotlin
implementation(libs.ktor.client.resources)
implementation(project(":core:data"))
```

## Platform-specific dependencies (Rule 14 — platform capability / native view)

Only when the feature uses a device capability or embeds a native view (Platform Profile = `platform-capability` / `native-view` / `mixed`). See [platform.md](./platform.md) for the decision tree.

**Option 1 — multiplatform library** (single `commonMain` dep, no `expect/actual`):
```kotlin
commonMain {
    dependencies {
        implementation(libs.maplibre.compose)   // e.g. MapLibre Compose — cross-platform Map() composable
    }
}
```

**Option 2 — per-platform SDKs** (with `expect/actual`): add each SDK to its source set. The default hierarchy already provides `androidMain` / `iosMain` / `desktopMain` (intermediate `iosMain` covers all three iOS targets):
```kotlin
sourceSets {
    androidMain.dependencies {
        implementation(libs.play.services.maps)        // Android-only SDK
        implementation(libs.androidx.activity.compose)  // if AndroidView needs Activity context
    }
    // iosMain / desktopMain dependencies as needed (often none — iOS uses platform frameworks)
}
```

**iOS native frameworks** (MapLibre, MapKit-via-SPM, etc.) — required even when the Kotlin API is a `commonMain` lib. Configure on the iOS targets and export:
```kotlin
// Swift Package Manager
listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.swiftPackageConfig {
        dependency {
            remotePackageVersion(
                url = URI("https://github.com/maplibre/maplibre-gl-native-distribution.git"),
                products = { add("MapLibre", exportToKotlin = true) },
                packageName = "maplibre-gl-native-distribution",
                version = "<version>",
            )
        }
    }
    target.binaries.framework { baseName = xcfName; export(/* lib if it must cross the framework boundary */) }
}
// — or CocoaPods —
cocoapods { pod("MapLibre", "<version>") }
```

> When a platform DataSource needs the Android `Context`, the `actual` class takes it via constructor and the `androidMain` `platformModule` binds it with `androidContext()` — no extra Gradle dep beyond what `:core:*` already provides.

## Module-level `dependencies` block (Android preview renderer)

After the `kotlin { ... }` block, add a top-level `dependencies` block so Android Studio can render `@Preview` composables:

```kotlin
dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
```

Both `compose-ui-tooling-preview` and `compose-ui-tooling` aliases already exist in `gradle/libs.versions.toml`. With these wired, components in `presentation/ui/components/` can declare `@Preview` composables in commonMain using `import androidx.compose.ui.tooling.preview.Preview`. See [ui.md → "Previews"](./ui.md) for the full preview pattern.

> **Do NOT add** `androidx.lifecycle.viewmodel` or `androidx.lifecycle.runtime.compose` — `:core:common` already `api`-exposes both. `collectAsStateWithLifecycle` and `ViewModel` are available transitively.

## Add the `commonTest` source set only when needed

Some features ship without tests at first; in that case omit `commonTest` and `desktopTest` blocks. `/feature-test` will inject them later, but the `kover`/`mokkery` plugins must already be in the `plugins {}` block (see top).

## Adding to the test-deps later

If you create a feature without tests and later run `/feature-test {featurename}`, that command will add:

```kotlin
commonTest {
    dependencies {
        implementation(libs.bundles.testing.common)

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.uiTest)
        implementation(libs.turbine)
        implementation(libs.ktor.client.mock)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.client.resources)
    }
}

val desktopTest by getting {
    dependencies {
        implementation(compose.desktop.currentOs)
    }
}
```

## Reference

- Root configuration (SDK, JVM 21, ktlint, kover aggregation): `build.gradle.kts`
- Module include: `settings.gradle.kts` → `include(":feature:{featurename}")`
