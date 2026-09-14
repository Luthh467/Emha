package com.example.data

import kotlin.math.roundToInt

data class KemenkesBmiClassification(
    val categoryName: String,
    val rangeText: String,
    val minBmi: Float,
    val maxBmi: Float,
    val statusDescription: String,
    val actionRecommendation: String,
    val colorHex: Long,
    val isIdeal: Boolean = false
)

data class BmrTdeeResult(
    val bmr: Float, // BMT / Basal Metabolic Rate (kkal)
    val tdee: Float, // Total Daily Energy Expenditure (kkal)
    val activityFactor: Float,
    val activityLevelName: String,
    val breakfastCalories: Int,
    val lunchCalories: Int,
    val dinnerCalories: Int,
    val snackCalories: Int,
    val targetAdvice: String
)

data class NutritionEvaluationResult(
    val bmi: Float,
    val bmiCategory: String, // "Gizi Kurang (Sangat Kurus)", "Gizi Kurang (Kurus Ringan)", "Gizi Baik (Normal)", "Gizi Lebih", "Obesitas"
    val riskCategory: String, // "Risiko Rendah", "Perlu Perhatian", "Risiko Tinggi"
    val factorsToWatch: String,
    val generalAdvice: String,
    val heightInMeters: Float = 0f,
    val heightSquared: Float = 0f,
    val idealWeightKg: Float = 0f,
    val healthyWeightMin: Float = 0f,
    val healthyWeightMax: Float = 0f,
    val classification: KemenkesBmiClassification? = null
)

object NutritionCalculator {

    /**
     * Klasifikasi Status Gizi berdasarkan Ambang Batas IMT Standar Kemenkes RI
     * (Rujukan: Pedoman Gizi Seimbang Kemenkes RI & Permenkes No. 2 Tahun 2020)
     */
    val KEMENKES_STANDARDS = listOf(
        KemenkesBmiClassification(
            categoryName = "Kurus Tingkat Berat (Sangat Kurus)",
            rangeText = "< 17.0",
            minBmi = 0f,
            maxBmi = 16.99f,
            statusDescription = "Kekurangan berat badan tingkat berat. Kondisi ini berisiko menyebabkan anemia, daya tahan tubuh rendah, dan Kurang Energi Kronis (KEK).",
            actionRecommendation = "Perlu peningkatan asupan energi dan protein secara bertahap dengan menu seimbang, serta konsultasi berkala ke petugas UKS / Puskesmas.",
            colorHex = 0xFF2563EB, // Blue
            isIdeal = false
        ),
        KemenkesBmiClassification(
            categoryName = "Kurus Tingkat Ringan (Gizi Kurang)",
            rangeText = "17.0 - 18.4",
            minBmi = 17.0f,
            maxBmi = 18.49f,
            statusDescription = "Kekurangan berat badan tingkat ringan. Cadangan energi tubuh belum optimal untuk aktivitas harian remaja.",
            actionRecommendation = "Tingkatkan porsi makan teratur 3 kali sehari diselingi camilan sehat padat gizi (buah, kacang-kacangan, susu).",
            colorHex = 0xFF0284C7, // Light blue / Sky
            isIdeal = false
        ),
        KemenkesBmiClassification(
            categoryName = "Normal (Gizi Baik / Ideal)",
            rangeText = "18.5 - 25.0",
            minBmi = 18.5f,
            maxBmi = 25.0f,
            statusDescription = "Status gizi normal dan ideal sesuai standar Kementerian Kesehatan RI. Keseimbangan energi dan metabolisme tubuh terjaga dengan baik.",
            actionRecommendation = "Pertahankan pola makan Isi Piringku (1/2 sayur-buah, 1/3 karbohidrat, 1/6 lauk protein), cukupi minum air putih, dan olahraga rutin minimal 30 menit sehari.",
            colorHex = 0xFF16A34A, // Green (Safe)
            isIdeal = true
        ),
        KemenkesBmiClassification(
            categoryName = "Gemuk Tingkat Ringan (Gizi Lebih / Overweight)",
            rangeText = "25.1 - 27.0",
            minBmi = 25.1f,
            maxBmi = 27.0f,
            statusDescription = "Kelebihan berat badan tingkat ringan. Asupan kalori melebihi kebutuhan energi harian tubuh.",
            actionRecommendation = "Batasi konsumsi makanan/minuman tinggi GGL (Gula, Garam, Lemak), hindari camilan manis, dan tingkatkan frekuensi aktivitas fisik.",
            colorHex = 0xFFD97706, // Amber
            isIdeal = false
        ),
        KemenkesBmiClassification(
            categoryName = "Gemuk Tingkat Berat (Obesitas)",
            rangeText = "> 27.0",
            minBmi = 27.01f,
            maxBmi = 100f,
            statusDescription = "Kelebihan berat badan tingkat berat. Berisiko meningkatkan risiko penyakit tidak menular (hipertensi, resistensi insulin, sindrom metabolik) di usia muda.",
            actionRecommendation = "Disarankan evaluasi pola makan dengan pendampingan petugas UKS / tenaga gizi Puskesmas serta program latihan fisik teratur.",
            colorHex = 0xFFDC2626, // Red
            isIdeal = false
        )
    )

    /**
     * Rumus IMT Standar Kemenkes:
     * IMT = Berat Badan (kg) / (Tinggi Badan (m))^2
     */
    fun calculateBmi(weightKg: Float, heightCm: Float): Float {
        if (heightCm <= 0f || weightKg <= 0f) return 0f
        val heightM = heightCm / 100f
        val rawBmi = weightKg / (heightM * heightM)
        return (rawBmi * 10f).roundToInt() / 10f
    }

    /**
     * Mendapatkan klasifikasi Kemenkes yang sesuai dengan nilai IMT
     */
    fun getKemenkesClassification(bmi: Float): KemenkesBmiClassification {
        return when {
            bmi < 17.0f -> KEMENKES_STANDARDS[0]
            bmi < 18.5f -> KEMENKES_STANDARDS[1]
            bmi <= 25.0f -> KEMENKES_STANDARDS[2]
            bmi <= 27.0f -> KEMENKES_STANDARDS[3]
            else -> KEMENKES_STANDARDS[4]
        }
    }

    /**
     * Perhitungan Berat Badan Ideal (BBI) menurut Rumus Broca Modifikasi Kemenkes RI:
     * Laki-laki / Perempuan: BBI = (TB - 100) - (10% x (TB - 100))
     */
    fun calculateIdealWeightBroca(heightCm: Float, gender: String = "Laki-laki"): Float {
        if (heightCm <= 100f) return 0f
        val diff = heightCm - 100f
        val percentage = if (gender.contains("Perempuan", ignoreCase = true)) 0.15f else 0.10f
        val bbi = diff - (percentage * diff)
        return (bbi * 10f).roundToInt() / 10f
    }

    /**
     * Rentang Berat Badan Normal Sehat Kemenkes (berdasarkan IMT 18.5 - 25.0):
     * BB Min = 18.5 * (TB dalam meter)^2
     * BB Max = 25.0 * (TB dalam meter)^2
     */
    fun calculateHealthyWeightRange(heightCm: Float): Pair<Float, Float> {
        if (heightCm <= 0f) return Pair(0f, 0f)
        val heightM = heightCm / 100f
        val heightSquared = heightM * heightM
        val min = 18.5f * heightSquared
        val max = 25.0f * heightSquared
        return Pair(
            (min * 10f).roundToInt() / 10f,
            (max * 10f).roundToInt() / 10f
        )
    }

    /**
     * Menghitung BMT (Basal Metabolic Tariif / Rate / Angka Metabolisme Basal - AMB)
     * Menggunakan Rumus Standar Mifflin-St Jeor:
     * Laki-laki: (10 × BB) + (6.25 × TB) - (5 × Usia) + 5
     * Perempuan: (10 × BB) + (6.25 × TB) - (5 × Usia) - 161
     *
     * Dan menghitung TDEE (Total Daily Energy Expenditure):
     * TDEE = BMT × Faktor Aktivitas Fisik (Physical Activity Level)
     */
    fun calculateBmrTdee(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: String,
        physicalActivityDuration: String = "30–60 menit",
        bmiCategory: String = "Normal"
    ): BmrTdeeResult {
        val safeWeight = if (weightKg <= 0f) 50f else weightKg
        val safeHeight = if (heightCm <= 0f) 160f else heightCm
        val safeAge = if (age <= 0) 16 else age
        val isFemale = gender.contains("Perempuan", ignoreCase = true) || gender.contains("Wanita", ignoreCase = true)

        // Rumus Mifflin-St Jeor
        val rawBmr = if (isFemale) {
            (10f * safeWeight) + (6.25f * safeHeight) - (5f * safeAge) - 161f
        } else {
            (10f * safeWeight) + (6.25f * safeHeight) - (5f * safeAge) + 5f
        }
        val bmr = (rawBmr * 10f).roundToInt() / 10f

        // Penentuan Faktor Aktivitas Fisik (Kemenkes RI)
        val (factor, activityLabel) = when {
            physicalActivityDuration.contains("> 60", ignoreCase = true) ||
            physicalActivityDuration.contains("Berat", ignoreCase = true) ||
            physicalActivityDuration.contains("Sangat Aktif", ignoreCase = true) -> {
                1.725f to "Aktif / Ekstrakurikuler (>60 mnt)"
            }
            physicalActivityDuration.contains("30–60", ignoreCase = true) ||
            physicalActivityDuration.contains("30-60", ignoreCase = true) ||
            physicalActivityDuration.contains("Sedang", ignoreCase = true) -> {
                1.55f to "Sedang / Olahraga Rutin (30–60 mnt)"
            }
            physicalActivityDuration.contains("< 30", ignoreCase = true) ||
            physicalActivityDuration.contains("Ringan", ignoreCase = true) -> {
                1.375f to "Ringan / Jalan Santai (<30 mnt)"
            }
            else -> {
                1.2f to "Sangat Ringan / Sedentari (Banyak Duduk)"
            }
        }

        val tdee = ((bmr * factor) * 10f).roundToInt() / 10f

        // Proporsi Pembagian Kalori Santap Harian Standar Kemenkes
        val breakfastCal = (tdee * 0.25f).roundToInt()
        val lunchCal = (tdee * 0.35f).roundToInt()
        val dinnerCal = (tdee * 0.25f).roundToInt()
        val snackCal = (tdee * 0.15f).roundToInt()

        val targetAdvice = when {
            bmiCategory.contains("Kurus", ignoreCase = true) || bmiCategory.contains("Kurang", ignoreCase = true) -> {
                "Target kalori pemulihan: penuhi minimal ${(tdee + 300).toInt()} kkal/hari dengan gizi padat nutrisi untuk mencapai berat badan ideal."
            }
            bmiCategory.contains("Lebih", ignoreCase = true) || bmiCategory.contains("Obesitas", ignoreCase = true) -> {
                "Target kalori seimbang: batasi asupan manis/gorengan hingga rentang ${(tdee - 300).toInt()} - ${tdee.toInt()} kkal/hari diimbangi aktif gerak."
            }
            else -> {
                "Target kalori seimbang: jaga asupan harian sekitar ${tdee.toInt()} kkal/hari dengan komposisi piring gizi seimbang."
            }
        }

        return BmrTdeeResult(
            bmr = bmr,
            tdee = tdee,
            activityFactor = factor,
            activityLevelName = activityLabel,
            breakfastCalories = breakfastCal,
            lunchCalories = lunchCal,
            dinnerCalories = dinnerCal,
            snackCalories = snackCal,
            targetAdvice = targetAdvice
        )
    }

    fun evaluateNutrition(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: String,
        breakfastHabit: String,
        vegHabit: String,
        fruitHabit: String,
        fastFoodHabit: String,
        sugaryDrinkHabit: String,
        physicalActivity: String,
        sedentaryDuration: String
    ): NutritionEvaluationResult {
        val heightM = heightCm / 100f
        val heightSquared = (heightM * heightM * 10000f).roundToInt() / 10000f
        val bmi = calculateBmi(weightKg, heightCm)
        val classification = getKemenkesClassification(bmi)

        val idealWeight = calculateIdealWeightBroca(heightCm, gender)
        val healthyRange = calculateHealthyWeightRange(heightCm)

        // Penyesuaian nama kategori agar kompatibel dengan filter UKS
        val bmiCategory = when {
            bmi < 17.0f -> "Gizi Kurang (Sangat Kurus)"
            bmi < 18.5f -> "Gizi Kurang (Kurus Ringan)"
            bmi <= 25.0f -> "Gizi Baik (Normal)"
            bmi <= 27.0f -> "Gizi Lebih (Overweight)"
            else -> "Obesitas"
        }

        var riskScore = 0
        val factors = mutableListOf<String>()

        // Evaluasi Risiko berdasarkan Ambang Batas IMT Kemenkes
        when {
            bmi < 17.0f || bmi > 27.0f -> {
                riskScore += 3
                factors.add("Nilai IMT ($bmi kg/m²) berada di kategori ${classification.categoryName} menurut Kemenkes RI.")
            }
            bmi < 18.5f || bmi > 25.0f -> {
                riskScore += 2
                factors.add("Nilai IMT ($bmi kg/m²) mengarah ke batas ${if (bmi < 18.5f) "Gizi Kurang" else "Gizi Lebih"}.")
            }
        }

        // Kebiasaan Sarapan
        if (breakfastHabit.contains("Jarang", ignoreCase = true)) {
            riskScore += 2
            factors.add("Sering melewatkan sarapan pagi sebelum belajar di madrasah.")
        } else if (breakfastHabit.contains("Kadang", ignoreCase = true)) {
            riskScore += 1
            factors.add("Kebiasaan sarapan belum rutin setiap hari.")
        }

        // Konsumsi Sayur
        if (vegHabit.contains("Jarang", ignoreCase = true)) {
            riskScore += 2
            factors.add("Kurang asupan serat dan mikronutrien dari sayur-sayuran.")
        }

        // Konsumsi Buah
        if (fruitHabit.contains("Jarang", ignoreCase = true)) {
            riskScore += 1
            factors.add("Jarang mengonsumsi buah segar sebagai sumber vitamin alami.")
        }

        // Gorengan & Fast Food
        if (fastFoodHabit.contains(">= 3x", ignoreCase = true) || fastFoodHabit.contains("Sering", ignoreCase = true)) {
            riskScore += 2
            factors.add("Tingginya frekuensi konsumsi makanan cepat saji atau gorengan kantin.")
        }

        // Minuman Manis
        if (sugaryDrinkHabit.contains(">= 2", ignoreCase = true) || sugaryDrinkHabit.contains("Tiap Hari", ignoreCase = true)) {
            riskScore += 2
            factors.add("Asupan gula berlebih dari minuman manis kemasan / es manis madrasah.")
        }

        // Aktivitas Fisik
        if (physicalActivity.contains("Kurang", ignoreCase = true)) {
            riskScore += 2
            factors.add("Aktivitas fisik harian kurang dari 30 menit rekomendasi Kemenkes.")
        }

        // Durasi Duduk / Layar
        if (sedentaryDuration.contains("> 6", ignoreCase = true)) {
            riskScore += 1
            factors.add("Durasi duduk santai / menatap gawai melebihi 6 jam per hari.")
        }

        // Penentuan Kategori Risiko Skrining
        val riskCategory = when {
            riskScore <= 2 -> "Risiko Rendah"
            riskScore in 3..5 -> "Perlu Perhatian"
            else -> "Risiko Tinggi"
        }

        val factorsString = if (factors.isEmpty()) {
            "Tidak ditemukan faktor risiko signifikan. Pola hidup harian dan antropometri sudah memenuhi standar sehat Kemenkes."
        } else {
            factors.joinToString(separator = "\n• ", prefix = "• ")
        }

        // Saran Edukatif Terstruktur Kemenkes
        val advice = buildString {
            append(classification.actionRecommendation)
            append(" ")
            when (riskCategory) {
                "Risiko Rendah" -> {
                    append("Pertahankan capaian positif ini dengan mematuhi pilar Gizi Seimbang Kemenkes dan tetap aktif berolahraga.")
                }
                "Perlu Perhatian" -> {
                    append("Disarankan melakukan konsultasi gizi ringan dengan petugas UKS madrasah untuk memperbaiki kebiasaan sarapan dan asupan serat.")
                }
                else -> {
                    append("Sangat dianjurkan berkonsultasi dengan petugas UKS madrasah atau tenaga gizi di Puskesmas untuk evaluasi antropometri berkala dan perencanaan menu gizi seimbang.")
                }
            }
        }

        return NutritionEvaluationResult(
            bmi = bmi,
            bmiCategory = bmiCategory,
            riskCategory = riskCategory,
            factorsToWatch = factorsString,
            generalAdvice = advice,
            heightInMeters = heightM,
            heightSquared = heightSquared,
            idealWeightKg = idealWeight,
            healthyWeightMin = healthyRange.first,
            healthyWeightMax = healthyRange.second,
            classification = classification
        )
    }
}
