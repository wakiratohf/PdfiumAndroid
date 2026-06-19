plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

// Bản Groovy tham chiếu `versionName` trong library defaultConfig — thuộc tính này không tồn
// tại trong DSL library (AGP) nên Groovy resolve về null. Library DSL mới (Kotlin) bỏ hẳn
// versionName, nên khai báo null tường minh để GIỮ ĐÚNG output cũ:
//   BuildConfig.VERSION_NAME = "null", AAR = "PdfiumAndroid-null-<buildType>.aar".
val pdfiumVersionName: String? = null

android {
    namespace = "com.shockwave.pdfium"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        buildConfigField("String", "VERSION_NAME", "\"$pdfiumVersionName\"")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            jni.setSrcDirs(emptyList<String>())
            jniLibs.srcDir("src/main/jni/lib")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    ndkVersion = "28.2.13676358"

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // Configure AAR filename to include version
    val libVersionName = pdfiumVersionName
    libraryVariants.all {
        val buildTypeName = buildType.name
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "PdfiumAndroid-$libVersionName-$buildTypeName.aar"
        }
    }

    publishing {
        // Configura la pubblicazione per la variante 'release'
        singleVariant("release") {}
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core)
}
