import { firebaseConfig } from "./firebase-config.js";
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.13.2/firebase-app.js";
import {
  getAuth,
  setPersistence,
  browserLocalPersistence,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  updateProfile,
  GoogleAuthProvider,
  signInWithPopup,
  signInWithCredential,
  signOut,
  sendPasswordResetEmail,
  EmailAuthProvider,
  reauthenticateWithCredential,
  updatePassword,
} from "https://www.gstatic.com/firebasejs/10.13.2/firebase-auth.js";
import {
  ensureUserProfile,
  getUserProfile,
  awardWatchExp,
  uploadAvatar,
  getLevelInfo,
  isAdminEmail,
  fetchAllUsersForAdmin,
} from "./user-data.js";

const firebaseApp = initializeApp(firebaseConfig);
const auth = getAuth(firebaseApp);
const googleProvider = new GoogleAuthProvider();

// Biar user nggak perlu login ulang tiap buka app lagi - sesi kesimpen
// sampai dia pencet "Keluar" sendiri.
setPersistence(auth, browserLocalPersistence).catch(() => {});

/* ---------- DOM REFS ---------- */
const el = {
  overlay: document.getElementById("authOverlay"),
  authChecking: document.getElementById("authChecking"),
  authCard: document.getElementById("authCard"),
  tabs: document.querySelectorAll(".auth-tab-btn"),
  loginForm: document.getElementById("loginForm"),
  registerForm: document.getElementById("registerForm"),
  errorBox: document.getElementById("authError"),
  infoBox: document.getElementById("authInfo"),
  btnForgotPassword: document.getElementById("btnForgotPassword"),
  btnGoogleSignIn: document.getElementById("btnGoogleSignIn"),
  btnLoginSubmit: document.getElementById("btnLoginSubmit"),
  btnRegisterSubmit: document.getElementById("btnRegisterSubmit"),

  // Account page (viewAkun)
  btnToggleChangePassword: document.getElementById("btnToggleChangePassword"),
  changePasswordForm: document.getElementById("changePasswordForm"),
  changePasswordMsg: document.getElementById("changePasswordMsg"),
  btnLogout: document.getElementById("btnLogout"),
  btnEditAvatar: document.getElementById("btnEditAvatar"),
  avatarSheet: document.getElementById("avatarSheet"),
  btnAvatarCamera: document.getElementById("btnAvatarCamera"),
  btnAvatarGallery: document.getElementById("btnAvatarGallery"),
  btnAvatarCancel: document.getElementById("btnAvatarCancel"),
  avatarCameraInput: document.getElementById("avatarCameraInput"),
  avatarGalleryInput: document.getElementById("avatarGalleryInput"),

  // Admin panel
  adminSection: document.getElementById("adminSection"),
  adminTotalUsers: document.getElementById("adminTotalUsers"),
  adminUserList: document.getElementById("adminUserList"),
  btnRefreshAdmin: document.getElementById("btnRefreshAdmin"),
};

/* ---------- PESAN ERROR FIREBASE -> BAHASA INDONESIA ---------- */
function friendlyAuthError(err) {
  const code = err?.code || "";
  const map = {
    "auth/invalid-email": "Format email tidak valid.",
    "auth/user-disabled": "Akun ini telah dinonaktifkan.",
    "auth/user-not-found": "Email belum terdaftar. Coba daftar dulu.",
    "auth/wrong-password": "Password salah. Coba lagi.",
    "auth/invalid-credential": "Email atau password salah.",
    "auth/email-already-in-use": "Email ini sudah terdaftar. Coba masuk saja.",
    "auth/weak-password": "Password terlalu lemah, minimal 6 karakter.",
    "auth/too-many-requests": "Terlalu banyak percobaan. Coba lagi beberapa saat lagi.",
    "auth/network-request-failed": "Koneksi internet bermasalah. Cek jaringan kamu.",
    "auth/popup-closed-by-user": "Jendela login Google ditutup sebelum selesai.",
    "auth/cancelled-popup-request": "Proses login Google dibatalkan.",
    "auth/operation-not-allowed": "Metode login ini belum diaktifkan di server.",
    "auth/unauthorized-domain": "Domain aplikasi ini belum diizinkan untuk login Google.",
    "auth/popup-blocked": "Popup login Google diblokir browser/WebView. Coba lewat email & password dulu ya.",
  };
  return map[code] || err?.message || "Terjadi kesalahan, coba lagi.";
}

function showAuthError(msg) {
  el.infoBox.classList.add("hidden");
  el.errorBox.textContent = msg;
  el.errorBox.classList.remove("hidden");
}

function showAuthInfo(msg) {
  el.errorBox.classList.add("hidden");
  el.infoBox.textContent = msg;
  el.infoBox.classList.remove("hidden");
}

function clearAuthMessages() {
  el.errorBox.classList.add("hidden");
  el.infoBox.classList.add("hidden");
}

function setButtonLoading(btn, loading, loadingText, defaultText) {
  btn.disabled = loading;
  btn.textContent = loading ? loadingText : defaultText;
}

/* ---------- TAB SWITCHING (LOGIN / REGISTER) ---------- */
el.tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    el.tabs.forEach((t) => t.classList.remove("active"));
    tab.classList.add("active");
    clearAuthMessages();

    if (tab.dataset.tab === "login") {
      el.loginForm.classList.remove("hidden");
      el.registerForm.classList.add("hidden");
    } else {
      el.registerForm.classList.remove("hidden");
      el.loginForm.classList.add("hidden");
    }
  });
});

/* ---------- LOGIN (EMAIL & PASSWORD) ---------- */
el.loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearAuthMessages();

  const email = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPassword").value;

  setButtonLoading(el.btnLoginSubmit, true, "Memproses...", "Masuk");
  try {
    await signInWithEmailAndPassword(auth, email, password);
  } catch (err) {
    showAuthError(friendlyAuthError(err));
  } finally {
    setButtonLoading(el.btnLoginSubmit, false, "Memproses...", "Masuk");
  }
});

/* ---------- LUPA PASSWORD ---------- */
el.btnForgotPassword.addEventListener("click", async () => {
  clearAuthMessages();
  const email = document.getElementById("loginEmail").value.trim();
  if (!email) {
    showAuthError("Isi dulu email kamu di atas, baru pencet 'Lupa password?'.");
    return;
  }
  try {
    await sendPasswordResetEmail(auth, email);
    showAuthInfo(`Link reset password sudah dikirim ke ${email}. Cek inbox/spam ya.`);
  } catch (err) {
    showAuthError(friendlyAuthError(err));
  }
});

/* ---------- REGISTER (EMAIL & PASSWORD) ---------- */
el.registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearAuthMessages();

  const name = document.getElementById("registerName").value.trim();
  const email = document.getElementById("registerEmail").value.trim();
  const password = document.getElementById("registerPassword").value;
  const confirmPassword = document.getElementById("registerConfirmPassword").value;

  if (password !== confirmPassword) {
    showAuthError("Konfirmasi password tidak sama dengan password di atas.");
    return;
  }
  if (password.length < 6) {
    showAuthError("Password minimal 6 karakter.");
    return;
  }

  setButtonLoading(el.btnRegisterSubmit, true, "Mendaftarkan...", "Daftar Akun");
  try {
    const cred = await createUserWithEmailAndPassword(auth, email, password);
    if (name) {
      await updateProfile(cred.user, { displayName: name });
    }
  } catch (err) {
    showAuthError(friendlyAuthError(err));
  } finally {
    setButtonLoading(el.btnRegisterSubmit, false, "Mendaftarkan...", "Daftar Akun");
  }
});

/* ---------- GOOGLE SIGN-IN ----------
   Di dalam app Android/iOS (native), kita pakai plugin native
   @capacitor-firebase/authentication -> munculin akun picker asli
   (semua akun Google yang login di HP), bukan popup di dalam WebView.
   WebView OAuth popup biasa DITOLAK Google demi keamanan, makanya
   sebelumnya bisa kelihatan nge-bug / nggak jalan sama sekali.
   Setelah dapet ID token dari native, kita sinkronkan ke Firebase JS
   SDK juga (signInWithCredential) biar auth.currentUser & seluruh
   fitur lain (ubah password dll) tetap konsisten.
   Kalau dibuka lewat browser biasa (bukan app), fallback ke popup web. */
let googleSignInInProgress = false;

el.btnGoogleSignIn.addEventListener("click", async () => {
  if (googleSignInInProgress) return;
  googleSignInInProgress = true;
  clearAuthMessages();

  const originalLabel = el.btnGoogleSignIn.querySelector("span").textContent;
  el.btnGoogleSignIn.disabled = true;
  el.btnGoogleSignIn.querySelector("span").textContent = "Menghubungkan...";

  try {
    const isNative = window.Capacitor?.isNativePlatform?.();
    const nativeFirebaseAuth = window.Capacitor?.Plugins?.FirebaseAuthentication;

    if (isNative && nativeFirebaseAuth) {
      const result = await nativeFirebaseAuth.signInWithGoogle();
      const idToken = result?.credential?.idToken;
      const accessToken = result?.credential?.accessToken;

      if (!idToken) {
        throw new Error("Tidak menerima ID token dari Google. Coba lagi.");
      }

      // Samain sesi native dengan Firebase JS SDK biar seluruh app
      // (ubah password, onAuthStateChanged, dll) baca user yang sama.
      const credential = GoogleAuthProvider.credential(idToken, accessToken);
      await signInWithCredential(auth, credential);
    } else if (isNative && !nativeFirebaseAuth) {
      throw new Error(
        "Plugin native Google Sign-In belum terpasang. Jalankan 'npm install' lalu 'npx cap sync android' dulu."
      );
    } else {
      // Fallback buat preview di browser biasa / desktop (Tauri).
      await signInWithPopup(auth, googleProvider);
    }
  } catch (err) {
    // Batal/tutup picker akun bukan error sungguhan, jadi nggak perlu nampilin pesan merah.
    const cancelledCodes = ["auth/popup-closed-by-user", "auth/cancelled-popup-request", "12501", "canceled"];
    const isCancelled = cancelledCodes.some((c) => String(err?.code || err?.message || "").includes(c));
    if (!isCancelled) {
      showAuthError(friendlyAuthError(err));
    }
  } finally {
    googleSignInInProgress = false;
    el.btnGoogleSignIn.disabled = false;
    el.btnGoogleSignIn.querySelector("span").textContent = originalLabel;
  }
});

/* ---------- LOGOUT ---------- */
el.btnLogout.addEventListener("click", async () => {
  try {
    await signOut(auth);
    showToastSafe("Berhasil keluar dari akun.");
  } catch (err) {
    showToastSafe("Gagal keluar, coba lagi.");
  }
});

/* ---------- UBAH PASSWORD ---------- */
el.btnToggleChangePassword.addEventListener("click", () => {
  el.changePasswordForm.classList.toggle("hidden");
});

el.changePasswordForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const msgBox = el.changePasswordMsg;
  msgBox.classList.remove("hidden", "success");
  msgBox.textContent = "";

  const currentPassword = document.getElementById("currentPassword").value;
  const newPassword = document.getElementById("newPassword").value;
  const confirmNewPassword = document.getElementById("confirmNewPassword").value;
  const user = auth.currentUser;

  if (!user || !user.email) {
    msgBox.textContent = "Sesi login bermasalah, coba login ulang.";
    return;
  }
  if (newPassword !== confirmNewPassword) {
    msgBox.textContent = "Konfirmasi password baru tidak cocok.";
    return;
  }
  if (newPassword.length < 6) {
    msgBox.textContent = "Password baru minimal 6 karakter.";
    return;
  }

  try {
    const credential = EmailAuthProvider.credential(user.email, currentPassword);
    await reauthenticateWithCredential(user, credential);
    await updatePassword(user, newPassword);

    msgBox.textContent = "Password berhasil diubah!";
    msgBox.classList.add("success");
    el.changePasswordForm.reset();
    setTimeout(() => el.changePasswordForm.classList.add("hidden"), 1200);
  } catch (err) {
    msgBox.textContent = friendlyAuthError(err);
  }
});

function showToastSafe(msg) {
  const toast = document.getElementById("toast");
  const toastMsg = document.getElementById("toastMessage");
  if (!toast || !toastMsg) return;
  toastMsg.textContent = msg;
  toast.classList.remove("hidden");
  setTimeout(() => toast.classList.add("hidden"), 2500);
}

/* ---------- GANTI AVATAR (KAMERA / GALERI) ---------- */
el.btnEditAvatar.addEventListener("click", () => {
  el.avatarSheet.classList.remove("hidden");
});
el.btnAvatarCancel.addEventListener("click", () => {
  el.avatarSheet.classList.add("hidden");
});
el.avatarSheet.addEventListener("click", (e) => {
  if (e.target === el.avatarSheet) el.avatarSheet.classList.add("hidden");
});
el.btnAvatarCamera.addEventListener("click", () => {
  el.avatarSheet.classList.add("hidden");
  el.avatarCameraInput.click();
});
el.btnAvatarGallery.addEventListener("click", () => {
  el.avatarSheet.classList.add("hidden");
  el.avatarGalleryInput.click();
});

async function handleAvatarFile(fileInput) {
  const file = fileInput.files?.[0];
  fileInput.value = ""; // reset biar bisa pilih file yang sama lagi lain kali
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    showToastSafe("File yang dipilih bukan gambar.");
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    showToastSafe("Ukuran foto maksimal 5MB.");
    return;
  }

  const user = auth.currentUser;
  if (!user) return;

  showToastSafe("Mengunggah foto profil...");
  try {
    const url = await uploadAvatar(user.uid, file);
    await updateProfile(user, { photoURL: url });
    cachedProfile = { ...(cachedProfile || {}), photoURL: url };
    window.renderAccountPage?.();
    showToastSafe("Foto profil berhasil diganti!");
  } catch (err) {
    console.error(err);
    showToastSafe("Gagal unggah foto. Coba lagi.");
  }
}
el.avatarCameraInput.addEventListener("change", () => handleAvatarFile(el.avatarCameraInput));
el.avatarGalleryInput.addEventListener("change", () => handleAvatarFile(el.avatarGalleryInput));

/* ---------- ADMIN PANEL ---------- */
el.btnRefreshAdmin?.addEventListener("click", () => loadAdminPanel());

async function loadAdminPanel() {
  if (!isAdminEmail(auth.currentUser?.email)) return;
  el.adminSection.classList.remove("hidden");
  el.adminUserList.innerHTML = `<div class="admin-loading">Memuat data pengguna...</div>`;

  try {
    const users = await fetchAllUsersForAdmin();
    el.adminTotalUsers.textContent = users.length;

    if (!users.length) {
      el.adminUserList.innerHTML = `<div class="admin-loading">Belum ada pengguna terdaftar.</div>`;
      return;
    }

    el.adminUserList.innerHTML = users
      .map((u) => {
        const info = getLevelInfo(u.exp || 0);
        const joined = u.createdAt?.toDate ? u.createdAt.toDate().toLocaleDateString("id-ID") : "-";
        return `
          <div class="admin-user-row">
            <div class="admin-user-main">
              <span class="admin-user-name">${escapeHtml(u.displayName || "-")}</span>
              <span class="admin-user-email">${escapeHtml(u.email || "-")}</span>
            </div>
            <div class="admin-user-meta">
              <span class="admin-user-level ${info.theme}">Lv.${info.level}</span>
              <span class="admin-user-date">${joined}</span>
            </div>
          </div>`;
      })
      .join("");
  } catch (err) {
    console.error(err);
    el.adminUserList.innerHTML = `<div class="admin-loading">Gagal memuat data. Cek Firestore Security Rules kamu (lihat firestore.rules).</div>`;
  }
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

/* ---------- GERBANG UTAMA: TAMPIL APP CUMA KALAU SUDAH LOGIN ---------- */
let cachedProfile = null;

onAuthStateChanged(auth, async (user) => {
  // Begitu Firebase selesai ngecek sesi (baik ketemu user maupun nggak),
  // spinner "Memeriksa sesi login..." langsung disembunyikan.
  el.authChecking.classList.add("hidden");

  if (user) {
    // Udah login sebelumnya -> langsung ke halaman film, form login/daftar
    // nggak perlu sempet nongol sama sekali.
    el.authCard.classList.add("hidden");
    el.overlay.classList.add("auth-overlay-hidden");
    document.body.style.overflow = "";
    clearAuthMessages();
    el.loginForm.reset();
    el.registerForm.reset();

    window.startSuikaApp?.();

    try {
      cachedProfile = await ensureUserProfile(user);
      window.renderAccountPage?.();

      if (isAdminEmail(user.email)) {
        loadAdminPanel();
      } else {
        el.adminSection.classList.add("hidden");
      }
    } catch (err) {
      console.warn("Firestore belum siap / gagal dimuat:", err);
      showToastSafe("Fitur level/EXP belum aktif — pastikan Firestore sudah dinyalakan di Firebase Console.");
    }
  } else {
    // Belum login -> baru sekarang form login/daftar ditampilkan.
    cachedProfile = null;
    el.overlay.classList.remove("auth-overlay-hidden");
    el.authCard.classList.remove("hidden");
    document.body.style.overflow = "hidden";
  }
});

/* Diekspos ke app.js buat baca data user aktif (nama, email, foto, level, EXP, admin) */
window.SuikaAuth = {
  getCurrentUser: () => auth.currentUser,
  getProfile: () => cachedProfile,
  isAdmin: () => isAdminEmail(auth.currentUser?.email),
  getLevelInfo: (exp) => getLevelInfo(exp),
  logout: () => signOut(auth),

  // Dipanggil dari app.js tiap user mulai nonton film/episode.
  awardWatchExp: async () => {
    const user = auth.currentUser;
    if (!user) return;

    try {
      const prevLevel = getLevelInfo(cachedProfile?.exp || 0).level;
      await awardWatchExp(user.uid);

      cachedProfile = await getUserProfile(user.uid);
      const newLevelInfo = getLevelInfo(cachedProfile?.exp || 0);

      window.renderAccountPage?.();

      if (newLevelInfo.level > prevLevel) {
        showToastSafe(`🎉 Naik ke Level ${newLevelInfo.level} — ${newLevelInfo.title}!`);
      }
    } catch (err) {
      console.warn("Gagal nambah EXP (Firestore belum siap?):", err);
    }
  },
};
