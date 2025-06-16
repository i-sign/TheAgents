import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kspDevTools)
}

val localProps = Properties()
if (rootProject.file("local.properties").exists()) {
    localProps.load(rootProject.file("local.properties").inputStream())
}
val isDebug = localProps.getOrDefault("debug", "false") == "true"
val matrixUrl = localProps.getOrDefault("matrixUrl", "").toString()

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            api(libs.trixnity.client.media.okio)
            implementation(libs.androidx.sqlite3.bundled)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.voyager.navigator)
            implementation(compose.materialIconsExtended)
            implementation(libs.napier)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            api(libs.koin.core)
            api(libs.trixnity.client)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.logging)
            implementation(libs.multiplatform.settings)
            api(libs.trixnity.client.repository.room)
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.trixnity.client.repository.room)
                api(libs.trixnity.client.media.okio)
                implementation(libs.androidx.sqlite3.bundled)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.signin.theagents"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.signin.theagents"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/**"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    useKotlinOutput { internalVisibility = true }
    buildConfigField("boolean", "DEBUG", "$isDebug")
    buildConfigField("String", "MATRIX_URL", "$matrixUrl")
}

