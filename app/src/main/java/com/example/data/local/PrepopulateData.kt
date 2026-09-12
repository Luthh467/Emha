package com.example.data.local

import com.example.data.model.EducationArticle
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UserEntity

object PrepopulateData {
    val sampleUsers = emptyList<UserEntity>()

    val sampleNutritionChecks = emptyList<NutritionCheckEntity>()

    val articles = listOf(
        // Kategori A: Gizi Seimbang
        EducationArticle(
            title = "Mengenal Konsep 'Isi Piringku' untuk Remaja",
            category = "Gizi Seimbang",
            summary = "Panduan porsi makan seimbang setiap kali makan: 1/3 karbohidrat, 1/3 sayuran, 1/6 lauk protein, dan 1/6 buah-buahan segar.",
            content = "Konsep 'Isi Piringku' dari Kementerian Kesehatan merupakan pedoman visual praktis bagi siswa madrasah.\n\nDalam satu piring makan harian:\n1. Makanan Pokok (Karbohidrat): Mengisi sepertiga piring. Pilih nasi merah, ubi, kentang, atau jagung.\n2. Sayuran: Mengisi sepertiga piring dengan sayuran berwarna seperti bayam, kangkung, wortel, dan brokoli.\n3. Lauk-Pauk: Mengisi seperenam piring dengan variasi protein hewani (ikan, ayam, telur) dan protein nabati (tahu, tempe).\n4. Buah-buahan: Mengisi seperenam piring dengan buah segar seperti pisang, pepaya, atau jeruk.\n\nJangan lupa minum air putih minimal 8 gelas per hari dan cuci tangan dengan sabun sebelum makan.",
            readTimeMinutes = 3,
            recommendedForStatus = "ALL"
        ),
        EducationArticle(
            title = "Pentingnya Keberagaman Pangan Sehari-hari",
            category = "Gizi Seimbang",
            summary = "Mengapa tidak ada satu jenis makanan pun yang memiliki kandungan zat gizi lengkap bagi tubuh remaja.",
            content = "Setiap bahan pangan memiliki keunggulan zat gizi yang berbeda-beda. Misalnya, nasi kaya karbohidrat namun miskin kalsium; ikan kaya protein dan omega-3 namun rendah serat.\n\nOleh karena itu, mengonsumsi makanan yang beragam setiap hari memastikan kebutuhan mikronutrien (vitamin dan mineral) terpenuhi secara optimal untuk menunjang pertumbuhan fisik dan kecerdasan siswa madrasah.",
            readTimeMinutes = 2,
            recommendedForStatus = "ALL"
        ),

        // Kategori B: Sarapan
        EducationArticle(
            title = "Mengapa Sarapan Sangat Penting bagi Pelajar?",
            category = "Sarapan",
            summary = "Sarapan adalah sumber energi otak untuk berkonsentrasi penuh saat pelajaran di kelas pagi madrasah.",
            content = "Setelah berpuasa tidur semalam selama 7-8 jam, cadangan glukosa otak menurun drastis. Sarapan bernutrisi menyediakan glukosa yang dibutuhkan sel otak untuk memproses informasi baru, berhitung, dan menghafal.\n\nSiswa yang rutin sarapan terbukti memiliki daya ingat lebih tajam, tidak cepat lelah, serta terhindar dari rasa pusing saat upacara atau kegiatan belajar.",
            readTimeMinutes = 3,
            recommendedForStatus = "Gizi Kurang"
        ),
        EducationArticle(
            title = "Contoh Menu Sarapan Sederhana & Cepat",
            category = "Sarapan",
            summary = "Ide sarapan padat gizi yang mudah disiapkan sebelum berangkat ke madrasah tanpa memakan waktu lama.",
            content = "Bagi siswa yang terburu-buru, sarapan tidak harus selalu rumit:\n1. Nasi telur ceplok dengan potongan tomat dan ketimun.\n2. Roti tawar isi selai kacang atau telur dadar ditambah segelas susu hangat.\n3. Bubur havermut (oatmeal) dengan irisan pisang dan madu.\n4. Lontong sayur dengan tahu tempe.\n\nKuncinya adalah memadukan karbohidrat kompleks dengan protein agar rasa kenyang bertahan hingga waktu istirahat siang.",
            readTimeMinutes = 2,
            recommendedForStatus = "Perlu Perhatian"
        ),

        // Kategori C: Buah dan Sayur
        EducationArticle(
            title = "Manfaat Sayuran Hijau untuk Mencegah Anemia",
            category = "Buah dan Sayur",
            summary = "Sayuran berdaun hijau gelap kaya zat besi dan folat yang krusial bagi kebugaran remaja putri dan putra.",
            content = "Anemia defisiensi besi sering membuat siswa merasa 5L (Lesu, Lemah, Letih, Lelah, Lalai). Sayuran hijau seperti bayam, daun kelor, kangkung, dan sawi merupakan sumber zat besi nabati yang baik.\n\nTips: Kombinasikan sayuran dengan sumber vitamin C (seperti jeruk atau tomat) karena vitamin C membantu penyerapan zat besi hingga 3 kali lebih efektif!",
            readTimeMinutes = 3,
            recommendedForStatus = "Perlu Perhatian"
        ),
        EducationArticle(
            title = "Cara Mudah Menambah Porsi Buah Harian",
            category = "Buah dan Sayur",
            summary = "Trik sederhana agar tidak lupa mengonsumsi buah segar setiap hari di madrasah.",
            content = "Bawalah buah potong praktis dalam kotak bekal seperti apel, semangka, atau jeruk kupas. Mengganti camilan berminyak saat istirahat dengan sepotong pisang atau pepaya tidak hanya menyehatkan pencernaan, tetapi juga membuat kulit lebih cerah dan tubuh tetap segar sepanjang siang.",
            readTimeMinutes = 2,
            recommendedForStatus = "ALL"
        ),

        // Kategori D: Makanan dan Minuman
        EducationArticle(
            title = "Waspadai Bahaya Tersembunyi Minuman Manis (Boba & Kopi Susu)",
            category = "Makanan dan Minuman",
            summary = "Kandungan gula berlebih dalam minuman kekinian dan risikonya terhadap obesitas remaja.",
            content = "Kemenkes menganjurkan batas konsumsi gula maksimal 4 sendok makan (50 gram) per hari. Tahukah kamu bahwa satu gelas minuman boba atau teh manis kemasan sering kali mengandung 30 hingga 45 gram gula?\n\nMinuman manis memberikan kalori kosong tanpa serat maupun protein, memicu penumpukan lemak visceral, rasa kantuk saat jam belajar, dan meningkatkan risiko resistensi insulin sejak usia muda.",
            readTimeMinutes = 4,
            recommendedForStatus = "Gizi Lebih"
        ),
        EducationArticle(
            title = "Tips Cerdas Memilih Jajanan di Kantin Madrasah",
            category = "Makanan dan Minuman",
            summary = "Cara membedakan jajanan yang bernutrisi dengan jajanan yang tinggi garam dan lemak jenuh.",
            content = "Saat berada di kantin:\n1. Pilih makanan yang dimasak dengan cara direbus atau dikukus (seperti siomay, bakso kuah bening, jagung rebus).\n2. Batasi gorengan tepung yang menyerap minyak banyak.\n3. Selalu bawa botol air minum sendiri dari rumah untuk menghemat uang saku dan memastikan hidrasi yang sehat.",
            readTimeMinutes = 3,
            recommendedForStatus = "Risiko Tinggi"
        ),

        // Kategori E: Aktivitas Fisik
        EducationArticle(
            title = "Pentingnya Bergerak 30–60 Menit Setiap Hari",
            category = "Aktivitas Fisik",
            summary = "Aktivitas fisik teratur memperkuat otot, mengoptimalkan kepadatan tulang remaja, dan membakar kalori berlebih.",
            content = "Remaja disarankan melakukan aktivitas fisik aerobik minimal 60 menit setiap hari. Aktivitas ini tidak harus di tempat gym mahal:\n- Jalan kaki aktif ke madrasah\n- Bermain bulu tangkis, futsal, atau basket saat jam olahraga\n- Menggunakan tangga daripada lift\n- Membantu membersihkan rumah atau asrama madrasah.",
            readTimeMinutes = 2,
            recommendedForStatus = "Gizi Lebih"
        ),

        // Kategori F: Tidur dan Kebiasaan Sehat
        EducationArticle(
            title = "Hubungan Tidur Cukup dengan Keseimbangan Metabolisme",
            category = "Tidur & Sehat",
            summary = "Mengapa kurang tidur justru memicu nafsu makan berlebih dan kebiasaan ngemil tengah malam.",
            content = "Saat kita kurang tidur (tidur kurang dari 6 jam), tubuh memproduksi hormon ghrelin lebih banyak (hormon pemicu lapar) dan menekan hormon leptin (hormon sinyal kenyang).\n\nAkibatnya, di pagi dan siang hari kamu akan mendambakan makanan berkalori tinggi dan manis. Tidur yang cukup (7-9 jam) menjaga kestabilan metabolisme hormon gizi kita.",
            readTimeMinutes = 3,
            recommendedForStatus = "ALL"
        )
    )
}
