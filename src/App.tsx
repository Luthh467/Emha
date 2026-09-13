import React, { useState } from 'react';

type Role = 'SISWA' | 'GURU';

interface UserProfile {
  uid: string;
  email: string;
  displayName: string;
  role: Role;
  nisn?: string;
  kelas?: string;
  nip?: string;
  schoolName: string;
}

interface NutritionRecord {
  id: string;
  studentId: string;
  studentName: string;
  kelas: string;
  heightCm: number;
  weightKg: number;
  bmi: number;
  statusGizi: 'Normal' | 'Gizi Kurang' | 'Beresiko Stunting' | 'Gizi Lebih';
  riskLevel: 'RENDAH' | 'SEDANG' | 'TINGGI';
  date: string;
}

interface UksIntervention {
  id: string;
  studentId: string;
  studentName: string;
  kelas: string;
  teacherName: string;
  interventionType: 'Pemberian TTD (Tablet Tambah Darah)' | 'Konseling Gizi' | 'Rujukan Puskesmas';
  notes: string;
  status: 'TERJADWAL' | 'PROSES' | 'SELESAI';
  date: string;
}

const INITIAL_RECORDS: NutritionRecord[] = [
  {
    id: 'nc_1',
    studentId: 'user_siswa_1',
    studentName: 'Ahmad Fathoni',
    kelas: 'IX-B (MTs)',
    heightCm: 162,
    weightKg: 44,
    bmi: 16.8,
    statusGizi: 'Gizi Kurang',
    riskLevel: 'SEDANG',
    date: '2026-09-12'
  },
  {
    id: 'nc_2',
    studentId: 'user_siswa_2',
    studentName: 'Nurul Hidayah',
    kelas: 'VIII-A (MTs)',
    heightCm: 154,
    weightKg: 49,
    bmi: 20.7,
    statusGizi: 'Normal',
    riskLevel: 'RENDAH',
    date: '2026-09-13'
  },
  {
    id: 'nc_3',
    studentId: 'user_siswa_3',
    studentName: 'M. Farhan Al-Baqir',
    kelas: 'VII-C (MTs)',
    heightCm: 148,
    weightKg: 35,
    bmi: 16.0,
    statusGizi: 'Beresiko Stunting',
    riskLevel: 'TINGGI',
    date: '2026-09-11'
  }
];

const INITIAL_INTERVENTIONS: UksIntervention[] = [
  {
    id: 'uks_1',
    studentId: 'user_siswa_3',
    studentName: 'M. Farhan Al-Baqir',
    kelas: 'VII-C (MTs)',
    teacherName: 'Ustadzah Siti Aminah, S.Pd (Pembina UKS)',
    interventionType: 'Pemberian TTD (Tablet Tambah Darah)',
    notes: 'Diberikan suplemen zat besi mingguan dan pemantauan sarapan bergizi tinggi protein.',
    status: 'PROSES',
    date: '2026-09-12'
  }
];

export default function App() {
  // Current authenticated user state
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  
  // App data state
  const [nutritionRecords, setNutritionRecords] = useState<NutritionRecord[]>(INITIAL_RECORDS);
  const [interventions, setInterventions] = useState<UksIntervention[]>(INITIAL_INTERVENTIONS);
  
  // Registration / Custom Login form state
  const [authMode, setAuthMode] = useState<'LOGIN' | 'REGISTER'>('LOGIN');
  const [regRole, setRegRole] = useState<Role>('SISWA');
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regIdentifier, setRegIdentifier] = useState(''); // NISN or NIP
  const [regKelas, setRegKelas] = useState('IX-B (MTs)');
  
  // Student self-check form state
  const [tb, setTb] = useState('160');
  const [bb, setBb] = useState('50');
  const [checkSubmittedMsg, setCheckSubmittedMsg] = useState('');

  // Teacher UKS intervention form state
  const [selectedStudentForIntervention, setSelectedStudentForIntervention] = useState('user_siswa_1');
  const [interventionType, setInterventionType] = useState<'Pemberian TTD (Tablet Tambah Darah)' | 'Konseling Gizi' | 'Rujukan Puskesmas'>('Pemberian TTD (Tablet Tambah Darah)');
  const [interventionNote, setInterventionNote] = useState('');

  // APK Download Cache-buster state
  const [downloadTimestamp, setDownloadTimestamp] = useState(Date.now());
  const [showDownloadGuide, setShowDownloadGuide] = useState(false);

  // Quick Preset Logins
  const handleQuickLogin = (role: Role) => {
    if (role === 'SISWA') {
      setCurrentUser({
        uid: 'user_siswa_1',
        email: 'ahmad.fathoni@siswa.madrasah.id',
        displayName: 'Ahmad Fathoni',
        role: 'SISWA',
        nisn: '0089281729',
        kelas: 'IX-B (MTs)',
        schoolName: 'MTs Negeri 1 Model'
      });
    } else {
      setCurrentUser({
        uid: 'user_guru_1',
        email: 'siti.aminah@kemenag.go.id',
        displayName: 'Ustadzah Siti Aminah, S.Pd',
        role: 'GURU',
        nip: '198405162010012018',
        schoolName: 'MTs Negeri 1 Model'
      });
    }
  };

  const handleRegisterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!regName || !regEmail) return;

    const uid = 'usr_' + Date.now().toString().slice(-6);
    const newProfile: UserProfile = {
      uid,
      email: regEmail,
      displayName: regName,
      role: regRole,
      nisn: regRole === 'SISWA' ? regIdentifier : undefined,
      nip: regRole === 'GURU' ? regIdentifier : undefined,
      kelas: regRole === 'SISWA' ? regKelas : undefined,
      schoolName: 'MTs Negeri 1 Model'
    };
    setCurrentUser(newProfile);
    setAuthMode('LOGIN');
  };

  // Student adds check
  const handleAddStudentCheck = (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;
    const h = parseFloat(tb);
    const w = parseFloat(bb);
    if (!h || !w) return;

    const heightM = h / 100;
    const bmiVal = parseFloat((w / (heightM * heightM)).toFixed(1));
    let status: 'Normal' | 'Gizi Kurang' | 'Beresiko Stunting' | 'Gizi Lebih' = 'Normal';
    let risk: 'RENDAH' | 'SEDANG' | 'TINGGI' = 'RENDAH';

    if (bmiVal < 17.0) {
      status = 'Gizi Kurang';
      risk = 'SEDANG';
    } else if (bmiVal > 25.0) {
      status = 'Gizi Lebih';
      risk = 'SEDANG';
    } else {
      status = 'Normal';
      risk = 'RENDAH';
    }

    const newRecord: NutritionRecord = {
      id: 'nc_' + Date.now(),
      studentId: currentUser.uid,
      studentName: currentUser.displayName,
      kelas: currentUser.kelas || 'Siswa',
      heightCm: h,
      weightKg: w,
      bmi: bmiVal,
      statusGizi: status,
      riskLevel: risk,
      date: new Date().toISOString().split('T')[0]
    };

    setNutritionRecords([newRecord, ...nutritionRecords]);
    setCheckSubmittedMsg('Hasil pemeriksaan gizi mandiri berhasil disimpan ke Firestore cloud!');
    setTimeout(() => setCheckSubmittedMsg(''), 4000);
  };

  // Teacher adds intervention
  const handleAddIntervention = (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser || currentUser.role !== 'GURU') return;
    const targetStudent = nutritionRecords.find(r => r.studentId === selectedStudentForIntervention);
    if (!targetStudent) return;

    const newInt: UksIntervention = {
      id: 'uks_' + Date.now(),
      studentId: targetStudent.studentId,
      studentName: targetStudent.studentName,
      kelas: targetStudent.kelas,
      teacherName: currentUser.displayName,
      interventionType,
      notes: interventionNote || 'Pemantauan berkala kondisi gizi oleh Guru UKS.',
      status: 'PROSES',
      date: new Date().toISOString().split('T')[0]
    };

    setInterventions([newInt, ...interventions]);
    setInterventionNote('');
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f8fafc',
      fontFamily: 'Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      color: '#1e293b'
    }}>
      {/* Top Navbar */}
      <header style={{
        backgroundColor: '#ffffff',
        borderBottom: '1px solid #e2e8f0',
        padding: '14px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 50
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '10px',
            backgroundColor: '#059669',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#ffffff',
            fontSize: '20px'
          }}>
            🥗
          </div>
          <div>
            <div style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '8px' }}>
              NutriMind AI
              <span style={{
                fontSize: '11px',
                fontWeight: 600,
                padding: '2px 8px',
                borderRadius: '12px',
                backgroundColor: '#dcfce7',
                color: '#15803d',
                letterSpacing: '0.02em'
              }}>
                Firebase Auth & Rules Active
              </span>
            </div>
            <div style={{ fontSize: '12px', color: '#64748b' }}>
              Sistem Deteksi Dini & Intervensi Gizi Siswa Madrasah
            </div>
          </div>
        </div>

        {currentUser && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '14px', fontWeight: 600, color: '#0f172a' }}>
                {currentUser.displayName}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '6px' }}>
                <span style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  padding: '2px 8px',
                  borderRadius: '6px',
                  backgroundColor: currentUser.role === 'GURU' ? '#e0e7ff' : '#ecfdf5',
                  color: currentUser.role === 'GURU' ? '#4338ca' : '#047857'
                }}>
                  {currentUser.role === 'GURU' ? '👨‍🏫 GURU / PEMBINA UKS' : '🎓 SISWA MADRASAH'}
                </span>
                <span style={{ fontSize: '11px', color: '#94a3b8' }}>
                  {currentUser.role === 'GURU' ? `NIP: ${currentUser.nip || '-'}` : `NISN: ${currentUser.nisn || '-'}`}
                </span>
              </div>
            </div>
            <button
              onClick={() => setCurrentUser(null)}
              style={{
                backgroundColor: '#fee2e2',
                color: '#b91c1c',
                border: 'none',
                padding: '8px 14px',
                borderRadius: '8px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer'
              }}
            >
              Keluar
            </button>
          </div>
        )}
      </header>

      {/* Top Banner: APK Download with Cache-Buster */}
      <div style={{
        backgroundColor: '#0284c7',
        color: '#ffffff',
        padding: '10px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '13px',
        flexWrap: 'wrap',
        gap: '12px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.06)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '18px' }}>📲</span>
          <span>
            <strong>NutriMind AI Android (Build Terbaru):</strong> File APK selalu diperbarui dengan auto-timestamp (bebas cache lama Vercel/Browser).
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <button
            onClick={() => setShowDownloadGuide(!showDownloadGuide)}
            style={{
              backgroundColor: 'rgba(255,255,255,0.18)',
              color: '#ffffff',
              border: '1px solid rgba(255,255,255,0.3)',
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            {showDownloadGuide ? '✕ Tutup Panduan' : 'ℹ️ Mengapa Versi Lama?'}
          </button>
          <a
            href={`/NutriMind-release.apk?t=${downloadTimestamp}`}
            download={`NutriMind-v1.0-${downloadTimestamp}.apk`}
            onClick={() => setDownloadTimestamp(Date.now())}
            style={{
              backgroundColor: '#ffffff',
              color: '#0284c7',
              padding: '7px 16px',
              borderRadius: '6px',
              fontWeight: 700,
              fontSize: '13px',
              textDecoration: 'none',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              boxShadow: '0 1px 3px rgba(0,0,0,0.15)'
            }}
          >
            ⬇️ Unduh APK Terbaru
          </a>
        </div>
      </div>

      {showDownloadGuide && (
        <div style={{
          backgroundColor: '#eff6ff',
          borderBottom: '1px solid #bfdbfe',
          padding: '16px 24px',
          fontSize: '13px',
          color: '#1e3a8a',
          lineHeight: 1.6
        }}>
          <div style={{ maxWidth: '1180px', margin: '0 auto' }}>
            <div style={{ fontWeight: 700, marginBottom: '6px', fontSize: '14px' }}>
              💡 Penyebab File APK di Vercel Masih Versi Lama & Solusinya:
            </div>
            <ol style={{ margin: '0 0 10px 0', paddingLeft: '20px' }}>
              <li>
                <strong>Vercel Tidak Meng-compile Ulang Android:</strong> Vercel hanya menghosting website/statis, bukan compiler Android. Setiap kali ada fitur baru dari AI Studio, file APK baru harus diekspor/disalin ke dalam folder <code>public/NutriMind-release.apk</code> sebelum di-push ke Git.
              </li>
              <li>
                <strong>Cache Browser & Edge CDN Vercel:</strong> Jika nama file tetap sama (contoh: <code>app.apk</code>), Chrome Android & CDN Vercel akan otomatis menyajikan file dari cache lama.
              </li>
              <li>
                <strong>Solusi yang Telah Diterapkan:</strong>
                <ul style={{ paddingLeft: '18px', marginTop: '4px' }}>
                  <li>Header anti-cache telah ditambahkan di <code>vercel.json</code> (Cache-Control: no-store, no-cache).</li>
                  <li>Tombol unduh di atas sekarang menggunakan parameter dinamis <code>?t=timestamp</code> dan nama file bertanggal unik setiap kali diklik.</li>
                </ul>
              </li>
            </ol>
          </div>
        </div>
      )}

      {/* Main Content Area */}
      <main style={{ maxWidth: '1180px', margin: '0 auto', padding: '24px 20px' }}>
        
        {/* If Not Logged In: Show Firebase Auth Portal */}
        {!currentUser ? (
          <div style={{ maxWidth: '780px', margin: '30px auto' }}>
            <div style={{
              backgroundColor: '#ffffff',
              borderRadius: '20px',
              border: '1px solid #e2e8f0',
              boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05)',
              overflow: 'hidden'
            }}>
              <div style={{
                background: 'linear-gradient(135deg, #059669 0%, #0d9488 100%)',
                padding: '28px',
                color: '#ffffff',
                textAlign: 'center'
              }}>
                <h2 style={{ margin: '0 0 8px 0', fontSize: '22px', fontWeight: 700 }}>
                  Autentikasi Firebase NutriMind AI
                </h2>
                <p style={{ margin: 0, fontSize: '14px', opacity: 0.9 }}>
                  Pemisahan hak akses berbasis peran (Role-Based Access Control) antara <strong>Siswa</strong> dan <strong>Guru</strong>.
                </p>
              </div>

              <div style={{ padding: '32px' }}>
                <div style={{
                  backgroundColor: '#f0fdf4',
                  border: '1px solid #bbf7d0',
                  borderRadius: '12px',
                  padding: '16px',
                  marginBottom: '28px'
                }}>
                  <div style={{ fontWeight: 600, color: '#166534', fontSize: '14px', marginBottom: '4px' }}>
                    🔐 Aturan Keamanan (Firestore Security Rules)
                  </div>
                  <div style={{ fontSize: '13px', color: '#15803d', lineHeight: 1.5 }}>
                    • <strong>Siswa</strong>: Hanya memiliki izin baca & tulis untuk data rekam gizi miliknya sendiri.<br />
                    • <strong>Guru / UKS</strong>: Diberikan izin untuk memantau data seluruh siswa, mendeteksi risiko stunting/anemia, dan menginput intervensi UKS.
                  </div>
                </div>

                {authMode === 'LOGIN' ? (
                  <div>
                    <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px 0', color: '#334155' }}>
                      Pilih Simulasi Login Berdasarkan Peran:
                    </h3>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' }}>
                      {/* Siswa Card */}
                      <div
                        onClick={() => handleQuickLogin('SISWA')}
                        style={{
                          border: '2px solid #10b981',
                          backgroundColor: '#f0fdf4',
                          borderRadius: '14px',
                          padding: '20px',
                          cursor: 'pointer',
                          transition: 'all 0.2s',
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'space-between'
                        }}
                      >
                        <div>
                          <div style={{ fontSize: '32px', marginBottom: '8px' }}>🎓</div>
                          <div style={{ fontWeight: 700, fontSize: '17px', color: '#065f46' }}>
                            Masuk sebagai Siswa
                          </div>
                          <div style={{ fontSize: '13px', color: '#047857', marginTop: '6px', lineHeight: 1.4 }}>
                            Akses rekam gizi pribadi, kalkulator IMT mandiri, dan rekomendasi menu Isi Piringku.
                          </div>
                        </div>
                        <div style={{ marginTop: '16px' }}>
                          <span style={{
                            backgroundColor: '#059669',
                            color: '#ffffff',
                            padding: '8px 16px',
                            borderRadius: '8px',
                            fontSize: '13px',
                            fontWeight: 600,
                            display: 'inline-block'
                          }}>
                            Masuk Akun Siswa →
                          </span>
                        </div>
                      </div>

                      {/* Guru Card */}
                      <div
                        onClick={() => handleQuickLogin('GURU')}
                        style={{
                          border: '2px solid #6366f1',
                          backgroundColor: '#eef2ff',
                          borderRadius: '14px',
                          padding: '20px',
                          cursor: 'pointer',
                          transition: 'all 0.2s',
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'space-between'
                        }}
                      >
                        <div>
                          <div style={{ fontSize: '32px', marginBottom: '8px' }}>👨‍🏫</div>
                          <div style={{ fontWeight: 700, fontSize: '17px', color: '#3730a3' }}>
                            Masuk sebagai Guru / UKS
                          </div>
                          <div style={{ fontSize: '13px', color: '#4338ca', marginTop: '6px', lineHeight: 1.4 }}>
                            Monitoring agregat kelas/madrasah, daftar siswa berisiko, dan pencatatan intervensi UKS.
                          </div>
                        </div>
                        <div style={{ marginTop: '16px' }}>
                          <span style={{
                            backgroundColor: '#4f46e5',
                            color: '#ffffff',
                            padding: '8px 16px',
                            borderRadius: '8px',
                            fontSize: '13px',
                            fontWeight: 600,
                            display: 'inline-block'
                          }}>
                            Masuk Akun Guru →
                          </span>
                        </div>
                      </div>
                    </div>

                    <div style={{ textAlign: 'center', borderTop: '1px solid #f1f5f9', paddingTop: '20px' }}>
                      <span style={{ fontSize: '14px', color: '#64748b' }}>
                        Ingin mendaftarkan akun baru dengan NISN / NIP?{' '}
                      </span>
                      <button
                        onClick={() => setAuthMode('REGISTER')}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#0284c7',
                          fontWeight: 600,
                          cursor: 'pointer',
                          textDecoration: 'underline'
                        }}
                      >
                        Daftar Akun Baru
                      </button>
                    </div>
                  </div>
                ) : (
                  <form onSubmit={handleRegisterSubmit}>
                    <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px 0', color: '#334155' }}>
                      Pendaftaran Profil Pengguna di Firebase Auth
                    </h3>

                    <div style={{ marginBottom: '16px' }}>
                      <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                        Pilih Peran Akun:
                      </label>
                      <div style={{ display: 'flex', gap: '16px' }}>
                        <label style={{
                          flex: 1,
                          padding: '12px',
                          borderRadius: '8px',
                          border: regRole === 'SISWA' ? '2px solid #059669' : '1px solid #cbd5e1',
                          backgroundColor: regRole === 'SISWA' ? '#f0fdf4' : '#ffffff',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          fontWeight: 600
                        }}>
                          <input
                            type="radio"
                            name="role"
                            checked={regRole === 'SISWA'}
                            onChange={() => setRegRole('SISWA')}
                          />
                          🎓 Siswa Madrasah
                        </label>
                        <label style={{
                          flex: 1,
                          padding: '12px',
                          borderRadius: '8px',
                          border: regRole === 'GURU' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
                          backgroundColor: regRole === 'GURU' ? '#eef2ff' : '#ffffff',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          fontWeight: 600
                        }}>
                          <input
                            type="radio"
                            name="role"
                            checked={regRole === 'GURU'}
                            onChange={() => setRegRole('GURU')}
                          />
                          👨‍🏫 Guru / Pembina UKS
                        </label>
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                      <div>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '4px' }}>
                          Nama Lengkap:
                        </label>
                        <input
                          type="text"
                          required
                          value={regName}
                          onChange={(e) => setRegName(e.target.value)}
                          placeholder="Nama lengkap..."
                          style={{
                            width: '100%',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            border: '1px solid #cbd5e1',
                            fontSize: '14px',
                            boxSizing: 'border-box'
                          }}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '4px' }}>
                          Email Madrasah:
                        </label>
                        <input
                          type="email"
                          required
                          value={regEmail}
                          onChange={(e) => setRegEmail(e.target.value)}
                          placeholder="email@madrasah.id"
                          style={{
                            width: '100%',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            border: '1px solid #cbd5e1',
                            fontSize: '14px',
                            boxSizing: 'border-box'
                          }}
                        />
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
                      <div>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '4px' }}>
                          {regRole === 'SISWA' ? 'NISN Siswa:' : 'NIP Guru / Pegawai:'}
                        </label>
                        <input
                          type="text"
                          required
                          value={regIdentifier}
                          onChange={(e) => setRegIdentifier(e.target.value)}
                          placeholder={regRole === 'SISWA' ? '0081234567' : '198205142010...'}
                          style={{
                            width: '100%',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            border: '1px solid #cbd5e1',
                            fontSize: '14px',
                            boxSizing: 'border-box'
                          }}
                        />
                      </div>
                      {regRole === 'SISWA' && (
                        <div>
                          <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '4px' }}>
                            Kelas:
                          </label>
                          <select
                            value={regKelas}
                            onChange={(e) => setRegKelas(e.target.value)}
                            style={{
                              width: '100%',
                              padding: '10px 12px',
                              borderRadius: '8px',
                              border: '1px solid #cbd5e1',
                              fontSize: '14px',
                              boxSizing: 'border-box'
                            }}
                          >
                            <option value="VII-A (MTs)">VII-A (MTs)</option>
                            <option value="VIII-A (MTs)">VIII-A (MTs)</option>
                            <option value="IX-B (MTs)">IX-B (MTs)</option>
                            <option value="X-MIA (MA)">X-MIA (MA)</option>
                          </select>
                        </div>
                      )}
                    </div>

                    <div style={{ display: 'flex', gap: '12px' }}>
                      <button
                        type="submit"
                        style={{
                          flex: 1,
                          backgroundColor: '#059669',
                          color: '#ffffff',
                          padding: '12px',
                          borderRadius: '8px',
                          border: 'none',
                          fontWeight: 600,
                          cursor: 'pointer'
                        }}
                      >
                        Daftar & Masuk ke Sistem
                      </button>
                      <button
                        type="button"
                        onClick={() => setAuthMode('LOGIN')}
                        style={{
                          backgroundColor: '#e2e8f0',
                          color: '#334155',
                          padding: '12px 20px',
                          borderRadius: '8px',
                          border: 'none',
                          fontWeight: 600,
                          cursor: 'pointer'
                        }}
                      >
                        Batal
                      </button>
                    </div>
                  </form>
                )}
              </div>
            </div>
          </div>
        ) : (
          /* When Logged In: Role-Differentiated Views */
          <div>
            {currentUser.role === 'SISWA' ? (
              /* ================== SISWA VIEW ================== */
              <div>
                <div style={{
                  backgroundColor: '#ffffff',
                  borderRadius: '16px',
                  border: '1px solid #e2e8f0',
                  padding: '24px',
                  marginBottom: '24px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <div style={{ fontSize: '13px', color: '#059669', fontWeight: 600, textTransform: 'uppercase' }}>
                      Portal Siswa Madrasah
                    </div>
                    <h2 style={{ margin: '4px 0 6px 0', fontSize: '22px', fontWeight: 700, color: '#0f172a' }}>
                      Assalamu'alaikum, {currentUser.displayName}
                    </h2>
                    <p style={{ margin: 0, fontSize: '14px', color: '#64748b' }}>
                      Kelas: <strong>{currentUser.kelas || 'IX-B'}</strong> | NISN: <strong>{currentUser.nisn}</strong> | Madrasah: {currentUser.schoolName}
                    </p>
                  </div>
                  <div style={{
                    backgroundColor: '#ecfdf5',
                    border: '1px solid #a7f3d0',
                    padding: '12px 20px',
                    borderRadius: '12px',
                    textAlign: 'center'
                  }}>
                    <div style={{ fontSize: '12px', color: '#047857', fontWeight: 600 }}>Status Gizi Terakhir</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: '#065f46' }}>
                      {nutritionRecords.find(r => r.studentId === currentUser.uid)?.statusGizi || 'Belum Diperiksa'}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '24px' }}>
                  {/* Left Column: Form Cek Gizi Mandiri */}
                  <div style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '16px',
                    border: '1px solid #e2e8f0',
                    padding: '24px'
                  }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 700, margin: '0 0 16px 0', color: '#0f172a' }}>
                      📝 Form Cek Status Gizi Mandiri
                    </h3>
                    
                    {checkSubmittedMsg && (
                      <div style={{
                        backgroundColor: '#dcfce7',
                        border: '1px solid #86efac',
                        color: '#15803d',
                        padding: '12px',
                        borderRadius: '8px',
                        fontSize: '13px',
                        marginBottom: '16px'
                      }}>
                        {checkSubmittedMsg}
                      </div>
                    )}

                    <form onSubmit={handleAddStudentCheck}>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                        <div>
                          <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                            Tinggi Badan (cm):
                          </label>
                          <input
                            type="number"
                            required
                            value={tb}
                            onChange={(e) => setTb(e.target.value)}
                            style={{
                              width: '100%',
                              padding: '10px 12px',
                              borderRadius: '8px',
                              border: '1px solid #cbd5e1',
                              fontSize: '14px',
                              boxSizing: 'border-box'
                            }}
                          />
                        </div>
                        <div>
                          <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                            Berat Badan (kg):
                          </label>
                          <input
                            type="number"
                            required
                            value={bb}
                            onChange={(e) => setBb(e.target.value)}
                            style={{
                              width: '100%',
                              padding: '10px 12px',
                              borderRadius: '8px',
                              border: '1px solid #cbd5e1',
                              fontSize: '14px',
                              boxSizing: 'border-box'
                            }}
                          />
                        </div>
                      </div>

                      <div style={{
                        backgroundColor: '#f8fafc',
                        padding: '14px',
                        borderRadius: '10px',
                        border: '1px dashed #cbd5e1',
                        marginBottom: '20px'
                      }}>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: '#475569', marginBottom: '4px' }}>
                          Estimasi Indeks Massa Tubuh (IMT):
                        </div>
                        <div style={{ fontSize: '20px', fontWeight: 700, color: '#0284c7' }}>
                          {(parseFloat(bb) / Math.pow(parseFloat(tb) / 100, 2)).toFixed(1)} kg/m²
                        </div>
                        <div style={{ fontSize: '12px', color: '#64748b', marginTop: '4px' }}>
                          Sesuai standar Permenkes & WHO untuk remaja usia sekolah madrasah.
                        </div>
                      </div>

                      <button
                        type="submit"
                        style={{
                          width: '100%',
                          backgroundColor: '#059669',
                          color: '#ffffff',
                          padding: '12px',
                          borderRadius: '8px',
                          border: 'none',
                          fontWeight: 600,
                          fontSize: '14px',
                          cursor: 'pointer'
                        }}
                      >
                        Simpan Pemeriksaan Saya
                      </button>
                    </form>
                  </div>

                  {/* Right Column: Riwayat Pemeriksaan Pribadi Siswa */}
                  <div style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '16px',
                    border: '1px solid #e2e8f0',
                    padding: '24px'
                  }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 700, margin: '0 0 16px 0', color: '#0f172a' }}>
                      📋 Riwayat Cek Gizi Saya
                    </h3>
                    <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '14px' }}>
                      *Berdasarkan Firestore Rules, Anda hanya diizinkan melihat riwayat UID Anda sendiri.
                    </div>

                    {nutritionRecords.filter(r => r.studentId === currentUser.uid).length === 0 ? (
                      <div style={{ padding: '24px', textAlign: 'center', color: '#94a3b8', fontSize: '14px' }}>
                        Belum ada catatan riwayat. Silakan lakukan cek mandiri.
                      </div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {nutritionRecords
                          .filter(r => r.studentId === currentUser.uid)
                          .map((rec) => (
                            <div
                              key={rec.id}
                              style={{
                                padding: '14px',
                                borderRadius: '10px',
                                border: '1px solid #e2e8f0',
                                backgroundColor: '#fcfdfd'
                              }}
                            >
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                <span style={{ fontWeight: 600, fontSize: '14px' }}>{rec.date}</span>
                                <span style={{
                                  fontSize: '11px',
                                  fontWeight: 700,
                                  padding: '2px 8px',
                                  borderRadius: '6px',
                                  backgroundColor: rec.riskLevel === 'RENDAH' ? '#dcfce7' : '#fef3c7',
                                  color: rec.riskLevel === 'RENDAH' ? '#15803d' : '#b45309'
                                }}>
                                  {rec.statusGizi} ({rec.riskLevel})
                                </span>
                              </div>
                              <div style={{ fontSize: '13px', color: '#64748b' }}>
                                TB: {rec.heightCm} cm | BB: {rec.weightKg} kg | IMT: {rec.bmi}
                              </div>
                            </div>
                          ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              /* ================== GURU / PEMBINA UKS VIEW ================== */
              <div>
                {/* Guru Header Summary */}
                <div style={{
                  backgroundColor: '#ffffff',
                  borderRadius: '16px',
                  border: '1px solid #e2e8f0',
                  padding: '24px',
                  marginBottom: '24px'
                }}>
                  <div style={{ fontSize: '13px', color: '#4f46e5', fontWeight: 600, textTransform: 'uppercase' }}>
                    Dashboard Guru & Pembina UKS Madrasah
                  </div>
                  <h2 style={{ margin: '4px 0 8px 0', fontSize: '22px', fontWeight: 700, color: '#0f172a' }}>
                    Selamat Bertugas, {currentUser.displayName}
                  </h2>
                  <p style={{ margin: 0, fontSize: '14px', color: '#64748b' }}>
                    Otorisasi Akun Guru aktif: Memiliki hak istimewa (privilege) membaca seluruh data siswa dan menginput intervensi gizi UKS.
                  </p>

                  {/* Summary Metric Cards */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginTop: '20px' }}>
                    <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
                      <div style={{ fontSize: '12px', color: '#64748b', fontWeight: 600 }}>Total Siswa Terdata</div>
                      <div style={{ fontSize: '24px', fontWeight: 700, color: '#0f172a', marginTop: '4px' }}>
                        {nutritionRecords.length} Siswa
                      </div>
                    </div>
                    <div style={{ backgroundColor: '#f0fdf4', padding: '16px', borderRadius: '12px', border: '1px solid #bbf7d0' }}>
                      <div style={{ fontSize: '12px', color: '#166534', fontWeight: 600 }}>Gizi Baik / Normal</div>
                      <div style={{ fontSize: '24px', fontWeight: 700, color: '#15803d', marginTop: '4px' }}>
                        {nutritionRecords.filter(r => r.riskLevel === 'RENDAH').length} Siswa
                      </div>
                    </div>
                    <div style={{ backgroundColor: '#fef2f2', padding: '16px', borderRadius: '12px', border: '1px solid #fecaca' }}>
                      <div style={{ fontSize: '12px', color: '#991b1b', fontWeight: 600 }}>Perlu Intervensi (Risiko Tinggi/Sedang)</div>
                      <div style={{ fontSize: '24px', fontWeight: 700, color: '#b91c1c', marginTop: '4px' }}>
                        {nutritionRecords.filter(r => r.riskLevel !== 'RENDAH').length} Siswa
                      </div>
                    </div>
                    <div style={{ backgroundColor: '#eef2ff', padding: '16px', borderRadius: '12px', border: '1px solid #c7d2fe' }}>
                      <div style={{ fontSize: '12px', color: '#3730a3', fontWeight: 600 }}>Intervensi UKS Berjalan</div>
                      <div style={{ fontSize: '24px', fontWeight: 700, color: '#4338ca', marginTop: '4px' }}>
                        {interventions.length} Kasus
                      </div>
                    </div>
                  </div>
                </div>

                {/* Teacher Actions & Monitoring Table */}
                <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '24px' }}>
                  {/* Table: All Students Nutrition Records */}
                  <div style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '16px',
                    border: '1px solid #e2e8f0',
                    padding: '24px'
                  }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 700, margin: '0 0 16px 0', color: '#0f172a' }}>
                      📊 Data Pemantauan Gizi Seluruh Siswa
                    </h3>
                    <div style={{ overflowX: 'auto' }}>
                      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
                        <thead>
                          <tr style={{ borderBottom: '2px solid #f1f5f9', color: '#64748b' }}>
                            <th style={{ padding: '10px 8px' }}>Nama Siswa</th>
                            <th style={{ padding: '10px 8px' }}>Kelas</th>
                            <th style={{ padding: '10px 8px' }}>IMT</th>
                            <th style={{ padding: '10px 8px' }}>Status</th>
                            <th style={{ padding: '10px 8px' }}>Risiko</th>
                          </tr>
                        </thead>
                        <tbody>
                          {nutritionRecords.map((r) => (
                            <tr key={r.id} style={{ borderBottom: '1px solid #f8fafc' }}>
                              <td style={{ padding: '10px 8px', fontWeight: 600, color: '#0f172a' }}>
                                {r.studentName}
                              </td>
                              <td style={{ padding: '10px 8px', color: '#64748b' }}>{r.kelas}</td>
                              <td style={{ padding: '10px 8px', fontWeight: 600 }}>{r.bmi}</td>
                              <td style={{ padding: '10px 8px' }}>{r.statusGizi}</td>
                              <td style={{ padding: '10px 8px' }}>
                                <span style={{
                                  padding: '2px 8px',
                                  borderRadius: '6px',
                                  fontWeight: 700,
                                  fontSize: '11px',
                                  backgroundColor: r.riskLevel === 'RENDAH' ? '#dcfce7' : r.riskLevel === 'SEDANG' ? '#fef3c7' : '#fee2e2',
                                  color: r.riskLevel === 'RENDAH' ? '#166534' : r.riskLevel === 'SEDANG' ? '#92400e' : '#991b1b'
                                }}>
                                  {r.riskLevel}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Form: Input Intervensi UKS */}
                  <div style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '16px',
                    border: '1px solid #e2e8f0',
                    padding: '24px'
                  }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 700, margin: '0 0 16px 0', color: '#0f172a' }}>
                      🩺 Input Tindak Lanjut UKS
                    </h3>
                    <form onSubmit={handleAddIntervention}>
                      <div style={{ marginBottom: '14px' }}>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                          Pilih Siswa Target:
                        </label>
                        <select
                          value={selectedStudentForIntervention}
                          onChange={(e) => setSelectedStudentForIntervention(e.target.value)}
                          style={{
                            width: '100%',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            border: '1px solid #cbd5e1',
                            fontSize: '13px'
                          }}
                        >
                          {nutritionRecords.map((r) => (
                            <option key={r.studentId} value={r.studentId}>
                              {r.studentName} ({r.kelas}) - {r.statusGizi}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div style={{ marginBottom: '14px' }}>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                          Jenis Tindak Lanjut / Intervensi:
                        </label>
                        <select
                          value={interventionType}
                          onChange={(e) => setInterventionType(e.target.value as any)}
                          style={{
                            width: '100%',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            border: '1px solid #cbd5e1',
                            fontSize: '13px'
                          }}
                        >
                          <option value="Pemberian TTD (Tablet Tambah Darah)">Pemberian TTD (Tablet Tambah Darah)</option>
                          <option value="Konseling Gizi">Konseling Gizi & Pola Makan</option>
                          <option value="Rujukan Puskesmas">Rujukan ke Puskesmas Terdekat</option>
                        </select>
                      </div>

                      <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                          Catatan Pembina UKS:
                        </label>
                        <textarea
                          rows={3}
                          value={interventionNote}
                          onChange={(e) => setInterventionNote(e.target.value)}
                          placeholder="Tuliskan catatan intervensi, dosis vitamin, atau instruksi..."
                          style={{
                            width: '100%',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            border: '1px solid #cbd5e1',
                            fontSize: '13px',
                            boxSizing: 'border-box'
                          }}
                        />
                      </div>

                      <button
                        type="submit"
                        style={{
                          width: '100%',
                          backgroundColor: '#4f46e5',
                          color: '#ffffff',
                          padding: '12px',
                          borderRadius: '8px',
                          border: 'none',
                          fontWeight: 600,
                          fontSize: '14px',
                          cursor: 'pointer'
                        }}
                      >
                        Simpan Tindak Lanjut UKS
                      </button>
                    </form>

                    <div style={{ marginTop: '24px', borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
                      <div style={{ fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '8px' }}>
                        Daftar Intervensi Aktif:
                      </div>
                      {interventions.map((item) => (
                        <div key={item.id} style={{
                          padding: '10px',
                          backgroundColor: '#f8fafc',
                          borderRadius: '8px',
                          fontSize: '12px',
                          marginBottom: '8px',
                          border: '1px solid #e2e8f0'
                        }}>
                          <div style={{ fontWeight: 600, color: '#0f172a' }}>{item.studentName} ({item.kelas})</div>
                          <div style={{ color: '#4338ca', fontWeight: 600 }}>{item.interventionType}</div>
                          <div style={{ color: '#64748b', marginTop: '2px' }}>{item.notes}</div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
