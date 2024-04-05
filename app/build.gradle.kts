plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventsnapqr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eventsnapqr"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    configurations {
        named("androidTestImplementation") {
            exclude(group = "com.google.protobuf", module = "protobuf-lite")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}
allprojects {
    // forces all changing dependencies (i.e. SNAPSHOTs) to automatically download
    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(0, "seconds")
        }
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.concurrent:concurrent-listenablefuture:1.0.0-beta01")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("org.testng:testng:7.9.0")

    androidTestImplementation("androidx.fragment:fragment-testing:1.6.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-contrib:3.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-intents:3.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-web:3.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-accessibility:3.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-contrib:3.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-idling-resource:3.0.2")
    androidTestImplementation("com.google.firebase:firebase-storage:20.3.0")
    androidTestImplementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    androidTestImplementation("com.google.firebase:firebase-firestore:24.11.0")
    androidTestImplementation("com.firebaseui:firebase-ui-storage:8.0.2")
    androidTestImplementation("com.google.firebase:firebase-database:20.3.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation ("androidx.test:rules:1.5.0")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-android:5.11.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation ("org.osmdroid:osmdroid-mapsforge:6.1.18") 

    implementation("com.firebaseui:firebase-ui-storage:8.0.2")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("androidmads.library.qrgenearator:QRGenearator:1.0.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")


}
