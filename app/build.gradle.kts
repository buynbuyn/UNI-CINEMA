plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.uni_cinema"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uni_cinema"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }


    buildTypes {
        debug {
            buildConfigField ("String", "VNP_TMN_CODE", "\"YYEMP9YC\"")
            buildConfigField ("String", "VNP_HASH_SECRET", "\"ANDG1IXPFGZL9MBYJDJRUMDZ83L79GCJ\"")
            buildConfigField ("String", "VNP_URL", "\"https://sandbox.vnpayment.vn/paymentv2/vpcpay.html\"")
            buildConfigField ("String", "VNP_RETURN_URL", "\"uni_cinema://payment-result\"")
        }
        release {
            android.buildFeatures.buildConfig = true
            buildConfigField ("String", "VNP_TMN_CODE", "\"YOUR_PRODUCTION_TMN_CODE\"")
            buildConfigField ("String", "VNP_HASH_SECRET", "\"YOUR_PRODUCTION_HASH_SECRET\"")
            buildConfigField ("String", "VNP_URL", "\"https://pay.vnpay.vn/vpcpay.html\"")
            buildConfigField ("String", "VNP_RETURN_URL", "\"uni_cinema://payment-result\"")
        }
        release {
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2") // Use Java version
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2") // Use Java version
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0") // Use stable version
    implementation("com.google.code.gson:gson:2.10.1") // Đã cập nhật phiên bản phổ biến
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Đã cập nhật phiên bản phổ biến
    implementation(files("libs/merchant-1.0.25.aar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    // thu viện để ẩn hiện mật khẩu
    implementation ("com.google.android.material:material:1.10.0")

}