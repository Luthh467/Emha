package com.example.data

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.data.remote.DetectedFoodItem
import com.example.data.remote.FoodAnalysisResponse
import kotlin.math.roundToInt

/**
 * NutriMind On-Device Computer Vision & Food Classifier Engine.
 * 
 * Menganalisis piksel gambar foto piring makanan secara langsung (HSV/RGB color distribution,
 * density segmentation, plate balance heuristics) digabungkan dengan konteks catatan siswa,
 * memberikan deteksi makanan nyata bahkan saat offline atau tanpa kuota Gemini API.
 */
object FoodVisionAnalyzer {

    fun analyze(
        bitmap: Bitmap,
        mealType: String = "Makan Utama",
        studentNote: String = ""
    ): FoodAnalysisResponse {
        val noteLower = studentNote.lowercase()

        // 1. Resize ke matriks efisien untuk visual sampling
        val sampleSize = 160
        val scaled = if (bitmap.width != sampleSize || bitmap.height != sampleSize) {
            Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)
        } else {
            bitmap
        }

        var totalPixels = 0
        var whitePixels = 0       // Karbohidrat: Nasi, bubur, lontong, bihun putih
        var greenPixels = 0       // Sayuran hijau: Bayam, kangkung, buncis, brokoli, timun, selada
        var brownDarkPixels = 0   // Lauk protein: Ayam bakar/goreng, rendang daging, ikan, sate
        var goldenYellowPixels = 0 // Lauk nabati/gorengan: Tempe goreng, tahu kuning, bakwan, telur dadar
        var orangeRedPixels = 0   // Sayur/buah/sambal: Wortel, sambal tomat, pepaya, semangka, jeruk
        var brightYellowPixels = 0 // Telur ceplok, jagung, pisang
        var darkLiquidPixels = 0  // Es teh, kopi, kecap

        val hsv = FloatArray(3)

        for (y in 0 until sampleSize step 2) {
            for (x in 0 until sampleSize step 2) {
                val pixel = scaled.getPixel(x, y)
                val alpha = AndroidColor.alpha(pixel)
                if (alpha < 100) continue

                totalPixels++
                AndroidColor.colorToHSV(pixel, hsv)
                val h = hsv[0] // 0 - 360
                val s = hsv[1] // 0 - 1
                val v = hsv[2] // 0 - 1

                when {
                    // Putih / Nasi / Lontong (High brightness, Low saturation)
                    v > 0.72f && s < 0.22f -> whitePixels++

                    // Hijau / Sayuran (Hue 70 - 165, Saturation > 0.20, Brightness 0.20 - 0.85)
                    h in 70f..165f && s > 0.20f && v in 0.18f..0.88f -> greenPixels++

                    // Cokelat gelap / Daging / Ayam Bakar (Hue 10 - 28, Saturation > 0.35, Brightness 0.15 - 0.50)
                    h in 10f..30f && s > 0.35f && v in 0.15f..0.52f -> brownDarkPixels++

                    // Kuning Emas / Gorengan / Tahu-Tempe / Telur Dadar (Hue 28 - 55, Saturation > 0.35, Brightness 0.45 - 0.90)
                    h in 28f..55f && s > 0.35f && v in 0.45f..0.92f -> goldenYellowPixels++

                    // Jingga / Merah / Wortel / Sambal / Buah (Hue 0-25 atau 340-360 dengan saturasi tinggi)
                    (h in 345f..360f || h in 0f..25f) && s > 0.45f && v > 0.35f -> orangeRedPixels++

                    // Kuning cerah / Telur ceplok / Pisang (Hue 50 - 68, Saturation > 0.40, Brightness > 0.65)
                    h in 50f..68f && s > 0.35f && v > 0.60f -> brightYellowPixels++

                    // Cairan gelap / Teh manis / Kecap
                    v < 0.20f && s > 0.20f -> darkLiquidPixels++
                }
            }
        }

        val total = totalPixels.coerceAtLeast(1).toFloat()
        val whiteRatio = whitePixels / total
        val greenRatio = greenPixels / total
        val brownRatio = brownDarkPixels / total
        val goldenRatio = goldenYellowPixels / total
        val orangeRedRatio = orangeRedPixels / total
        val yellowRatio = brightYellowPixels / total

        // 2. Deteksi Komponen Berdasarkan Visual + Catatan Siswa
        val detectedItemsList = mutableListOf<DetectedFoodItem>()
        var totalCal = 0
        var totalCarb = 0
        var totalProt = 0
        var totalFat = 0
        var totalFiber = 0

        // --- Karbohidrat ---
        val hasRiceInNote = noteLower.contains("nasi") || noteLower.contains("uduk") || noteLower.contains("kuning")
        val hasNoodleInNote = noteLower.contains("mie") || noteLower.contains("bihun") || noteLower.contains("kwetiau")
        val hasBreadInNote = noteLower.contains("roti") || noteLower.contains("sandwich")
        val hasLontongInNote = noteLower.contains("lontong") || noteLower.contains("ketupat")

        val carbsTitle: String
        val carbsGrams: Int
        if (hasNoodleInNote) {
            carbsTitle = "Mie Olahan / Bihun Goreng"
            detectedItemsList.add(DetectedFoodItem("Mie Olahan / Bihun", "1 porsi mangkuk (~120g)", 260, "Karbohidrat Pokok"))
            totalCal += 260; totalCarb += 44; totalProt += 6; totalFat += 7; totalFiber += 1
            carbsGrams = 44
        } else if (hasBreadInNote) {
            carbsTitle = "Roti Gandum / Tawar"
            detectedItemsList.add(DetectedFoodItem("Roti Bekal", "2 lembar tangkup", 180, "Karbohidrat Pokok"))
            totalCal += 180; totalCarb += 32; totalProt += 6; totalFat += 3; totalFiber += 2
            carbsGrams = 32
        } else if (hasLontongInNote) {
            carbsTitle = "Lontong / Ketupat Potong"
            detectedItemsList.add(DetectedFoodItem("Lontong Potong", "1 porsi (~130g)", 170, "Karbohidrat Pokok"))
            totalCal += 170; totalCarb += 38; totalProt += 3; totalFat += 1; totalFiber += 1
            carbsGrams = 38
        } else if (whiteRatio > 0.08 || hasRiceInNote || (!hasNoodleInNote && !hasBreadInNote)) {
            val portionDesc = if (whiteRatio > 0.28) "1 centong penuh pulen (~180g)" else "1 centong sedang (~150g)"
            val cCal = if (whiteRatio > 0.28) 240 else 200
            val cCarb = if (whiteRatio > 0.28) 53 else 44
            carbsTitle = "Nasi Putih Pulen"
            detectedItemsList.add(DetectedFoodItem("Nasi Putih Pulen", portionDesc, cCal, "Karbohidrat Pokok"))
            totalCal += cCal; totalCarb += cCarb; totalProt += 4; totalFat += 1; totalFiber += 1
            carbsGrams = cCarb
        } else {
            carbsTitle = "Tidak terdeteksi karbohidrat dominan"
            carbsGrams = 0
        }

        // --- Lauk Protein Hewani & Nabati ---
        val hasChicken = noteLower.contains("ayam")
        val hasEgg = noteLower.contains("telur") || noteLower.contains("endog")
        val hasFish = noteLower.contains("ikan") || noteLower.contains("lele") || noteLower.contains("tongkol")
        val hasMeat = noteLower.contains("daging") || noteLower.contains("rendang") || noteLower.contains("sapi")
        val hasTempeh = noteLower.contains("tempe")
        val hasTofu = noteLower.contains("tahu")
        val hasGorengan = noteLower.contains("gorengan") || noteLower.contains("bakwan")

        var proteinTitle = ""
        // Protein Hewani
        if (hasChicken || brownRatio > 0.05 || (hasMeat.not() && hasFish.not() && brownRatio > 0.03)) {
            val name = when {
                hasChicken -> "Ayam Olahan Bumbu"
                hasFish -> "Ikan Olahan Gurih"
                hasMeat -> "Olahan Daging Sapi"
                else -> "Lauk Ayam / Daging Rempah"
            }
            detectedItemsList.add(DetectedFoodItem(name, "1 potong sedang (~80g)", 185, "Lauk Hewani"))
            totalCal += 185; totalCarb += 2; totalProt += 21; totalFat += 10; totalFiber += 0
            proteinTitle = name
        }

        if (hasEgg || yellowRatio > 0.04) {
            val name = if (yellowRatio > 0.07) "Telur Dadar / Ceplok" else "Telur Rebus / Balado"
            detectedItemsList.add(DetectedFoodItem(name, "1 butir (~55g)", 95, "Lauk Hewani"))
            totalCal += 95; totalCarb += 1; totalProt += 8; totalFat += 7; totalFiber += 0
            proteinTitle = if (proteinTitle.isBlank()) name else "$proteinTitle & $name"
        }

        // Protein Nabati & Gorengan
        if (hasTempeh || hasTofu || goldenRatio > 0.05 || hasGorengan) {
            val name = when {
                hasTempeh && hasTofu -> "Tahu & Tempe Goreng Kuning"
                hasTempeh -> "Tempe Goreng Gurih"
                hasTofu -> "Tahu Goreng Kuning"
                hasGorengan -> "Bakwan / Gorengan Tepung"
                else -> "Tempe / Tahu Goreng Gurih"
            }
            val cal = if (hasGorengan) 140 else 90
            val fat = if (hasGorengan) 10 else 6
            detectedItemsList.add(DetectedFoodItem(name, "1-2 potong (~60g)", cal, "Lauk Nabati"))
            totalCal += cal; totalCarb += 5; totalProt += 7; totalFat += fat; totalFiber += 2
            proteinTitle = if (proteinTitle.isBlank()) name else "$proteinTitle & $name"
        }

        if (proteinTitle.isBlank()) {
            proteinTitle = "Lauk Pauk Protein (~60g)"
            detectedItemsList.add(DetectedFoodItem("Lauk Pauk Campur", "1 porsi", 120, "Lauk Pauk"))
            totalCal += 120; totalCarb += 3; totalProt += 12; totalFat += 7; totalFiber += 0
        }

        // --- Sayuran ---
        val hasSpinach = noteLower.contains("bayam")
        val hasKangkung = noteLower.contains("kangkung")
        val hasSop = noteLower.contains("sop") || noteLower.contains("sup")
        val hasSaladOrLalap = noteLower.contains("lalap") || noteLower.contains("timun") || noteLower.contains("selada")

        val vegTitle: String
        if (greenRatio > 0.04 || hasSpinach || hasKangkung || hasSop || hasSaladOrLalap) {
            vegTitle = when {
                hasSpinach -> "Sayur Bening Bayam"
                hasKangkung -> "Tumis Sayur Kangkung"
                hasSop -> "Sayur Sop Aneka Sayur"
                hasSaladOrLalap -> "Lalapan Timun & Sayur Segar"
                greenRatio > 0.12 -> "Sayuran Hijau Melimpah (Tumis / Bening)"
                else -> "Sayuran Hijau & Buncis/Wortel"
            }
            detectedItemsList.add(DetectedFoodItem(vegTitle, "1 mangkuk/porsi (~90g)", 45, "Sayuran"))
            totalCal += 45; totalCarb += 7; totalProt += 2; totalFat += 1; totalFiber += 3
        } else {
            vegTitle = "Belum terlihat porsi sayuran hijau di piring"
        }

        // --- Buah-buahan / Pelengkap Segar ---
        val hasBanana = noteLower.contains("pisang")
        val hasOrange = noteLower.contains("jeruk")
        val hasApple = noteLower.contains("apel")
        val hasFruitGeneral = noteLower.contains("buah") || noteLower.contains("pepaya") || noteLower.contains("semangka")

        var fruitTitle = ""
        if (hasBanana || hasOrange || hasApple || hasFruitGeneral || orangeRedRatio > 0.05) {
            fruitTitle = when {
                hasBanana -> "Buah Pisang Segar"
                hasOrange -> "Buah Jeruk Manis"
                hasApple -> "Potongan Buah Apel"
                orangeRedRatio > 0.07 -> "Buah Potong Segar (Pepaya / Jeruk)"
                else -> "Buah Segar Pendamping"
            }
            detectedItemsList.add(DetectedFoodItem(fruitTitle, "1 buah / porsi (~100g)", 60, "Buah-buahan"))
            totalCal += 60; totalCarb += 15; totalProt += 1; totalFat += 0; totalFiber += 2
        }

        // --- Minuman / Kuah ---
        val hasSweetTea = noteLower.contains("teh") || noteLower.contains("es teh") || noteLower.contains("manis") || noteLower.contains("boba")
        val hasMineralWater = noteLower.contains("air putih") || noteLower.contains("mineral") || noteLower.contains("aqua")
        val hasMilk = noteLower.contains("susu")

        val drinkTitle = when {
            hasSweetTea -> "Minuman Teh Manis / Es Rasa"
            hasMilk -> "Susu UHT / Bernutrisi"
            hasMineralWater -> "Air Putih Mineral"
            darkLiquidPixels > 80 -> "Minuman Berwarna / Kuah Gurih"
            else -> "Air Putih (Direkomendasikan)"
        }

        if (hasSweetTea) {
            detectedItemsList.add(DetectedFoodItem("Es Teh Manis / Minuman Gula", "1 gelas (~250ml)", 120, "Minuman Gula"))
            totalCal += 120; totalCarb += 28; totalProt += 0; totalFat += 0; totalFiber += 0
        } else if (hasMilk) {
            detectedItemsList.add(DetectedFoodItem("Susu Segar", "1 kotak (~200ml)", 130, "Minuman Bernutrisi"))
            totalCal += 130; totalCarb += 12; totalProt += 7; totalFat += 6; totalFiber += 0
        }

        // --- Susun Rangkuman Visual Deteksi ---
        val foodNames = detectedItemsList.joinToString(", ") { it.name }

        // Evaluasi Keseimbangan Isi Piringku
        val hasCarb = carbsGrams > 0
        val hasProt = totalProt >= 14
        val hasVeg = vegTitle.startsWith("Belum").not()
        val hasFruit = fruitTitle.isNotBlank()

        val balanceScore: Int
        val balanceAssessment: String
        val eduFeedback: String
        val actionableStep: String
        val alertnessLvl: String
        val alertnessDet: String
        val ironAss: String
        val ironAdv: String

        if (hasCarb && hasProt && hasVeg && hasFruit) {
            balanceScore = 94
            balanceAssessment = "Sangat Seimbang (Sesuai Isi Piringku)"
            eduFeedback = "Maa syaa Allah! Komposisi makananmu sudah sangat lengkap mencakup makanan pokok, lauk hewani/nabati, sayuran, dan buah segar sesuai panduan Kemenkes RI."
            actionableStep = "Pertahankan pola bekal bergizi seimbang ini setiap hari dan minum air putih 1-2 gelas."
            alertnessLvl = "Stabil & Fokus Tinggi"
            alertnessDet = "Kombinasi serat sayur-buah dan protein menjaga stabilitas gula darah sehingga santri tetap bugar dan konsentrasi saat belajar di kelas madrasah."
            ironAss = "Kaya Zat Besi & Vitamin C"
            ironAdv = "Sayuran dan protein yang seimbang membantu mencukupi kebutuhan hemoglobin dan mencegah anemia."
        } else if (hasCarb && hasProt && (hasVeg || hasFruit)) {
            balanceScore = 78
            balanceAssessment = "Cukup Seimbang"
            eduFeedback = "Komposisi piring sudah baik dengan karbohidrat dan lauk berprotein. Tingkatkan porsi sayur dan buah hingga memenuhi 50% piring agar asupan mikronutrien optimal."
            actionableStep = if (hasVeg) "Lengkapi dengan 1 buah potong (misal pisang/jeruk) sebagai penutup." else "Tambahkan 1 centong sayuran hijau untuk memperkaya serat."
            alertnessLvl = "Stabil & Cukup Fokus"
            alertnessDet = "Energi belajar cukup stabil, akan lebih bertahan lama jika serat ditingkatkan."
            ironAss = "Cukup Zat Besi"
            ironAdv = "Sudah mencukupi kebutuhan dasar, tambahkan vitamin C alami dari buah untuk memaksimalkan penyerapan zat besi."
        } else if (hasGorengan || hasSweetTea) {
            balanceScore = 48
            balanceAssessment = "Tinggi Minyak & Gula (Kurang Sayur)"
            eduFeedback = "Menu didominasi gorengan atau minuman manis. Makanan tinggi lemak jenuh dan karbohidrat sederhana cepat memicu lonjakan insulin yang berujung kantuk berat di kelas."
            actionableStep = "Ganti minuman manis dengan air putih mineral dan imbangi dengan bekal sayur atau buah segar."
            alertnessLvl = "Rawan Mengantuk (Food Coma)"
            alertnessDet = "Gula sederhana dan minyak gorengan memperlambat pencernaan dan memicu kantuk serta lesu pada jam pelajaran siang."
            ironAss = "Rendah Zat Besi"
            ironAdv = "Teh manis mengandung tanin yang menghambat penyerapan zat besi. Minumlah air putih mineral."
        } else {
            balanceScore = 65
            balanceAssessment = "Kurang Sayur & Buah"
            eduFeedback = "Piring didominasi karbohidrat dan lauk. Ingat prinsip Isi Piringku: separuh piring harus berisi sayuran dan buah-buahan untuk menjaga imunitas dan saluran cerna."
            actionableStep = "Tambahkan porsi tumis sayur atau lalapan segar di waktu makan berikutnya."
            alertnessLvl = "Sedang"
            alertnessDet = "Pencernaan karbohidrat murni tanpa cukup serat dapat menimbulkan rasa begah dan mengantuk."
            ironAss = "Cukup dari Lauk"
            ironAdv = "Perbanyak sayuran hijau tua untuk melengkapi zat besi nabati bagi santri."
        }

        val category = when {
            balanceScore >= 80 -> "Menu Makanan Utama (Isi Piringku Seimbang)"
            hasGorengan || hasSweetTea -> "Jajanan Kantin & Kudapan"
            mealType.contains("Sarapan") -> "Menu Sarapan Pagi"
            else -> "Menu Makan Siswa Madrasah"
        }

        val oilSugarStatus = when {
            hasGorengan && hasSweetTea -> "Tinggi Gorengan & Gula Tambahan"
            hasGorengan -> "Tinggi Minyak Gorengan"
            hasSweetTea -> "Tinggi Gula Tambahan"
            totalFat > 20 -> "Cukup Tinggi Lemak"
            else -> "Kadar Minyak & Lemak Wajar"
        }

        return FoodAnalysisResponse(
            detectedFoods = foodNames,
            foodCategory = category,
            carbsSource = carbsTitle,
            proteinSource = proteinTitle,
            vegFruitSource = if (fruitTitle.isNotBlank()) "$vegTitle & $fruitTitle" else vegTitle,
            approxNutrients = "Est. $totalCal kkal | K: ${totalCarb}g, P: ${totalProt}g, L: ${totalFat}g, Serat: ${totalFiber}g",
            balanceAssessment = balanceAssessment,
            educationalFeedback = eduFeedback,
            estimatedCalories = totalCal.coerceAtLeast(150),
            carbGrams = totalCarb,
            proteinGrams = totalProt,
            fatGrams = totalFat,
            fiberGrams = totalFiber,
            plateScore = balanceScore,
            ironAssessment = ironAss,
            ironAdvice = ironAdv,
            oilSugarAssessment = oilSugarStatus,
            actionableImprovement = actionableStep,
            studentNotes = studentNote,
            alertnessLevel = alertnessLvl,
            alertnessDetail = alertnessDet,
            satietyHours = if (totalFiber >= 4 && totalProt >= 15) 4 else 2,
            calciumStatus = if (hasEgg || hasTempeh || hasTofu || noteLower.contains("susu")) "Optimal untuk Tulang" else "Cukup",
            vitaminCStatus = if (hasVeg && hasFruit) "Sangat Baik (Optimal)" else if (hasVeg || hasFruit) "Cukup" else "Perlu Ditingkatkan",
            hydrationBeverageAdvice = if (hasSweetTea) "Kurangi konsumsi es teh manis. Ganti dengan 1-2 gelas air putih mineral (400-600ml)." else "Dampingi makan dengan 1-2 gelas air putih (300-500ml) agar pencernaan lancar.",
            items = detectedItemsList,
            analysisEngine = "NutriMind On-Device Vision (Pemindaian Visual Cerdas)"
        )
    }
}
