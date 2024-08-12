import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.jerson.hcdc_portal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jerson.hcdc_portal"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"

        val prop = Properties()
        prop.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String","key1",prop.getProperty("key1"))
        buildConfigField("String","key2",prop.getProperty("key2"))
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        disable += listOf("NotifyDataSetChanged", "SetTextI18n","checkReleaseBuilds")
        baseline = file("lint-baseline.xml")
    }

    afterEvaluate {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
        }
    }

    buildFeatures {
        viewBinding = true
    }
    buildFeatures.buildConfig = true

}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.webkit:webkit:1.11.0")


    // Web Scraping Library
    implementation("org.jsoup:jsoup:1.17.2")
    // Arch components
    implementation("androidx.lifecycle:lifecycle-process:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

//    Okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-coroutines:5.0.0-alpha.12")
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")

    // Dagger - Hilt
    implementation("androidx.hilt:hilt-work:1.2.0")
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Image Loading
    implementation("io.coil-kt:coil:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    // Image View
    implementation("com.github.KotatsuApp:subsampling-scale-image-view:169806d928")

    // Gson - JSON Object Parser
    implementation("com.google.code.gson:gson:2.10.1")

    //Room & RxJava Support
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-rxjava2:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // RxJava
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Coroutine Lifecycle Scopes
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")


    implementation("com.hendraanggrian.material:collapsingtoolbarlayout-subtitle:1.5.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Leak Canary
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:3.0-alpha-1")
}