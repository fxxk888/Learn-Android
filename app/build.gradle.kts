import com.android.build.api.artifact.SingleArtifact

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fuck.learn"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.fuck.learn"
        minSdk = 26
        targetSdk = 36
        versionCode = 22000
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
//    implementation(libs.androidx.material3)
    implementation(libs.reorderable)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui.graphics)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.commons.codec)
    implementation(libs.rhino)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.transformations)

    // mmkv
    implementation(libs.mmkv.static)
    implementation(libs.crashreport)

    // room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.reorderable)

}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val variantName = variant.name.replaceFirstChar { it.uppercase() }

        val versionNameProvider = variant.outputs.first().versionName
        val vName = versionNameProvider.getOrElse("1.0")

        val projectName = rootProject.name

        val copyAndRenameApkTask = tasks.register<Copy>("copyAndRename${variantName}Apk") {
            group = "custom build"

            val apkDir = variant.artifacts.get(SingleArtifact.APK)
            from(apkDir)
            include("**/*.apk")
            into(layout.projectDirectory.dir("releases/${vName}"))

            rename { originalFileName ->
                "${projectName}-v${vName}.apk"
            }
        }

        project.tasks.matching { it.name == "assemble$variantName" }.configureEach {
            finalizedBy(copyAndRenameApkTask)
        }
    }
}