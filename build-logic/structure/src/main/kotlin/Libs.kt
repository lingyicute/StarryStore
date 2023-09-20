import org.gradle.kotlin.dsl.DependencyHandlerScope

object AndroidX {
	const val appCompat = "androidx.appcompat:appcompat:1.6.1"
	const val desugar = "com.android.tools:desugar_jdk_libs:2.0.3"
	const val material = "com.google.android.material:material:1.9.0"
	const val recyclerView = "androidx.recyclerview:recyclerview:1.3.1"
}

object Core {
	private const val coreVersion = "1.10.1"
	const val core = "androidx.core:core-ktx:$coreVersion"
}

object Coil {
	private const val coilVersion = "2.4.0"
	const val coil = "io.coil-kt:coil:$coilVersion"
}

private object Compose {
	const val bom = "androidx.compose:compose-bom:2023.08.00"

	const val animation = "androidx.compose.animation:animation"
	const val ui = "androidx.compose.ui:ui"
	const val tooling = "androidx.compose.ui:ui-tooling"
	const val preview = "androidx.compose.ui:ui-tooling-preview"
	const val foundation = "androidx.compose.foundation:foundation"
	const val runtime = "androidx.compose.runtime:runtime"
	const val material3 = "androidx.compose.material3:material3"
}

private object Coroutines {
	private const val coroutinesVersion = "1.7.3"
	const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
	const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
}

object Datastore {
	private const val version = "1.0.0"
	const val datastore = "androidx.datastore:datastore-preferences:$version"
}

object Excludes {
	val listExclude: List<String> = listOf(
		"/DebugProbesKt.bin",
		"/kotlin/**.kotlin_builtins",
		"/kotlin/**.kotlin_metadata",
		"/META-INF/**.kotlin_module",
		"/META-INF/**.pro",
		"/META-INF/**.version",
		"/META-INF/versions/9/previous-**.bin"
	)
}

private object FDroid {
	private const val indexVersion = "0.1.1"
	const val download = "org.fdroid:download:$indexVersion"
	const val index = "org.fdroid:index:$indexVersion"
}

object Hilt {
	const val version = "2.48"
	private const val androidXHilt = "1.0.0"
	const val plugin = "com.google.dagger.hilt.android"

	const val android = "com.google.dagger:hilt-android:$version"
	const val compiler = "com.google.dagger:hilt-compiler:$version"
	const val work = "androidx.hilt:hilt-work:$androidXHilt"
	const val androidX = "androidx.hilt:hilt-compiler:$androidXHilt"
}

object Jackson {
	const val core = "com.fasterxml.jackson.core:jackson-core:2.15.2"
}

object Kotlin {
	const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0"
	const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.1"
}

object Ksp {
	const val plugin = "com.google.devtools.ksp"
}

private object Ktor {
	private const val version = "2.3.4"
	const val core = "io.ktor:ktor-client-core:$version"
	const val okhttp = "io.ktor:ktor-client-okhttp:$version"
}

private object Lifecycle {
	private const val lifecycleVersion = "2.6.1"
	const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion"
	const val fragment = "androidx.fragment:fragment-ktx:1.6.1"
	const val activity = "androidx.activity:activity-ktx:1.7.2"
}

object Others {
	const val libsu = "com.github.topjohnwu.libsu:core:5.2.1"
	const val zoomage = "com.jsibbold:zoomage:1.3.1"
}

private object Room {
	private const val roomVersion = "2.5.2"
	const val runtime = "androidx.room:room-runtime:$roomVersion"
	const val compiler = "androidx.room:room-compiler:$roomVersion"
	const val ktx = "androidx.room:room-ktx:$roomVersion"
}

object Shizuku {
	private const val version = "13.0.0"
	const val api = "dev.rikka.shizuku:api:$version"
	const val provider = "dev.rikka.shizuku:provider:$version"
}

object SQLite {
	private const val version = "2.3.1"
	const val ktx = "androidx.sqlite:sqlite-ktx:$version"
}

object Test {
	const val jUnitRunner = "androidx.test.runner.AndroidJUnitRunner"
	const val jUnit = "junit:junit:4.13.2"
	const val androidJUnit = "androidx.test.ext:junit:1.1.5"
	const val espresso = "androidx.test.espresso:espresso-core:3.5.1"
}

object Work {
	private const val version = "2.8.1"
	const val manager = "androidx.work:work-runtime-ktx:$version"
}

fun DependencyHandlerScope.androidX() {
	add("implementation", AndroidX.appCompat)
	add("implementation", AndroidX.material)
	add("implementation", AndroidX.recyclerView)
}

fun DependencyHandlerScope.compose() {
	add("implementation", platform(Compose.bom))
	add("implementation", Compose.animation)
	add("implementation", Compose.ui)
	add("implementation", Compose.foundation)
	add("implementation", Compose.runtime)
	add("implementation", Compose.material3)
	add("implementation", Compose.preview)
	add("debugImplementation", Compose.tooling)
}

fun DependencyHandlerScope.coroutines() {
	add("implementation", Coroutines.core)
	add("implementation", Coroutines.android)
}

fun DependencyHandlerScope.desugar() {
	add("coreLibraryDesugaring", AndroidX.desugar)
}

fun DependencyHandlerScope.fdroid() {
	add("implementation", FDroid.index)
	add("implementation", FDroid.download)
}

fun DependencyHandlerScope.hilt(includeWork: Boolean = false) {
	add("implementation", Hilt.android)
	add("kapt", Hilt.compiler)
	if (includeWork) {
		add("implementation", Hilt.work)
		add("kapt", Hilt.androidX)
	}
}

fun DependencyHandlerScope.ktor() {
	add("implementation", Ktor.core)
	add("implementation", Ktor.okhttp)
}

fun DependencyHandlerScope.lifecycle() {
	add("implementation", Lifecycle.activity)
	add("implementation", Lifecycle.fragment)
	add("implementation", Lifecycle.viewmodel)
}

fun DependencyHandlerScope.recyclerView() {
	add("implementation", AndroidX.recyclerView)
}

fun DependencyHandlerScope.room() {
	add("implementation", Room.ktx)
	add("implementation", Room.runtime)
	add("ksp", Room.compiler)
}