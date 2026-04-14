plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    compileSdk = 34
    namespace = "com.jbk.alerte.coupure.faso"

    defaultConfig {
        applicationId = "com.jbk.alerte.coupure.faso"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- BIBLIOTHÈQUES ANDROID DE BASE ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- NAVIGATION ET ACTIVITY ---
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    // --- SERVICES GOOGLE & LOCALISATION ---
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // --- FIREBASE (Gestion par BoM - Très bien !) ---
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // --- UI EFFECTS & CHARTS ---
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- IMAGE LOADING (Nettoyé : Uniquement Glide 4.16.0) ---
    // Note : J'ai supprimé Picasso car avoir deux libs d'images alourdit l'app inutilement
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // --- ROOM (Base de données locale) ---
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // --- TESTS ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}