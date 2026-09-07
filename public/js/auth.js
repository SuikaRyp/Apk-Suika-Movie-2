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
  signOut,
  sendPasswordResetEmail,
  EmailAuthProvider,
  reauthenticateWithCredential,
  updatePassword,
} from "https://www.gstatic.com/firebasejs/10.13.2/firebase-auth.js";

const firebaseApp = initializeApp(firebaseConfig);
const auth = getAuth(firebaseApp);
const googleProvider = new GoogleAuthProvider();

// Biar user nggak perlu login ulang tiap buka app lagi - sesi kesimpen
// sampai dia pencet "Keluar" sendiri.
setPersistence(auth, browserLocalPersistence).catch(() => {});

/* ---------- DOM REFS ---------- */
const el = {
  overlay: document.getElementById("authOverlay"),
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

/* ---------- GOOGLE SIGN-IN ---------- */
el.btnGoogleSignIn.addEventListener("click", async () => {
  clearAuthMessages();
  try {
    await signInWithPopup(auth, googleProvider);
  } catch (err) {
    // Catatan: login Google via popup butuh browser standar. Di dalam
    // WebView Android biasa (bukan Chrome Custom Tab), Google kadang
    // menolak popup OAuth-nya demi keamanan. Kalau ini kejadian di app
    // Android-mu, solusi paling stabil adalah pasang plugin native
    // @capacitor-firebase/authentication supaya Google Sign-In lewat
    // native Google Play Services, bukan WebView.
    showAuthError(friendlyAuthError(err));
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

/* ---------- GERBANG UTAMA: TAMPIL APP CUMA KALAU SUDAH LOGIN ---------- */
onAuthStateChanged(auth, (user) => {
  if (user) {
    el.overlay.classList.add("auth-overlay-hidden");
    document.body.style.overflow = "";
    clearAuthMessages();
    el.loginForm.reset();
    el.registerForm.reset();

    window.startSuikaApp?.();
    window.renderAccountPage?.();
  } else {
    el.overlay.classList.remove("auth-overlay-hidden");
    document.body.style.overflow = "hidden";
  }
});

/* Diekspos ke app.js buat baca data user aktif (nama, email, foto, provider) */
window.SuikaAuth = {
  getCurrentUser: () => auth.currentUser,
  logout: () => signOut(auth),
};
