import React, { useState, useEffect } from 'react';

export default function App() {
  const [timestamp, setTimestamp] = useState('');

  useEffect(() => {
    const now = new Date();
    const formatted = now.getFullYear().toString() +
      String(now.getMonth() + 1).padStart(2, '0') +
      String(now.getDate()).padStart(2, '0') + '_' +
      String(now.getHours()).padStart(2, '0') +
      String(now.getMinutes()).padStart(2, '0');
    setTimestamp(formatted);
  }, []);

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f8fafc',
      color: '#1e293b',
      fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px'
    }}>
      <div style={{
        maxWidth: '560px',
        width: '100%',
        backgroundColor: '#ffffff',
        borderRadius: '16px',
        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)',
        padding: '36px 32px',
        textAlign: 'center',
        border: '1px solid #e2e8f0'
      }}>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: '64px',
          height: '64px',
          backgroundColor: '#e0f2fe',
          color: '#0284c7',
          borderRadius: '16px',
          fontSize: '28px',
          marginBottom: '20px'
        }}>
          🥗
        </div>
        
        <h1 style={{ fontSize: '24px', fontWeight: 700, margin: '0 0 8px 0', color: '#0f172a' }}>
          NutriMind AI Madrasah
        </h1>
        
        <p style={{ fontSize: '14px', color: '#64748b', margin: '0 0 24px 0', lineHeight: 1.5 }}>
          Sistem deteksi dini dan pemantauan risiko masalah gizi siswa madrasah berbasis Artificial Intelligence.
        </p>

        <div style={{
          backgroundColor: '#f1f5f9',
          padding: '16px',
          borderRadius: '12px',
          marginBottom: '24px',
          textAlign: 'left'
        }}>
          <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600, marginBottom: '6px' }}>
            Build Status & Informasi APK
          </div>
          <div style={{ fontSize: '14px', color: '#334155', display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
            <span>Versi Output:</span>
            <span style={{ fontWeight: 600, fontFamily: 'monospace' }}>NutriMind-release-{timestamp || '20260913_latest'}.apk</span>
          </div>
          <div style={{ fontSize: '14px', color: '#334155', display: 'flex', justifyContent: 'space-between' }}>
            <span>Auto-timestamp:</span>
            <span style={{ color: '#16a34a', fontWeight: 600 }}>Aktif (build.gradle.kts)</span>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <a
            href="/NutriMind-release.apk"
            style={{
              display: 'block',
              backgroundColor: '#0284c7',
              color: '#ffffff',
              padding: '14px 20px',
              borderRadius: '10px',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '15px',
              transition: 'background-color 0.2s',
              cursor: 'pointer'
            }}
          >
            Unduh APK NutriMind Terbaru
          </a>

          <div style={{ fontSize: '12px', color: '#94a3b8', marginTop: '6px' }}>
            File APK diberi nama unik secara otomatis berdasarkan timestamp build untuk mencegah caching versi lama di browser atau CDN.
          </div>
        </div>
      </div>
    </div>
  );
}
