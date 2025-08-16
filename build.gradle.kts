// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Add this line (if you don't have a version catalog entry, use the id+version form below)
    id("com.google.dagger.hilt.android") version "2.52" apply false
    // ...
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
}