buildscript {
    val agp_version by extra("8.1.4")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.android.library") version "8.1.1" apply false

    id("com.vanniktech.maven.publish") version "0.30.0" apply false
    id("com.gradleup.nmcp") version "0.0.8" apply false
}