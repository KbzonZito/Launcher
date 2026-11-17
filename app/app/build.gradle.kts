import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.baselineprofile) // [fork]
}

android {
    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("en", "es") // [fork] solo español e inglés
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("META-INF/ASL2.0")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/NOTICE.md")
    }

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = libs.versions.compileSdkMinor.get().toInt()
        }
    }
    // [fork] Firma personal: la misma clave PKCS12 que Bookloft (~/.config/biblioteca/release.p12).
    // En local la exporta scripts/deploy.sh; en CI viene de los secretos del repo.
    // Sin estas variables, release queda SIN firmar (no instalable) en vez de caer a la clave de debug.
    val personalSigning = signingConfigs.create("personal") {
        storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEYSTORE_PASSWORD")
        storeType = "PKCS12"
    }
    val hasPersonalSigning = personalSigning.storeFile?.exists() == true &&
            !personalSigning.storePassword.isNullOrEmpty() && !personalSigning.keyAlias.isNullOrEmpty()
    if (!hasPersonalSigning) {
        logger.warn("[signing] KEYSTORE_PATH/KEYSTORE_PASSWORD/KEY_ALIAS no definidos: release sin firmar, debug con clave de debug.")
    }

    defaultConfig {
        // [fork] applicationId propio: convive con Kvaesitso oficial. El namespace Kotlin sigue siendo dev.kbzon.launcher.
        applicationId = "dev.kbzon.launcher"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        // [fork] versionCode = nº de commits de HEAD: siempre crece en main, así cada APK actualiza al anterior.
        versionCode = System.getenv("VERSION_CODE_OVERRIDE")?.toIntOrNull()
            ?: providers.exec { commandLine("git", "rev-list", "--count", "HEAD") }
                .standardOutput.asText.get().trim().toInt()
        versionName = "0.3.2"
        signingConfig = if (hasPersonalSigning) personalSigning else null
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // [fork] .dev: se instala junto al daily driver para probar sin romper la home.
            applicationIdSuffix = ".dev"
            isDebuggable = true
            signingConfig = if (hasPersonalSigning) personalSigning else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    lint {
        abortOnError = false
    }
    namespace = "dev.kbzon.launcher"
    buildFeatures {
        buildConfig = true
    }
}


dependencies {
    implementation(libs.bundles.kotlin)

    //Android Jetpack
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)

    implementation(libs.coil.core)
    implementation(libs.coil.svg)

    implementation(libs.koin.android)
    implementation(libs.androidx.work) // [fork] migración de tareas tras el renombrado
    implementation(libs.androidx.profileinstaller) // [fork] instala el baseline profile
    baselineProfile(project(":baselineprofile")) // [fork]

    implementation(project(":data:applications"))
    implementation(project(":data:appshortcuts"))
    implementation(project(":services:backup"))
    implementation(project(":services:badges"))
    implementation(project(":core:base"))
    implementation(project(":data:calculator"))
    implementation(project(":data:calendar"))
    implementation(project(":data:contacts"))
    implementation(project(":core:crashreporter"))
    implementation(project(":data:currencies"))
    implementation(project(":data:customattrs"))
    implementation(project(":data:searchable"))
    implementation(project(":data:themes"))
    implementation(project(":data:i18n"))
    implementation(project(":data:files"))
    implementation(project(":core:i18n"))
    implementation(project(":services:icons"))
    implementation(project(":core:ktx"))
    implementation(project(":services:music"))
    implementation(project(":data:notifications"))
    implementation(project(":core:permissions"))
    implementation(project(":core:profiles"))
    implementation(project(":core:preferences"))
    implementation(project(":services:search"))
    implementation(project(":services:tags"))
    implementation(project(":data:unitconverter"))
    implementation(project(":app:ui"))
    implementation(project(":data:weather"))
    implementation(project(":data:websites"))
    implementation(project(":data:widgets"))
    implementation(project(":data:database"))
    implementation(project(":data:search-actions"))
    implementation(project(":services:global-actions"))
    implementation(project(":services:widgets"))
    implementation(project(":services:favorites"))
    implementation(project(":data:locations"))
    implementation(project(":core:devicepose"))
    implementation(project(":services:feed"))

    // Uncomment this if you want annoying notifications in your debug builds
    //debugImplementation(libs.leakcanary)
}
