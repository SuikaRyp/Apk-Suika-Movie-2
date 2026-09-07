import { firebaseConfig } from "./firebase-config.js";
import { initializeApp, getApps, getApp } from "https://www.gstatic.com/firebasejs/10.13.2/firebase-app.js";
import {
  getFirestore,
  doc,
  getDoc,
  setDoc,
  updateDoc,
  increment,
  serverTimestamp,
  collection,
  getDocs,
  query,
  orderBy,
} from "https://www.gstatic.com/firebasejs/10.13.2/firebase-firestore.js";
import {
  getStorage,
  ref,
  uploadBytes,
  getDownloadURL,
} from "https://www.gstatic.com/firebasejs/10.13.2/firebase-storage.js";

// firebase-app cuma boleh di-initializeApp() sekali per config; auth.js juga
// nge-init app yang sama, jadi kita reuse instance yang udah ada kalau ada.
const firebaseApp = getApps().length ? getApp() : initializeApp(firebaseConfig);
const db = getFirestore(firebaseApp);
const storage = getStorage(firebaseApp);

// Email yang otomatis dianggap OWNER/ADMIN aplikasi. Ganti/tambah di sini
// kalau mau nambah admin lain nantinya.
export const ADMIN_EMAILS = ["ciulbotak25@gmail.com"];

export function isAdminEmail(email) {
  return !!email && ADMIN_EMAILS.includes(email.toLowerCase());
}

/* ==========================================================================
   SISTEM LEVEL & EXP
   Kurva EXP progresif (makin tinggi level, makin banyak EXP dibutuhin):
   cumulative EXP buat nyampe level N = 50 * (N-1) * N
   ========================================================================== */
export const EXP_PER_WATCH = 20;

function cumulativeExpForLevel(level) {
  return 50 * (level - 1) * level;
}

const LEVEL_TITLE_TIERS = [
  { max: 4, title: "Pemula", theme: "tier-gray" },
  { max: 9, title: "Penikmat Film", theme: "tier-blue" },
  { max: 19, title: "Kolektor Genre", theme: "tier-purple" },
  { max: 34, title: "Master Streaming", theme: "tier-emerald" },
  { max: 49, title: "Legenda SuikaMovie", theme: "tier-gold" },
  { max: Infinity, title: "SUIKA GRANDMASTER", theme: "tier-legend" },
];

export function getTitleForLevel(level) {
  return LEVEL_TITLE_TIERS.find((t) => level <= t.max) || LEVEL_TITLE_TIERS[LEVEL_TITLE_TIERS.length - 1];
}

export function getLevelInfo(exp) {
  exp = exp || 0;
  let level = 1;
  while (cumulativeExpForLevel(level + 1) <= exp) level++;

  const currentLevelBaseExp = cumulativeExpForLevel(level);
  const nextLevelExp = cumulativeExpForLevel(level + 1);
  const expIntoLevel = exp - currentLevelBaseExp;
  const expNeededForLevel = nextLevelExp - currentLevelBaseExp;
  const progressPercent = Math.min(100, Math.round((expIntoLevel / expNeededForLevel) * 100));
  const { title, theme } = getTitleForLevel(level);

  return {
    level,
    title,
    theme,
    exp,
    expIntoLevel,
    expNeededForLevel,
    nextLevelExp,
    progressPercent,
  };
}

/* ==========================================================================
   PROFIL USER DI FIRESTORE (koleksi "users", 1 dokumen per UID)
   ========================================================================== */
export async function ensureUserProfile(user) {
  if (!user) return null;
  const ref_ = doc(db, "users", user.uid);
  const snap = await getDoc(ref_);

  if (!snap.exists()) {
    await setDoc(ref_, {
      uid: user.uid,
      displayName: user.displayName || user.email?.split("@")[0] || "Pengguna",
      email: user.email || "",
      photoURL: user.photoURL || "",
      exp: 0,
      watchCount: 0,
      createdAt: serverTimestamp(),
      lastLoginAt: serverTimestamp(),
    });
    return { exp: 0, watchCount: 0 };
  }

  // Sinkronin data profil dasar (siapa tau ganti nama/foto dari provider login)
  await updateDoc(ref_, {
    displayName: user.displayName || snap.data().displayName || "Pengguna",
    email: user.email || snap.data().email || "",
    photoURL: user.photoURL || snap.data().photoURL || "",
    lastLoginAt: serverTimestamp(),
  });

  return snap.data();
}

export async function getUserProfile(uid) {
  const snap = await getDoc(doc(db, "users", uid));
  return snap.exists() ? snap.data() : null;
}

/* Dipanggil tiap user mulai nonton film/episode -> nambah EXP */
export async function awardWatchExp(uid, amount = EXP_PER_WATCH) {
  if (!uid) return;
  try {
    await updateDoc(doc(db, "users", uid), {
      exp: increment(amount),
      watchCount: increment(1),
      lastWatchAt: serverTimestamp(),
    });
  } catch (err) {
    console.warn("Gagal nambah EXP:", err);
  }
}

/* ==========================================================================
   AVATAR (UPLOAD DARI GALERI / KAMERA) -> FIREBASE STORAGE
   ========================================================================== */
export async function uploadAvatar(uid, file) {
  const fileRef = ref(storage, `avatars/${uid}.jpg`);
  await uploadBytes(fileRef, file, { contentType: file.type || "image/jpeg" });
  const url = await getDownloadURL(fileRef);
  await updateDoc(doc(db, "users", uid), { photoURL: url });
  return url;
}

/* ==========================================================================
   ADMIN: LIHAT SEMUA USER TERDAFTAR
   Catatan: ini cuma jalan kalau Firestore Security Rules ngizinin admin
   baca koleksi "users" secara penuh (lihat firestore.rules).
   ========================================================================== */
export async function fetchAllUsersForAdmin() {
  const q = query(collection(db, "users"), orderBy("exp", "desc"));
  const snap = await getDocs(q);
  const users = [];
  snap.forEach((d) => users.push({ id: d.id, ...d.data() }));
  return users;
}
