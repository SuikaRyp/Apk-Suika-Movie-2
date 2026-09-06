# 🎬 SUIKAMOVIE - Premium Multi-Platform Movie & TV Series Streaming

<div align="center">

![SUIKAMOVIE Banner](https://raw.githubusercontent.com/berusigma/lk21-stream-premium/refs/heads/main/public/Screenshot_20260829-225932_1.jpg)

### **Aplikasi Streaming Film & Serial TV Sub Indo Modern, Cepat & Lintas Platform (Android, iOS & Web)**

[![Capacitor Version](https://img.shields.io/badge/Capacitor-7.6.8-blue?style=for-the-badge&logo=capacitor)](https://capacitorjs.com/)
[![Platforms](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Web-brightgreen?style=for-the-badge&logo=android)](https://github.com/berusigma/lk21-stream-premium)
[![Build Status](https://img.shields.io/badge/Build-GitHub%20Actions%20CI%2FCD-2088FF?style=for-the-badge&logo=github-actions)](https://github.com/berusigma/lk21-stream-premium/actions)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

---

</div>

## 🌟 Tangkapan Layar Aplikasi (App Previews)

<div align="center">

| 🎬 Beranda & Katalog Film | 📺 Pemutar Video & Multi-Server |
| :---: | :---: |
| ![Katalog Film](https://raw.githubusercontent.com/berusigma/lk21-stream-premium/refs/heads/main/public/Screenshot_20260829-225932_1.jpg) | ![Video Player](https://raw.githubusercontent.com/berusigma/lk21-stream-premium/refs/heads/main/public/Screenshot_20260829-225948_1.jpg) |

</div>

<div align="center">

### 📱 Tampilan Pencarian Film
![Multi-Platform Mockup](https://raw.githubusercontent.com/berusigma/lk21-stream-premium/refs/heads/main/public/Screenshot_20260829-230009_1.jpg)

</div>

---

## ✨ Fitur-Fitur Unggulan

- 🍿 **Katalog Terlengkap Sub Indo**: Akses ribuan judul film box office, drama Asia, anime, dan serial TV terbaru dengan subtitle Indonesia.
- ⚡ **Multi-Server Streaming**: Dukungan server streaming cadangan (Server VIP, Fast, Ultra) yang dapat berganti secara otomatis.
- 🔀 **Acak Film Baru (Random Feed Engine)**: Temukan rekomendasi film menarik secara acak menggunakan algoritma Fisher-Yates Shuffle dan pagination API acak.
- 🖥️ **Mode Layar Penuh Dedicated**: Pemutar video bawaan yang mendukung layar penuh interaktif dan manajemen sesi putar (`popstate` session persistence).
- 🔍 **Pencarian Pintar & Filter Genre**: Pencarian film secepat kilat dengan kustomisasi kategori (Action, Drama, Sci-Fi, Horror, Thriller, dll).
- 📱 **Multi-Platform (Single Codebase)**: Kode utama berbasis Web Tech (HTML5, Modern CSS Glassmorphism, JS) yang disinkronkan ke **Android APK** & **iOS IPA** via Capacitor 7.
- 🤖 **CI/CD Pipeline GitHub Actions**: Kompilasi APK Release ter-signed secara otomatis setiap ada push ke cabang `main`.

---

## 📁 Struktur Direktori Proyek

```text
lk21-stream-premium/
├── .github/
│   └── workflows/
│       └── build-suikamovie.yml        # Pipeline CI/CD GitHub Actions (Android & iOS)
├── android/                         # Proyek Native Android (Gradle & App Icon)
├── ios/                             # Proyek Native iOS (Xcode Workspace & AppIcon)
├── public/                          # ⭐ Basis Kode Utama Application (Single Codebase)
│   ├── index.html                   # Tampilan UI Utama & Modal Player
│   ├── style.css                    # Design System Dark Mode Glassmorphism
│   ├── app.js                       # Logika Streaming, API Provider & Event Listeners
│   └── assets/                      # Asset Gambar, Icon & Screenshots
│       └── previews/                # Foto Preview README (preview1.jpg, preview2.jpg, preview3.jpg)
├── capacitor.config.json            # Konfigurasi Capacitor (App ID & App Name)
├── package.json                     # Daftar Dependensi NPM & Script Command
└── README.md                        # Dokumentasi Resmi SUIKAMOVIE
```

---

## 🚀 Panduan Penggunaan & Pengembangan Lokal

### 1. Kloning Repository & Install Dependensi

```bash
git clone https://github.com/berusigma/lk21-stream-premium.git
cd lk21-stream-premium
npm install
```

### 2. Jalankan Server Lokal (Web)

Buka `public/index.html` langsung di browser Anda atau gunakan local server:

```bash
npx serve public
```

---

## 📱 Membangun Aplikasi Android & iOS

### Sinkronkan Kode Web ke Native

Setiap kali Anda melakukan perubahan di dalam folder `public/`, jalankan:

```bash
npm run cap:sync
```

Perintah ini akan menyalin seluruh aset web ke direktori Android dan iOS.

### Menjalankan di Android Studio

```bash
npm run cap:open:android
```

### Menjalankan di Xcode (iOS)

```bash
npm run cap:open:ios
```

---

## ⚙️ Build Otomatis via GitHub Actions (CI/CD)

Proyek ini telah dilengkapi dengan workflow GitHub Actions (`.github/workflows/build-suikamovie.yml`).

Setiap kali Anda melakukan **`git push origin main`**, GitHub Actions akan otomatis:
1. 📱 **Build Android Release APK**: Mengkompilasi APK signed dengan digital certificate bawaan.
2. 🍎 **Build iOS IPA Package**: Mengkompilasi bundle iOS `.ipa` siap pakai.
3. 🌐 **Build Web Bundle**: Menyiapkan artefak produksi web bundle.

Hasil build APK & IPA dapat diunduh pada tab **Actions** di repository GitHub Anda!

---

## 📄 Lisensi

Proyek ini dilindungi di bawah lisensi [MIT License](LICENSE).

Dibuat dengan ❤️ oleh **[berusigma](https://github.com/berusigma)**
