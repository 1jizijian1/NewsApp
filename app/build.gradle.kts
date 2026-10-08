plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)          // ★ Room 要靠它生成代码
}

android {
    namespace = "com.example.newsapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.newsapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // ★ 协程：lifecycle-runtime-ktx 带来 lifecycleScope，并会传递依赖 kotlinx-coroutines
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.recyclerview)

    // ★ Room：runtime 是运行时，ktx 让 DAO 能写 suspend 函数，compiler 由 KSP 生成代码
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ★ Markwon：把 Markdown 渲染成带格式的文字
    implementation(libs.markwon.core)
    implementation(libs.markwon.html)      // 认识 README 里的 <div> <br> 这些
    implementation(libs.markwon.tables)    // 表格
    implementation(libs.markwon.linkify)   // 裸网址自动变可点链接

    // ★ 启动画面（Android 12 官方做法，低版本由这个库模拟）
    implementation(libs.androidx.core.splashscreen)
}