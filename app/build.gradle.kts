import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.incomecontrol.tkrzq"
    minSdk = 24
    targetSdk = 36
    versionCode = 13
    versionName = "1.12"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val envKeystorePath = System.getenv("KEYSTORE_FILE")
    val envKeystorePassword = System.getenv("KEYSTORE_PASSWORD")
    val envKeyAlias = System.getenv("KEY_ALIAS")
    val envKeyPassword = System.getenv("KEY_PASSWORD")

    val hasEnvSigning = !envKeystorePath.isNullOrBlank() &&
        file(envKeystorePath).exists() &&
        !envKeystorePassword.isNullOrBlank() &&
        !envKeyAlias.isNullOrBlank() &&
        !envKeyPassword.isNullOrBlank()

    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    val hasPropsSigning = if (!hasEnvSigning && keystorePropsFile.exists()) {
      keystorePropsFile.inputStream().use { keystoreProps.load(it) }
      val storeFilePath = keystoreProps.getProperty("storeFile")
      !storeFilePath.isNullOrBlank() &&
          rootProject.file(storeFilePath).exists() &&
          !keystoreProps.getProperty("storePassword").isNullOrBlank() &&
          !keystoreProps.getProperty("keyAlias").isNullOrBlank() &&
          !keystoreProps.getProperty("keyPassword").isNullOrBlank()
    } else {
      false
    }

    if (hasEnvSigning) {
      create("release") {
        storeFile = file(envKeystorePath!!)
        storePassword = envKeystorePassword
        keyAlias = envKeyAlias
        keyPassword = envKeyPassword
      }
    } else if (hasPropsSigning) {
      create("release") {
        storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
        storePassword = keystoreProps.getProperty("storePassword")
        keyAlias = keystoreProps.getProperty("keyAlias")
        keyPassword = keystoreProps.getProperty("keyPassword")
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfigs.findByName("release")?.let {
        signingConfig = it
      }
    }
    debug {
      // Uses Android default debug keystore
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

ksp {
  arg("room.schemaLocation", "${projectDir}/schemas")
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.compose.material.icons.extended)

  // Room Database
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  // Testing
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.androidx.core)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
