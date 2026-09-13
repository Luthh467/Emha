import java.text.SimpleDateFormat
import java.util.Date

// Root build.gradle.kts
// Mendefinisikan fungsi/variabel timestamp global yang dapat digunakan di semua sub-modul (seperti :app)
val buildTimestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
extra["buildTimestamp"] = buildTimestamp
extra["buildVersionCode"] = SimpleDateFormat("yyMMddHH").format(Date()).toInt()

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

allprojects {
    // Menyediakan informasi build ke seluruh subproject
    extra["appBuildTime"] = buildTimestamp
}
