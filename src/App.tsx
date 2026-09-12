import React, { useState } from 'react';

export default function App() {
  const [activeTab, setActiveTab] = useState<'calculator' | 'questionnaire' | 'piringku' | 'apk'>('calculator');

  // State Kalkulator BMT & TDEE
  const [gender, setGender] = useState<'male' | 'female'>('male');
  const [age, setAge] = useState<number>(15);
  const [weight, setWeight] = useState<number>(50);
  const [height, setHeight] = useState<number>(160);
  const [activity, setActivity] = useState<number>(1.375); // Ringan
  const [calcResult, setCalcResult] = useState<any>(null);

  // State Kuisioner Singkat
  const [qBreakfast, setQBreakfast] = useState<'yes' | 'no'>('yes');
  const [qWater, setQWater] = useState<'sufficient' | 'less'>('sufficient');
  const [qVeggies, setQVeggies] = useState<'yes' | 'rare'>('yes');
  const [qFriedSnacks, setQFriedSnacks] = useState<'low' | 'frequent'>('low');
  const [qDizziness, setQDizziness] = useState<'no' | 'sometimes'>('no');
  const [qSubmitted, setQSubmitted] = useState(false);

  // State Modal APK Download
  const [showApkModal, setShowApkModal] = useState(false);

  const calculateBmtTdee = (e: React.FormEvent) => {
    e.preventDefault();
    const hMeter = height / 100;
    const bmi = +(weight / (hMeter * hMeter)).toFixed(1);

    let status = 'Normal (Gizi Baik)';
    let statusColor = '#047857';
    let statusBg = '#ecfdf5';

    if (bmi < 17.0) {
      status = 'Sangat Kurus (Kekurangan Berat Badan Berat)';
      statusColor = '#e11d48';
      statusBg = '#ffe4e6';
    } else if (bmi >= 17.0 && bmi < 18.5) {
      status = 'Kurus (Kekurangan Berat Badan Ringan)';
      statusColor = '#d97706';
      statusBg = '#fef3c7';
    } else if (bmi >= 18.5 && bmi <= 25.0) {
      status = 'Normal (Gizi Seimbang & Ideal)';
      statusColor = '#047857';
      statusBg = '#ecfdf5';
    } else if (bmi > 25.0 && bmi <= 27.0) {
      status = 'Gemuk (Kelebihan Berat Badan Ringan)';
      statusColor = '#d97706';
      statusBg = '#fef3c7';
    } else {
      status = 'Obesitas (Kelebihan Berat Badan Tingkat Tinggi)';
      statusColor = '#e11d48';
      statusBg = '#ffe4e6';
    }

    // Rumus BMR (Mifflin-St Jeor)
    let bmr = 10 * weight + 6.25 * height - 5 * age;
    if (gender === 'male') {
      bmr += 5;
    } else {
      bmr -= 161;
    }
    const tdee = Math.round(bmr * activity);

    setCalcResult({
      bmi,
      status,
      statusColor,
      statusBg,
      bmr: Math.round(bmr),
      tdee,
      carbsMin: Math.round((tdee * 0.5) / 4),
      carbsMax: Math.round((tdee * 0.65) / 4),
      proteinMin: Math.round((tdee * 0.15) / 4),
      proteinMax: Math.round((tdee * 0.2) / 4),
      fatMin: Math.round((tdee * 0.2) / 9),
      fatMax: Math.round((tdee * 0.25) / 9),
      waterMl: Math.round(weight * 35)
    });
  };

  return (
    <div>
      {/* Navbar */}
      <header className="navbar">
        <div className="container nav-wrapper">
          <div className="brand">
            <span style={{ fontSize: '1.75rem' }}>🥗</span>
            <div>
              <div className="brand-name">NutriMind Madrasah</div>
              <div style={{ fontSize: '0.72rem', color: '#64748b' }}>AI Gizi & Kesehatan Remaja Siswa</div>
            </div>
            <span className="brand-badge">Kemenkes RI</span>
          </div>

          <div className="nav-links">
            <button
              onClick={() => setActiveTab('calculator')}
              className={`nav-link ${activeTab === 'calculator' ? 'active' : ''}`}
            >
              Kalkulator BMT
            </button>
            <button
              onClick={() => setActiveTab('questionnaire')}
              className={`nav-link ${activeTab === 'questionnaire' ? 'active' : ''}`}
            >
              Kuisioner Harian
            </button>
            <button
              onClick={() => setActiveTab('piringku')}
              className={`nav-link ${activeTab === 'piringku' ? 'active' : ''}`}
            >
              Isi Piringku
            </button>
            <button
              onClick={() => setShowApkModal(true)}
              className="btn-primary"
            >
              <span>📱</span> Unduh APK Android
            </button>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="hero container">
        <div className="hero-pill">
          <span>✨</span> Didukung Artificial Intelligence & Pedoman Kemenkes RI
        </div>
        <h1 className="hero-title">
          Sistem Pemantauan Gizi &amp; <span>Kesehatan Remaja Madrasah</span>
        </h1>
        <p className="hero-desc">
          Platform digital pendeteksi dini status gizi, kebutuhan kalori harian (BMT &amp; TDEE),
          skrining anemia remaja, serta analisis porsi makan seimbang untuk siswa-siswi madrasah.
        </p>

        <div className="hero-actions">
          <button onClick={() => setActiveTab('calculator')} className="btn-primary" style={{ padding: '0.75rem 1.5rem', fontSize: '1rem' }}>
            Mulai Hitung BMT &amp; Kalori
          </button>
          <a
            href="/NutriMind-Madrasah.apk"
            download="NutriMind-Madrasah.apk"
            className="btn-outline"
            style={{ padding: '0.75rem 1.5rem', fontSize: '1rem', textDecoration: 'none' }}
          >
            <span>📱</span> Unduh Aplikasi Android (.APK)
          </a>
        </div>
      </section>

      {/* Main Content Area */}
      <main className="container" style={{ marginTop: '2rem' }}>
        {/* Navigation Tabs */}
        <div className="tabs">
          <button
            className={`tab-btn ${activeTab === 'calculator' ? 'active' : ''}`}
            onClick={() => setActiveTab('calculator')}
          >
            📊 Kalkulator BMT &amp; TDEE
          </button>
          <button
            className={`tab-btn ${activeTab === 'questionnaire' ? 'active' : ''}`}
            onClick={() => setActiveTab('questionnaire')}
          >
            📋 Kuisioner Singkat Kesehatan
          </button>
          <button
            className={`tab-btn ${activeTab === 'piringku' ? 'active' : ''}`}
            onClick={() => setActiveTab('piringku')}
          >
            🍽️ Panduan 'Isi Piringku'
          </button>
          <button
            className={`tab-btn ${activeTab === 'apk' ? 'active' : ''}`}
            onClick={() => setActiveTab('apk')}
          >
            📱 Aplikasi Android &amp; Fitur AI
          </button>
        </div>

        {/* TAB 1: KALKULATOR BMT & TDEE */}
        {activeTab === 'calculator' && (
          <div className="grid-cards" style={{ gridTemplateColumns: '1.1fr 1fr' }}>
            <div className="card">
              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem', color: '#0f172a' }}>
                Form Pengukuran Gizi Siswa
              </h2>
              <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '1.25rem' }}>
                Masukkan data antropometri siswa untuk menghitung Indeks Massa Tubuh (BMT) dan estimasi kebutuhan energi total (TDEE).
              </p>

              <form onSubmit={calculateBmtTdee}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Jenis Kelamin</label>
                    <select
                      className="form-select"
                      value={gender}
                      onChange={(e) => setGender(e.target.value as any)}
                    >
                      <option value="male">Laki-laki (Putra)</option>
                      <option value="female">Perempuan (Putri)</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Usia (Tahun)</label>
                    <input
                      type="number"
                      className="form-input"
                      value={age}
                      min={10}
                      max={22}
                      onChange={(e) => setAge(+e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Berat Badan (kg)</label>
                    <input
                      type="number"
                      step="0.5"
                      className="form-input"
                      value={weight}
                      onChange={(e) => setWeight(+e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Tinggi Badan (cm)</label>
                    <input
                      type="number"
                      className="form-input"
                      value={height}
                      onChange={(e) => setHeight(+e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Tingkat Aktivitas Harian</label>
                  <select
                    className="form-select"
                    value={activity}
                    onChange={(e) => setActivity(+e.target.value)}
                  >
                    <option value={1.2}>Sedentari (Banyak duduk belajar, jarang olahraga)</option>
                    <option value={1.375}>Ringan (Jalan santai sekolah, olahraga 1-3 hari/minggu)</option>
                    <option value={1.55}>Sedang (Aktif ekskul/olahraga 3-5 hari/minggu)</option>
                    <option value={1.725}>Sangat Aktif (Latihan fisik intensif / atletik madrasah)</option>
                  </select>
                </div>

                <button type="submit" className="btn-primary" style={{ width: '100%', justifyContent: 'center', marginTop: '0.5rem' }}>
                  Hitung Status BMT &amp; TDEE Sekarang
                </button>
              </form>
            </div>

            {/* Hasil Perhitungan */}
            <div>
              {calcResult ? (
                <div className="card" style={{ border: `1.5px solid ${calcResult.statusColor}` }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#64748b' }}>Hasil Evaluasi Gizi</span>
                    <span
                      style={{
                        background: calcResult.statusBg,
                        color: calcResult.statusColor,
                        padding: '0.25rem 0.75rem',
                        borderRadius: '999px',
                        fontWeight: 700,
                        fontSize: '0.8rem'
                      }}
                    >
                      {calcResult.status}
                    </span>
                  </div>

                  <div style={{ margin: '1.25rem 0', textAlign: 'center' }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 800, color: calcResult.statusColor, lineHeight: 1 }}>
                      {calcResult.bmi} <span style={{ fontSize: '1rem', fontWeight: 600, color: '#64748b' }}>kg/m²</span>
                    </div>
                    <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.25rem' }}>
                      Indeks Massa Tubuh (BMT Kemenkes)
                    </div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
                    <div style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                      <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Basal Metabolic Rate (BMR)</div>
                      <div style={{ fontSize: '1.2rem', fontWeight: 700, color: '#0f172a' }}>{calcResult.bmr} <span style={{ fontSize: '0.75rem' }}>kkal</span></div>
                      <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Energi saat istirahat total</div>
                    </div>

                    <div style={{ background: '#ecfdf5', padding: '0.85rem', borderRadius: '10px', border: '1px solid rgba(4, 120, 87, 0.2)' }}>
                      <div style={{ fontSize: '0.75rem', color: '#047857' }}>Kebutuhan Harian (TDEE)</div>
                      <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#047857' }}>{calcResult.tdee} <span style={{ fontSize: '0.75rem' }}>kkal</span></div>
                      <div style={{ fontSize: '0.7rem', color: '#065f46' }}>Total energi aktivitas harian</div>
                    </div>
                  </div>

                  <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                    <div style={{ fontSize: '0.85rem', fontWeight: 700, marginBottom: '0.5rem', color: '#0f172a' }}>
                      Rekomendasi Makronutrien Harian:
                    </div>
                    <ul style={{ listStyle: 'none', fontSize: '0.825rem', color: '#334155', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                      <li>🍞 <strong>Karbohidrat:</strong> {calcResult.carbsMin} - {calcResult.carbsMax} gram (50-65%)</li>
                      <li>🥩 <strong>Protein:</strong> {calcResult.proteinMin} - {calcResult.proteinMax} gram (15-20%)</li>
                      <li>🥑 <strong>Lemak Sehat:</strong> {calcResult.fatMin} - {calcResult.fatMax} gram (20-25%)</li>
                      <li>💧 <strong>Hidrasi Cairan:</strong> Minimal {calcResult.waterMl} ml air putih / hari</li>
                    </ul>
                  </div>
                </div>
              ) : (
                <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '340px', textAlign: 'center' }}>
                  <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚖️</div>
                  <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.25rem' }}>
                    Menunggu Data Antropometri
                  </div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', maxWidth: '320px' }}>
                    Isi formulir di sebelah kiri untuk melihat evaluasi BMT, TDEE, serta pembagian porsi makronutrien harian siswa.
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 2: KUISIONER SINGKAT */}
        {activeTab === 'questionnaire' && (
          <div className="card" style={{ maxWidth: '780px', margin: '0 auto' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.35rem', color: '#0f172a' }}>
              Kuisioner Cepat Kebiasaan Makan &amp; Kesehatan Siswa
            </h2>
            <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '1.5rem' }}>
              Kuisioner skrining 5 pertanyaan untuk memantau risiko lemas, anemia remaja, dan kualitas bekal makanan madrasah.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>
                  1. Apakah kamu sarapan pagi sebelum berangkat ke madrasah hari ini?
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q1" checked={qBreakfast === 'yes'} onChange={() => setQBreakfast('yes')} />
                    Ya, sudah sarapan bergizi
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q1" checked={qBreakfast === 'no'} onChange={() => setQBreakfast('no')} />
                    Tidak sempat / sering terlewat
                  </label>
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>
                  2. Berapa perkiraan konsumsi air putihmu dalam sehari?
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q2" checked={qWater === 'sufficient'} onChange={() => setQWater('sufficient')} />
                    Cukup (&ge; 7-8 gelas per hari)
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q2" checked={qWater === 'less'} onChange={() => setQWater('less')} />
                    Kurang (&lt; 5 gelas per hari)
                  </label>
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>
                  3. Apakah menu makanmu hari ini menyertakan sayuran atau buah-buahan?
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q3" checked={qVeggies === 'yes'} onChange={() => setQVeggies('yes')} />
                    Ya, selalu ada sayur / buah
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q3" checked={qVeggies === 'rare'} onChange={() => setQVeggies('rare')} />
                    Jarang / tidak suka sayur
                  </label>
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>
                  4. Seberapa sering kamu membeli gorengan atau minuman manis di kantin?
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q4" checked={qFriedSnacks === 'low'} onChange={() => setQFriedSnacks('low')} />
                    Jarang / terjaga dengan baik
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q4" checked={qFriedSnacks === 'frequent'} onChange={() => setQFriedSnacks('frequent')} />
                    Sering (hampir setiap jam istirahat)
                  </label>
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#0f172a', marginBottom: '0.5rem' }}>
                  5. Apakah akhir-akhir ini kamu sering merasa pusing, lemas, atau sulit konsentrasi saat belajar?
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q5" checked={qDizziness === 'no'} onChange={() => setQDizziness('no')} />
                    Tidak, merasa segar dan bersemangat
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                    <input type="radio" name="q5" checked={qDizziness === 'sometimes'} onChange={() => setQDizziness('sometimes')} />
                    Ya, sering merasa lemas / mengantuk (gejala 5L)
                  </label>
                </div>
              </div>

              <button
                onClick={() => setQSubmitted(true)}
                className="btn-primary"
                style={{ justifyContent: 'center', padding: '0.75rem' }}
              >
                Kirim &amp; Lihat Evaluasi Kuisioner
              </button>

              {qSubmitted && (
                <div className="result-box">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '1.25rem' }}>🩺</span>
                    <strong style={{ color: '#047857' }}>Ringkasan Hasil Skrining:</strong>
                  </div>
                  <ul style={{ fontSize: '0.85rem', color: '#166534', paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                    {qBreakfast === 'yes' ? (
                      <li>✅ Kebiasaan sarapan sangat baik untuk konsentrasi belajar madrasah.</li>
                    ) : (
                      <li>⚠️ Biasakan sarapan pagi dengan gizi seimbang untuk mencegah hipoglikemia saat belajar.</li>
                    )}

                    {qWater === 'sufficient' ? (
                      <li>✅ Tingkat hidrasi optimal, menjaga fokus dan daya ingat otak.</li>
                    ) : (
                      <li>⚠️ Tingkatkan konsumsi air putih minimal 8 gelas per hari untuk mencegah dehidrasi.</li>
                    )}

                    {qVeggies === 'rare' && (
                      <li>⚠️ Konsumsi serat dan mikronutrien kurang. Tambahkan sayuran hijau dan buah segar.</li>
                    )}

                    {qFriedSnacks === 'frequent' && (
                      <li>⚠️ Batasi gorengan bertepung dan minuman tinggi gula sederhana agar tidak memicu kantuk (food coma).</li>
                    )}

                    {qDizziness === 'sometimes' ? (
                      <li>🚨 Terdeteksi risiko anemia remaja (5L). Disarankan rutin konsumsi lauk hewani kaya zat besi dan Tablet Tambah Darah (TTD) mingguan bagi remaja putri.</li>
                    ) : (
                      <li>✅ Tingkat kesiapan fisik dan kebugaran belajar dalam kondisi baik!</li>
                    )}
                  </ul>
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 3: PANDUAN ISI PIRINGKU */}
        {activeTab === 'piringku' && (
          <div>
            <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#0f172a' }}>
                Konsep Gizi Seimbang: 'Isi Piringku' Kemenkes RI
              </h2>
              <p style={{ fontSize: '0.9rem', color: '#64748b', maxWidth: '600px', margin: '0.5rem auto 0' }}>
                Panduan porsi makan satu kali santap untuk memenuhi kebutuhan energi, protein, vitamin, dan mineral harian siswa madrasah.
              </p>
            </div>

            <div className="grid-cards" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))' }}>
              <div className="card" style={{ borderLeft: '4px solid #1e40af' }}>
                <div className="badge" style={{ background: '#eff6ff', color: '#1e40af', marginBottom: '0.75rem' }}>
                  2/3 dari 1/2 Piring (33%)
                </div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.35rem' }}>
                  🍚 Makanan Pokok
                </h3>
                <p style={{ fontSize: '0.85rem', color: '#64748b', lineHeight: 1.4 }}>
                  Sumber karbohidrat kompleks sebagai bahan bakar utama otak siswa saat belajar: Nasi putih/merah, jagung, kentang, singkong, atau ubi jalar.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid #991b1b' }}>
                <div className="badge" style={{ background: '#fef2f2', color: '#991b1b', marginBottom: '0.75rem' }}>
                  1/3 dari 1/2 Piring (17%)
                </div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.35rem' }}>
                  🍗 Lauk Pauk (Protein)
                </h3>
                <p style={{ fontSize: '0.85rem', color: '#64748b', lineHeight: 1.4 }}>
                  Pembangun jaringan tubuh dan pencegah stunting/anemia: Ikan, ayam, telur, daging, tempe, tahu, dan kacang-kacangan tinggi zat besi.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid #166534' }}>
                <div className="badge" style={{ background: '#f0fdf4', color: '#166534', marginBottom: '0.75rem' }}>
                  2/3 dari 1/2 Piring (33%)
                </div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.35rem' }}>
                  🥦 Sayuran Berwarna
                </h3>
                <p style={{ fontSize: '0.85rem', color: '#64748b', lineHeight: 1.4 }}>
                  Sumber serat pangan, antioksidan, dan mineral: Bayam, kangkung, buncis, wortel, brokoli, dan sawi untuk kesehatan pencernaan.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid #b45309' }}>
                <div className="badge" style={{ background: '#fffbeb', color: '#b45309', marginBottom: '0.75rem' }}>
                  1/3 dari 1/2 Piring (17%)
                </div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '0.35rem' }}>
                  🍌 Buah-buahan
                </h3>
                <p style={{ fontSize: '0.85rem', color: '#64748b', lineHeight: 1.4 }}>
                  Pemasok vitamin C alami, hidrasi, dan enzim pencernaan: Pisang, pepaya, jeruk, semangka, apel, atau melon.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: APLIKASI ANDROID NATIVE & FITUR AI */}
        {activeTab === 'apk' && (
          <div className="card" style={{ maxWidth: '820px', margin: '0 auto', textAlign: 'center', padding: '2.5rem 1.5rem' }}>
            <span style={{ fontSize: '3.5rem' }}>📱</span>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#0f172a', margin: '0.5rem 0' }}>
              NutriMind Madrasah Android Native App
            </h2>
            <p style={{ fontSize: '0.95rem', color: '#64748b', maxWidth: '580px', margin: '0 auto 1.5rem' }}>
              Aplikasi Android lengkap yang dibangun dengan Kotlin &amp; Jetpack Compose untuk pengalaman pemantauan gizi offline-first di smartphone siswa.
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', textAlign: 'left', marginBottom: '2rem' }}>
              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '1.25rem', marginBottom: '0.35rem' }}>📸</div>
                <strong style={{ fontSize: '0.9rem', color: '#0f172a' }}>AI Kamera Piringku</strong>
                <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.2rem' }}>
                  Memotret bekal makanan dan otomatis mengenali komposisi gizi, vitamin C, dan zat besi.
                </p>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '1.25rem', marginBottom: '0.35rem' }}>💾</div>
                <strong style={{ fontSize: '0.9rem', color: '#0f172a' }}>Database Lokal (Room)</strong>
                <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.2rem' }}>
                  Riwayat makanan dan catatan kesehatan harian tersimpan aman di HP tanpa kuota internet.
                </p>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '1.25rem', marginBottom: '0.35rem' }}>🔔</div>
                <strong style={{ fontSize: '0.9rem', color: '#0f172a' }}>Pengingat TTD &amp; Air</strong>
                <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.2rem' }}>
                  Alarm terjadwal untuk minum air putih dan Tablet Tambah Darah bagi siswi madrasah.
                </p>
              </div>
            </div>

            <a
              href="/NutriMind-Madrasah.apk"
              download="NutriMind-Madrasah.apk"
              className="btn-primary"
              style={{ padding: '0.85rem 2rem', fontSize: '1rem', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
            >
              <span>📥</span> Unduh Berkas APK Android (32 MB)
            </a>
          </div>
        )}
      </main>

      {/* Modal Download APK */}
      {showApkModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 100,
            padding: '1rem'
          }}
          onClick={() => setShowApkModal(false)}
        >
          <div
            style={{
              background: 'white',
              borderRadius: '16px',
              padding: '2rem',
              maxWidth: '480px',
              width: '100%',
              boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div style={{ textAlign: 'center', marginBottom: '1.25rem' }}>
              <span style={{ fontSize: '2.5rem' }}>📦</span>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800, color: '#0f172a', marginTop: '0.5rem' }}>
                Unduh NutriMind Madrasah (.APK)
              </h3>
              <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
                Paket instalasi aplikasi native Android untuk ponsel siswa &amp; guru madrasah (Ukuran: 32 MB).
              </p>
            </div>

            <div style={{ background: '#ecfdf5', border: '1px solid rgba(4, 120, 87, 0.2)', padding: '1rem', borderRadius: '10px', marginBottom: '1.25rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#047857', marginBottom: '0.35rem' }}>
                Panduan Pasang di Smartphone:
              </div>
              <ol style={{ fontSize: '0.8rem', color: '#065f46', paddingLeft: '1.2rem', display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <li>Klik tombol <strong>"Unduh Berkas APK Sekarang"</strong> di bawah.</li>
                <li>Setelah selesai diunduh, buka file <code>NutriMind-Madrasah.apk</code> di ponsel.</li>
                <li>Pilih <strong>"Izinkan Pasang dari Sumber Ini"</strong> jika muncul peringatan keamanan browser.</li>
                <li>Aplikasi siap digunakan untuk memantau gizi dan memotret piring makanan!</li>
              </ol>
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <a
                href="/NutriMind-Madrasah.apk"
                download="NutriMind-Madrasah.apk"
                className="btn-primary"
                style={{ flex: 1, justifyContent: 'center', textDecoration: 'none', textAlign: 'center' }}
              >
                <span>📥</span> Unduh Berkas APK Sekarang
              </a>
              <button
                onClick={() => setShowApkModal(false)}
                className="btn-outline"
                style={{ flex: 0.4, justifyContent: 'center' }}
              >
                Tutup
              </button>
            </div>

            <div style={{ marginTop: '0.85rem', textAlign: 'center', fontSize: '0.75rem', color: '#64748b' }}>
              Tautan alternatif: <a href="https://github.com/Luthh467/Luthfi1i" target="_blank" rel="noopener noreferrer" style={{ color: '#047857', fontWeight: 600, textDecoration: 'underline' }}>Repositori GitHub (Source &amp; Release)</a>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="footer container">
        <p style={{ fontWeight: 600, color: '#0f172a' }}>NutriMind AI Madrasah Sehat &copy; {new Date().getFullYear()}</p>
        <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
          Sistem Deteksi Dini &amp; Pemantauan Risiko Masalah Gizi Siswa Madrasah Berbasis Artificial Intelligence
        </p>
      </footer>
    </div>
  );
}
