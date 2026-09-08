/*
 * SUIKAMOVIE PREMIUM - STREAM ENGINE & HP SYSTEM INFO
 * Author: berusigma / SUIKAMOVIE Team
 */

const TMDB_API_KEY = "82524e2faef91706a2d52d52496130ac";
const TMDB_BASE = "https://api.themoviedb.org/3";
const TMDB_IMG = "https://image.tmdb.org/t/p/w500";
const TMDB_IMG_ORIGINAL = "https://image.tmdb.org/t/p/original";

/* SERVER SELECTION MAP - RENAMED STRICTLY TO SERVER 1..7 WITH RELIABLE EMBEDS */
const SERVERS = {
  vidlink: { 
    name: "SERVER 1 (VIP)",
    movie: "https://vidlink.pro/movie/{id}", 
    tv: "https://vidlink.pro/tv/{id}/{s}/{e}" 
  },
  vidsrc: { 
    name: "SERVER 2 (FAST)",
    movie: "https://vidsrc.cc/v2/embed/movie/{id}", 
    tv: "https://vidsrc.cc/v2/embed/tv/{id}/{s}/{e}" 
  },
  embedsu: { 
    name: "SERVER 3 (HD)",
    movie: "https://embed.su/embed/movie/{id}", 
    tv: "https://embed.su/embed/tv/{id}/{s}/{e}" 
  },
  vidsrcpro: { 
    name: "SERVER 4",
    movie: "https://vidsrc.pro/embed/movie/{id}", 
    tv: "https://vidsrc.pro/embed/tv/{id}/{s}/{e}" 
  },
  autoembed: { 
    name: "SERVER 5",
    movie: "https://player.autoembed.cc/embed/movie/{id}", 
    tv: "https://player.autoembed.cc/embed/tv/{id}/{s}/{e}" 
  },
  superembed: { 
    name: "SERVER 6",
    movie: "https://multiembed.mov/directstream.php?video_id={id}&tmdb=1", 
    tv: "https://multiembed.mov/directstream.php?video_id={id}&tmdb=1&s={s}&e={e}" 
  },
  "2embed": { 
    name: "SERVER 7",
    movie: "https://www.2embed.cc/embed/{id}", 
    tv: "https://www.2embed.cc/embedtv/{id}&s={s}&e={e}" 
  }
};

/* SHUFFLE ARRAY HELPER */
function shuffleArray(arr) {
  if (!arr || !Array.isArray(arr)) return [];
  const array = [...arr];
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
  return array;
}

class SuikaMovieAPI {
  async _fetch(endpoint, params = {}) {
    try {
      const url = new URL(`${TMDB_BASE}${endpoint}`);
      url.searchParams.append("api_key", TMDB_API_KEY);
      url.searchParams.append("language", "id-ID");
      for (const [key, val] of Object.entries(params)) {
        url.searchParams.append(key, val);
      }
      const res = await fetch(url.toString());
      if (!res.ok) throw new Error(`TMDB HTTP error ${res.status}`);
      return await res.json();
    } catch (err) {
      console.warn("TMDB fetch error:", err.message);
      return null;
    }
  }

  async search(query) {
    const data = await this._fetch("/search/multi", { query, page: 1 });
    if (!data?.results) return [];
    return data.results
      .filter((r) => r.media_type === "movie" || r.media_type === "tv")
      .map((r) => ({
        id: r.id,
        title: r.title || r.name,
        type: r.media_type,
        year: (r.release_date || r.first_air_date || "").split("-")[0] || "2026",
        rating: r.vote_average ? r.vote_average.toFixed(1) : "8.5",
        poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
        backdrop: r.backdrop_path ? `${TMDB_IMG_ORIGINAL}${r.backdrop_path}` : null,
        overview: r.overview || "Sinopsis film pilihan penonton.",
        country: r.origin_country?.[0] || (r.original_language === "ko" ? "KR" : r.original_language === "ja" ? "JP" : "US")
      }));
  }

  async getTvSeasonDetails(id, seasonNumber) {
    return await this._fetch(`/tv/${id}/season/${seasonNumber}`);
  }

  async detail(id, type = "movie") {
    const data = await this._fetch(`/${type}/${id}`);
    const title = data?.title || data?.name || "Film Populer";
    const releaseDate = data?.release_date || data?.first_air_date || "2026";
    const year = releaseDate.split("-")[0] || "2026";
    const runtime = type === "movie" ? (data?.runtime ? `${data.runtime} min` : "120 min") : (data?.episode_run_time?.[0] ? `${data.episode_run_time[0]} min/eps` : "45 min/eps");
    const genres = data?.genres?.map((g) => g.name) || ["Action", "Drama", "Petualangan"];
    const countryCode = data?.production_countries?.[0]?.iso_3166_1 || data?.origin_country?.[0] || "US";

    let seasonsDetail = [];
    if (type === "tv" && data?.seasons && data.seasons.length > 0) {
      seasonsDetail = data.seasons
        .filter(s => s.season_number > 0)
        .map(s => ({
          seasonNumber: s.season_number,
          name: s.name || `Season ${s.season_number}`,
          episodeCount: s.episode_count || 10
        }));
    }

    return {
      id,
      title,
      type,
      year,
      rating: data?.vote_average ? data.vote_average.toFixed(1) : "8.7",
      runtime,
      genres,
      country: countryCode === "ID" ? "Indonesia" : countryCode === "KR" ? "Korea" : countryCode === "JP" ? "Jepang" : "Barat",
      overview: data?.overview || "Film spektakuler dengan alur cerita mendalam dan efek visual mengagumkan yang siap menghibur waktu santai Anda.",
      poster: data?.poster_path ? `${TMDB_IMG}${data.poster_path}` : "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
      backdrop: data?.backdrop_path ? `${TMDB_IMG_ORIGINAL}${data.backdrop_path}` : "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?auto=format&fit=crop&w=1000&q=80",
      seasons: type === "tv" ? (data?.number_of_seasons || seasonsDetail.length || 1) : 0,
      seasonsDetail
    };
  }

  async trending() {
    const page = Math.floor(Math.random() * 4) + 1;
    const data = await this._fetch("/trending/all/week", { page });
    if (!data?.results || data.results.length === 0) return shuffleArray(this._expandedFallbackList());
    return shuffleArray(data.results
      .filter((r) => r.media_type === "movie" || r.media_type === "tv")
      .map((r) => ({
        id: r.id,
        title: r.title || r.name,
        type: r.media_type,
        year: (r.release_date || r.first_air_date || "").split("-")[0] || "2026",
        rating: r.vote_average ? r.vote_average.toFixed(1) : "8.8",
        poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
        backdrop: r.backdrop_path ? `${TMDB_IMG_ORIGINAL}${r.backdrop_path}` : null,
        overview: r.overview || "Trending film pilihan terpopuler minggu ini.",
      })));
  }

  async popularMovies() {
    const page = Math.floor(Math.random() * 5) + 1;
    const data = await this._fetch("/movie/popular", { page });
    if (!data?.results || data.results.length === 0) return shuffleArray(this._expandedFallbackList("movie"));
    return shuffleArray(data.results.map((r) => ({
      id: r.id,
      title: r.title,
      type: "movie",
      year: (r.release_date || "").split("-")[0] || "2026",
      rating: r.vote_average ? r.vote_average.toFixed(1) : "8.5",
      poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
      backdrop: r.backdrop_path ? `${TMDB_IMG_ORIGINAL}${r.backdrop_path}` : null,
      overview: r.overview || "",
    })));
  }

  async popularTV() {
    const page = Math.floor(Math.random() * 5) + 1;
    const data = await this._fetch("/tv/popular", { page });
    if (!data?.results || data.results.length === 0) return shuffleArray(this._expandedFallbackList("tv"));
    return shuffleArray(data.results.map((r) => ({
      id: r.id,
      title: r.name,
      type: "tv",
      year: (r.first_air_date || "").split("-")[0] || "2026",
      rating: r.vote_average ? r.vote_average.toFixed(1) : "8.6",
      poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
      backdrop: r.backdrop_path ? `${TMDB_IMG_ORIGINAL}${r.backdrop_path}` : null,
      overview: r.overview || "",
    })));
  }

  _expandedFallbackList(type = "all") {
    const list = [
      { id: 653346, title: "Kingdom of Planet Apes", type: "movie", year: "2026", rating: "8.7", poster: "https://image.tmdb.org/t/p/w500/gKkl37BQuKTanygYQG1pyYgLVgf.jpg" },
      { id: 823464, title: "Godzilla x Kong", type: "movie", year: "2026", rating: "8.9", poster: "https://image.tmdb.org/t/p/w500/b0PlSFdDwbyK0cfOiBZaOfHQfeK.jpg" },
      { id: 573435, title: "Bad Boys: Ride or Die", type: "movie", year: "2026", rating: "8.4", poster: "https://image.tmdb.org/t/p/w500/nP6RliHjxH2uUjYqMZioHovLgvu.jpg" },
      { id: 1022789, title: "Inside Out 2", type: "movie", year: "2026", rating: "9.0", poster: "https://image.tmdb.org/t/p/w500/vpnP19zLqVGlOx1VoY8YeeOi9W5.jpg" },
      { id: 533535, title: "Deadpool & Wolverine", type: "movie", year: "2026", rating: "9.1", poster: "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg" },
      { id: 693134, title: "Dune: Part Two", type: "movie", year: "2026", rating: "8.8", poster: "https://image.tmdb.org/t/p/w500/1pdfLPoLMag8St8faOhvNUj9GlL.jpg" },
      { id: 872585, title: "Oppenheimer", type: "movie", year: "2025", rating: "8.9", poster: "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGvC2t78dG.jpg" },
      { id: 933260, title: "The Substance", type: "movie", year: "2026", rating: "8.6", poster: "https://image.tmdb.org/t/p/w500/l117yeUdMGRIFF3QvY6fTsjKZYx.jpg" },
      { id: 1184918, title: "The Wild Robot", type: "movie", year: "2026", rating: "8.7", poster: "https://image.tmdb.org/t/p/w500/v9L21IioP1uY9R91456uT1k31u4.jpg" },
      { id: 912649, title: "Venom: The Last Dance", type: "movie", year: "2026", rating: "8.3", poster: "https://image.tmdb.org/t/p/w500/k221nm0wDTHSTm2wqLQ8rLg2WvF.jpg" },
      { id: 93405, title: "Squid Game", type: "tv", year: "2026", rating: "8.9", poster: "https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNen3G82L.jpg" },
      { id: 94605, title: "Arcane", type: "tv", year: "2026", rating: "9.2", poster: "https://image.tmdb.org/t/p/w500/fqld2yobYU2FODohw4A42uLIwwh.jpg" },
      { id: 126308, title: "Shogun", type: "tv", year: "2026", rating: "8.8", poster: "https://image.tmdb.org/t/p/w500/7O4iVf26YScHaWFL9wFiYmBxMiK.jpg" },
      { id: 1396, title: "Breaking Bad", type: "tv", year: "2024", rating: "9.5", poster: "https://image.tmdb.org/t/p/w500/ztSlKpyE2zL4YLxmL2oB207iSpP.jpg" },
      { id: 92830, title: "Demon Slayer: Kimetsu no Yaiba", type: "tv", year: "2026", rating: "8.9", poster: "https://image.tmdb.org/t/p/w500/xUfVKlMSpfasLdF1j28BwPlbStb.jpg" },
      { id: 114479, title: "Agatha All Along", type: "tv", year: "2026", rating: "8.4", poster: "https://image.tmdb.org/t/p/w500/p487LllQ25nNf7oGZzM2vLp1PjS.jpg" },
      { id: 829280, title: "Enola Holmes 2", type: "movie", year: "2025", rating: "8.2", poster: "https://image.tmdb.org/t/p/w500/tegBawGEF7Xm2xDYTnhZ6w4fDqH.jpg" },
      { id: 76341, title: "Mad Max: Fury Road", type: "movie", year: "2024", rating: "8.7", poster: "https://image.tmdb.org/t/p/w500/8tZYtuYiF9u0DjwW02eYvexV2h2.jpg" }
    ];
    if (type !== "all") {
      const filtered = list.filter(item => item.type === type);
      return filtered.length > 0 ? filtered : list;
    }
    return list;
  }
}

/* APP STATE CONTROLLER */
const api = new SuikaMovieAPI();

let state = {
  trending: [],
  popularMovies: [],
  popularTV: [],
  heroItems: [],
  heroIndex: 0,
  heroTimer: null,
  activeView: "viewBeranda",
  currentDetail: null,
  activeServer: "vidlink",
  activeSeason: 1,
  activeEpisode: 1,
  searchHistory: JSON.parse(localStorage.getItem("suikamovie_search_history") || '["Avatar 3", "Squid Game 2", "Demon Slayer", "Siksa Kubur"]'),
  favorites: JSON.parse(localStorage.getItem("suikamovie_favorites") || "[]"),
  // Auto rotate ke landscape pas player di-fullscreen-in. Default ON, tapi
  // user bisa matiin lewat toggle di halaman Akun > Preferensi.
  autoRotateEnabled: localStorage.getItem("suikamovie_auto_rotate") !== "off"
};

/* DOM ELEMENTS */
const el = {
  topSearchInput: document.getElementById("topSearchInput"),
  btnTopSearchSubmit: document.getElementById("btnTopSearchSubmit"),
  btnClearTopSearch: document.getElementById("btnClearTopSearch"),
  topSearchDropdown: document.getElementById("topSearchDropdown"),
  categoryNavContainer: document.getElementById("categoryNavContainer"),

  heroBackdrop: document.getElementById("heroBackdrop"),
  heroTitle: document.getElementById("heroTitle"),
  heroRating: document.getElementById("heroRating"),
  heroYear: document.getElementById("heroYear"),
  heroType: document.getElementById("heroType"),
  heroGenres: document.getElementById("heroGenres"),
  heroOverview: document.getElementById("heroOverview"),
  btnHeroPlay: document.getElementById("btnHeroPlay"),
  btnHeroDetail: document.getElementById("btnHeroDetail"),
  heroIndicators: document.getElementById("heroIndicators"),

  recommendedScrollList: document.getElementById("recommendedScrollList"),
  btnRefreshHome: document.getElementById("btnRefreshHome"),
  rankingTabs: document.getElementById("rankingTabs"),
  rankingScrollList: document.getElementById("rankingScrollList"),
  categoryCardsGrid: document.getElementById("categoryCardsGrid"),
  actionMoviesList: document.getElementById("actionMoviesList"),
  tvSeriesList: document.getElementById("tvSeriesList"),

  bottomNavBar: document.getElementById("bottomNavBar"),
  navTabBtns: document.querySelectorAll(".nav-tab-btn"),
  views: document.querySelectorAll(".app-view"),

  pageSearchInput: document.getElementById("pageSearchInput"),
  btnPageSearchSubmit: document.getElementById("btnPageSearchSubmit"),
  searchHistoryChips: document.getElementById("searchHistoryChips"),
  btnClearHistory: document.getElementById("btnClearHistory"),
  verticalSearchResultsList: document.getElementById("verticalSearchResultsList"),

  detailModal: document.getElementById("detailModal"),
  btnDetailBack: document.getElementById("btnDetailBack"),
  btnDetailFav: document.getElementById("btnDetailFav"),
  detailBackdropImg: document.getElementById("detailBackdropImg"),
  btnDetailPlayCover: document.getElementById("btnDetailPlayCover"),
  detailInlinePlayer: document.getElementById("detailInlinePlayer"),
  detailTitle: document.getElementById("detailTitle"),
  detailRating: document.getElementById("detailRating"),
  detailYear: document.getElementById("detailYear"),
  detailCountry: document.getElementById("detailCountry"),
  detailRuntime: document.getElementById("detailRuntime"),
  detailTypeBadge: document.getElementById("detailTypeBadge"),
  detailGenresList: document.getElementById("detailGenresList"),
  detailOverviewText: document.getElementById("detailOverviewText"),
  sourcePillsWrap: document.getElementById("sourcePillsWrap"),
  detailTvControls: document.getElementById("detailTvControls"),
  detailSeasonSelect: document.getElementById("detailSeasonSelect"),
  detailEpisodeList: document.getElementById("detailEpisodeList"),
  detailRecommendationsList: document.getElementById("detailRecommendationsList"),
  btnAddToList: document.getElementById("btnAddToList"),
  btnPostComment: document.getElementById("btnPostComment"),
  commentInput: document.getElementById("commentInput"),
  commentsList: document.getElementById("commentsList"),

  // Account page elements
  accountAvatar: document.getElementById("accountAvatar"),
  accountName: document.getElementById("accountName"),
  accountEmail: document.getElementById("accountEmail"),
  accountProviderBadge: document.getElementById("accountProviderBadge"),
  accountAdminBadge: document.getElementById("accountAdminBadge"),
  accountLevelBadge: document.getElementById("accountLevelBadge"),
  accountTitleBadge: document.getElementById("accountTitleBadge"),
  accountExpBarFill: document.getElementById("accountExpBarFill"),
  accountExpLabel: document.getElementById("accountExpLabel"),
  accountWatchCount: document.getElementById("accountWatchCount"),
  btnToggleChangePassword: document.getElementById("btnToggleChangePassword"),
  changePasswordForm: document.getElementById("changePasswordForm"),
  changePasswordMsg: document.getElementById("changePasswordMsg"),
  googleOnlyNotice: document.getElementById("googleOnlyNotice"),
  btnLogout: document.getElementById("btnLogout"),
  toggleAutoRotate: document.getElementById("toggleAutoRotate"),

  toast: document.getElementById("toast"),
  toastMessage: document.getElementById("toastMessage")
};

/* INITIALIZATION */
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners();
  setupFullscreenOrientationLock();
  renderSearchHistory();
});

// Dipanggil dari auth.js setelah Firebase memastikan user sudah login.
// Sengaja dipisah dari wiring UI di atas biar data film baru ke-fetch
// SETELAH user lolos gerbang login, bukan dari awal app dibuka.
window.startSuikaApp = async function startSuikaApp() {
  if (window.__suikaAppStarted) return;
  window.__suikaAppStarted = true;

  await loadHomePageData();

  // RESTORE MOVIE DETAIL & PLAYER IF RETURNED FROM EXTERNAL AD REDIRECT OR BACK BUTTON
  restoreActivePlayerSession();
};

async function loadHomePageData() {
  try {
    const [trending, movies, tv] = await Promise.all([
      api.trending(),
      api.popularMovies(),
      api.popularTV()
    ]);

    state.trending = trending;
    state.popularMovies = movies;
    state.popularTV = tv;

    renderPosterCards(el.recommendedScrollList, state.trending.slice(0, 10));
    renderRankingCards(state.trending.slice(0, 10));
    renderPosterCards(el.actionMoviesList, state.popularMovies.slice(0, 10));
    renderPosterCards(el.tvSeriesList, state.popularTV.slice(0, 10));

    if (trending.length > 0) {
      state.heroItems = trending.slice(0, 5);
      renderHeroBanner(0);
      startHeroCarousel();
    }
  } catch (err) {
    console.error("App init error:", err);
  }
}

/* RESTORE ACTIVE STREAM SESSION AFTER RETURN FROM AD REDIRECT */
function restoreActivePlayerSession() {
  const savedSession = sessionStorage.getItem("suikamovie_active_detail");
  const hash = window.location.hash;
  
  let targetId = null;
  let targetType = "movie";
  let autoPlay = true;

  if (savedSession) {
    try {
      const data = JSON.parse(savedSession);
      targetId = data.id;
      targetType = data.type || "movie";
      autoPlay = data.autoPlay !== undefined ? data.autoPlay : true;
      if (data.server) state.activeServer = data.server;
      if (data.season) state.activeSeason = data.season;
      if (data.episode) state.activeEpisode = data.episode;
    } catch (e) {
      console.warn("Invalid session data:", e);
    }
  } else if (hash.startsWith("#detail-")) {
    targetId = parseInt(hash.replace("#detail-", ""));
  }

  if (targetId) {
    openDetailModal(targetId, targetType, autoPlay);
  }
}

/* EVENT LISTENERS SETUP */
function setupEventListeners() {
  // Toggle Auto Rotate (Akun > Preferensi) - simpan pilihan user di localStorage
  if (el.toggleAutoRotate) {
    el.toggleAutoRotate.checked = state.autoRotateEnabled;
    el.toggleAutoRotate.addEventListener("change", () => {
      state.autoRotateEnabled = el.toggleAutoRotate.checked;
      localStorage.setItem("suikamovie_auto_rotate", state.autoRotateEnabled ? "on" : "off");

      // Kalau dimatiin pas layar lagi kepaksa landscape, langsung unlock biar
      // nggak nyangkut di landscape.
      if (!state.autoRotateEnabled && screen.orientation && screen.orientation.unlock) {
        screen.orientation.unlock();
      }

      showToast(state.autoRotateEnabled ? "Auto rotate diaktifkan" : "Auto rotate dimatikan");
    });
  }

  // Refresh / Shuffle Movies Button
  if (el.btnRefreshHome) {
    el.btnRefreshHome.addEventListener("click", async (e) => {
      e.preventDefault();
      showToast("Memuat daftar film baru...");
      await loadHomePageData();
    });
  }

  // Handle Browser PopState / Hardware Back Button
  window.addEventListener("popstate", (e) => {
    const savedSession = sessionStorage.getItem("suikamovie_active_detail");
    if (savedSession) {
      restoreActivePlayerSession();
    } else if (!el.detailModal.classList.contains("hidden")) {
      closeDetailModal();
    }
  });

  // Native Capacitor App Hardware Back Button Listener
  if (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.App) {
    window.Capacitor.Plugins.App.addListener('backButton', () => {
      if (!el.detailModal.classList.contains("hidden")) {
        closeDetailModal();
      } else if (state.activeView !== "viewBeranda") {
        switchView("viewBeranda");
      } else {
        window.Capacitor.Plugins.App.exitApp();
      }
    });
  }

  // Bottom Navigation
  el.navTabBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      const targetView = btn.dataset.view;
      switchView(targetView);
    });
  });

  // Top Search Input Focus -> Direct redirect to Search Page
  el.topSearchInput.addEventListener("focus", () => {
    switchView("viewSearch");
    if (el.pageSearchInput) el.pageSearchInput.focus();
  });

  el.btnTopSearchSubmit.addEventListener("click", () => {
    const q = el.topSearchInput.value.trim();
    performSearch(q || "Trending");
  });

  el.topSearchInput.addEventListener("keyup", (e) => {
    if (e.key === "Enter") {
      const q = el.topSearchInput.value.trim();
      performSearch(q || "Trending");
    }
  });

  if (el.btnPageSearchSubmit) {
    el.btnPageSearchSubmit.addEventListener("click", () => {
      const q = el.pageSearchInput.value.trim();
      if (q) performSearch(q);
    });
  }

  el.categoryNavContainer.querySelectorAll(".cat-pill").forEach(pill => {
    pill.addEventListener("click", () => {
      el.categoryNavContainer.querySelectorAll(".cat-pill").forEach(p => p.classList.remove("active"));
      pill.classList.add("active");
      const cat = pill.dataset.cat;
      showToast(`Kategori: ${pill.textContent}`);
      if (cat === "movie") renderPosterCards(el.recommendedScrollList, shuffleArray(state.popularMovies));
      else if (cat === "series" || cat === "tv") renderPosterCards(el.recommendedScrollList, shuffleArray(state.popularTV));
      else renderPosterCards(el.recommendedScrollList, shuffleArray(state.trending));
    });
  });

  el.categoryCardsGrid.querySelectorAll(".cat-card").forEach(card => {
    card.addEventListener("click", () => {
      const genre = card.dataset.genre;
      showToast(`Membuka Kategori ${genre.toUpperCase()}`);
      performSearch(genre === "kdrama" ? "Korean" : genre === "indo" ? "Indonesia" : "Action");
    });
  });

  el.rankingTabs.querySelectorAll(".rank-tab").forEach(tab => {
    tab.addEventListener("click", () => {
      el.rankingTabs.querySelectorAll(".rank-tab").forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const filter = tab.dataset.rank;
      showToast(`Filter Ranking: ${tab.textContent}`);
      renderRankingCards(state.trending, filter);
    });
  });

  if (el.btnClearHistory) {
    el.btnClearHistory.addEventListener("click", () => {
      state.searchHistory = [];
      localStorage.setItem("suikamovie_search_history", JSON.stringify([]));
      renderSearchHistory();
      showToast("Riwayat pencarian dihapus.");
    });
  }

  document.querySelectorAll("#trendingSearchChips .chip-item").forEach(chip => {
    chip.addEventListener("click", () => {
      const q = chip.dataset.query;
      performSearch(q);
    });
  });

  el.btnDetailBack.addEventListener("click", closeDetailModal);
  el.btnDetailPlayCover.addEventListener("click", startInlinePlayer);

  el.btnAddToList.addEventListener("click", () => {
    if (!state.currentDetail) return;
    const isFav = toggleFavorite(state.currentDetail);
    showToast(isFav ? "Ditambahkan ke Daftar Saya" : "Dihapus dari Daftar Saya");
  });

  el.btnPostComment.addEventListener("click", () => {
    const text = el.commentInput.value.trim();
    if (!text) return;
    const newComment = document.createElement("div");
    newComment.className = "comment-item";
    newComment.innerHTML = `
      <div class="comment-content">
        <div class="comment-author">Anda <span class="comment-time">• Baru saja</span></div>
        <p class="comment-text">${escapeHtml(text)}</p>
      </div>
    `;
    el.commentsList.prepend(newComment);
    el.commentInput.value = "";
    showToast("Komentar terposting!");
  });

  // Server selection (SERVER 1, SERVER 2...)
  el.sourcePillsWrap.querySelectorAll(".source-pill").forEach(pill => {
    pill.addEventListener("click", () => {
      el.sourcePillsWrap.querySelectorAll(".source-pill").forEach(p => p.classList.remove("active"));
      pill.classList.add("active");
      state.activeServer = pill.dataset.server;
      saveActivePlayerSession();
      showToast(`Server diganti ke ${pill.textContent}`);
      if (!el.detailInlinePlayer.classList.contains("hidden")) {
        startInlinePlayer(false);
      }
    });
  });
}

/* VIEW SWITCHING & DEVICE SPECIFICATION LOADING */
function switchView(viewId) {
  state.activeView = viewId;
  el.views.forEach(v => v.classList.remove("active"));
  const target = document.getElementById(viewId);
  if (target) target.classList.add("active");

  el.navTabBtns.forEach(btn => {
    btn.classList.toggle("active", btn.dataset.view === viewId);
  });

  if (viewId === "viewAkun") {
    renderAccountPage();
  }
}

/* RENDER PROFIL AKUN (dipanggil dari auth.js tiap onAuthStateChanged & tiap buka tab Akun) */
function renderAccountPage() {
  const user = window.SuikaAuth?.getCurrentUser?.();
  if (!user) return;

  const profile = window.SuikaAuth?.getProfile?.();
  const displayName = user.displayName || user.email?.split("@")[0] || "Pengguna SuikaMovie";
  const initial = (displayName || "S").trim().charAt(0).toUpperCase();
  const photoURL = user.photoURL || profile?.photoURL;

  el.accountName.textContent = displayName;
  el.accountEmail.textContent = user.email || "-";

  if (photoURL) {
    el.accountAvatar.style.backgroundImage = `url(${photoURL})`;
    el.accountAvatar.textContent = "";
  } else {
    el.accountAvatar.style.backgroundImage = "";
    el.accountAvatar.textContent = initial;
  }

  const isGoogleOnly = user.providerData.length > 0 &&
    user.providerData.every(p => p.providerId === "google.com");

  el.accountProviderBadge.textContent = isGoogleOnly ? "Google" : "Email";
  el.changePasswordForm.closest(".account-card").classList.toggle("hidden", isGoogleOnly);
  el.googleOnlyNotice.classList.toggle("hidden", !isGoogleOnly);

  // ===== LEVEL, EXP, & TITLE =====
  const info = window.SuikaAuth?.getLevelInfo?.(profile?.exp || 0);
  if (info) {
    el.accountLevelBadge.textContent = `Lv. ${info.level}`;
    el.accountLevelBadge.className = `account-level-badge ${info.theme}`;
    el.accountTitleBadge.textContent = info.title;
    el.accountTitleBadge.className = `account-title-badge ${info.theme}`;
    el.accountExpBarFill.style.width = `${info.progressPercent}%`;
    el.accountExpBarFill.className = `account-exp-bar-fill ${info.theme}`;
    el.accountExpLabel.textContent = `${info.expIntoLevel} / ${info.expNeededForLevel} EXP`;
    el.accountWatchCount.textContent = profile?.watchCount || 0;
  }

  // ===== ADMIN BADGE (panel-nya sendiri dikendalikan dari auth.js) =====
  el.accountAdminBadge.classList.toggle("hidden", !window.SuikaAuth?.isAdmin?.());
}
window.renderAccountPage = renderAccountPage;

/* HERO BANNER CAROUSEL */
function renderHeroBanner(index) {
  const item = state.heroItems[index];
  if (!item) return;

  state.heroIndex = index;
  el.heroBackdrop.style.backgroundImage = `url('${item.backdrop || item.poster}')`;
  el.heroTitle.textContent = item.title;
  el.heroRating.textContent = item.rating;
  el.heroYear.textContent = item.year;
  el.heroType.textContent = (item.type || 'movie').toUpperCase();
  el.heroOverview.textContent = item.overview || "Sinopsis tidak tersedia.";

  el.heroIndicators.innerHTML = state.heroItems
    .map((_, i) => `<div class="indicator-dot ${i === index ? "active" : ""}" data-index="${i}"></div>`)
    .join("");

  el.btnHeroPlay.onclick = () => openDetailModal(item.id, item.type, true);
  el.btnHeroDetail.onclick = () => openDetailModal(item.id, item.type, false);
}

function startHeroCarousel() {
  if (state.heroTimer) clearInterval(state.heroTimer);
  state.heroTimer = setInterval(() => {
    const nextIdx = (state.heroIndex + 1) % state.heroItems.length;
    renderHeroBanner(nextIdx);
  }, 5000);

  el.heroIndicators.addEventListener("click", (e) => {
    const dot = e.target.closest(".indicator-dot");
    if (dot) {
      const idx = parseInt(dot.dataset.index);
      renderHeroBanner(idx);
      startHeroCarousel();
    }
  });
}

/* RENDER POSTER CARDS WITH CIRCULAR BLUE PLAY BUTTON */
function renderPosterCards(container, items) {
  if (!container) return;
  if (!items || items.length === 0) {
    container.innerHTML = `<div class="empty-state"><p>Tidak ada konten.</p></div>`;
    return;
  }

  container.innerHTML = items.map(item => `
    <div class="media-poster-card" data-id="${item.id}" data-type="${item.type || 'movie'}">
      <div class="poster-box">
        <img class="poster-img" src="${item.poster}" alt="${escapeHtml(item.title)}" loading="lazy" />
        <span class="poster-rating">${item.rating}</span>
        <div class="play-blue-circle">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
        </div>
      </div>
      <div class="poster-card-body">
        <h4 class="poster-card-title">${escapeHtml(item.title)}</h4>
        <div class="poster-card-sub">
          <span>${item.year}</span>
          <span>• ${(item.type || 'movie').toUpperCase()}</span>
        </div>
      </div>
    </div>
  `).join("");

  container.querySelectorAll(".media-poster-card").forEach(card => {
    card.addEventListener("click", () => {
      openDetailModal(parseInt(card.dataset.id), card.dataset.type);
    });
  });
}

/* RENDER PERINGKAT FILM */
function renderRankingCards(items, filter = "all") {
  if (!el.rankingScrollList) return;
  let filtered = items;
  if (filter === "kdrama") filtered = items.filter(i => i.title.toLowerCase().includes("korea") || i.id % 2 === 0);
  else if (filter === "indonesia") filtered = items.filter(i => i.title.toLowerCase().includes("indo") || i.id % 3 === 0);

  el.rankingScrollList.innerHTML = filtered.slice(0, 10).map((item, idx) => `
    <div class="ranking-card" data-id="${item.id}" data-type="${item.type || 'movie'}">
      <span class="ranking-badge-num">${idx + 1}</span>
      <div class="poster-box">
        <img class="poster-img" src="${item.poster}" alt="${escapeHtml(item.title)}" loading="lazy" />
        <span class="poster-rating">${item.rating}</span>
        <div class="play-blue-circle">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
        </div>
      </div>
      <div class="poster-card-body">
        <h4 class="poster-card-title">${escapeHtml(item.title)}</h4>
        <div class="poster-card-sub">
          <span>Peringkat #${idx + 1}</span>
        </div>
      </div>
    </div>
  `).join("");

  el.rankingScrollList.querySelectorAll(".ranking-card").forEach(card => {
    card.addEventListener("click", () => {
      openDetailModal(parseInt(card.dataset.id), card.dataset.type);
    });
  });
}

/* SEARCH HISTORY CHIPS */
function renderSearchHistory() {
  if (!el.searchHistoryChips) return;
  el.searchHistoryChips.innerHTML = state.searchHistory.map(q => `
    <button class="chip-item" data-query="${escapeHtml(q)}">${escapeHtml(q)}</button>
  `).join("");

  el.searchHistoryChips.querySelectorAll(".chip-item").forEach(chip => {
    chip.addEventListener("click", () => performSearch(chip.dataset.query));
  });
}

async function performSearch(query) {
  if (!query) return;
  switchView("viewSearch");
  if (el.pageSearchInput) el.pageSearchInput.value = query;

  if (!state.searchHistory.includes(query)) {
    state.searchHistory.unshift(query);
    if (state.searchHistory.length > 8) state.searchHistory.pop();
    localStorage.setItem("suikamovie_search_history", JSON.stringify(state.searchHistory));
    renderSearchHistory();
  }

  showToast(`Mencari "${query}"...`);
  const results = await api.search(query);
  renderVerticalSearchResults(results);
}

/* VERTICAL SEARCH RESULTS LIST WITH RANKING BADGES */
function renderVerticalSearchResults(items) {
  if (!el.verticalSearchResultsList) return;
  if (!items || items.length === 0) {
    el.verticalSearchResultsList.innerHTML = `<div class="empty-state"><p>Tidak ada hasil ditemukan.</p></div>`;
    return;
  }

  el.verticalSearchResultsList.innerHTML = items.map((item, idx) => `
    <div class="search-result-item" data-id="${item.id}" data-type="${item.type || 'movie'}">
      <div class="result-poster">
        <span class="result-rank-badge">#${idx + 1}</span>
        <img src="${item.poster}" alt="${escapeHtml(item.title)}" />
      </div>
      <div class="result-info">
        <h4 class="result-title">${escapeHtml(item.title)}</h4>
        <div class="result-meta-row">
          <span class="badge-rating">${item.rating}</span>
          <span class="badge-meta">${item.year}</span>
          <span class="result-country">${item.country || 'US'}</span>
          <span class="badge-type">${(item.type || 'movie').toUpperCase()}</span>
        </div>
        <p class="result-overview">${escapeHtml(item.overview)}</p>
      </div>
    </div>
  `).join("");

  el.verticalSearchResultsList.querySelectorAll(".search-result-item").forEach(card => {
    card.addEventListener("click", () => {
      openDetailModal(parseInt(card.dataset.id), card.dataset.type);
    });
  });
}

/* SAVE & PERSIST ACTIVE PLAYER SESSION STATE */
function saveActivePlayerSession() {
  if (!state.currentDetail) return;
  const sessionData = {
    id: state.currentDetail.id,
    type: state.currentDetail.type,
    autoPlay: !el.detailInlinePlayer.classList.contains("hidden"),
    server: state.activeServer,
    season: state.activeSeason,
    episode: state.activeEpisode
  };
  sessionStorage.setItem("suikamovie_active_detail", JSON.stringify(sessionData));
  history.replaceState({ modal: "detail", id: state.currentDetail.id }, "", `#detail-${state.currentDetail.id}`);
}

/* MOVIE DETAIL MODAL & INLINE STREAM PLAYER */
async function openDetailModal(id, type = "movie", autoPlay = false) {
  showToast("Memuat detail film...");
  const detail = await api.detail(id, type);
  state.currentDetail = detail;

  el.detailBackdropImg.style.backgroundImage = `url('${detail.backdrop || detail.poster}')`;
  el.detailTitle.textContent = detail.title;
  el.detailRating.textContent = detail.rating;
  el.detailYear.textContent = detail.year;
  el.detailCountry.textContent = detail.country;
  el.detailRuntime.textContent = detail.runtime;
  el.detailTypeBadge.textContent = detail.type === "tv" ? "TV SERIES" : "FILM";
  el.detailOverviewText.textContent = detail.overview;

  el.detailGenresList.innerHTML = detail.genres.map(g => `<span class="genre-pill">${g}</span>`).join("");

  // RESET & STOP PLAYER IF ACTIVE
  el.detailInlinePlayer.classList.add("hidden");
  el.detailInlinePlayer.src = "about:blank";
  el.btnDetailPlayCover.classList.remove("hidden");

  // SEASON CONTROLS ONLY FOR TV SHOWS (STRICTLY HIDDEN FOR MOVIES)
  if (type === "tv") {
    el.detailTvControls.classList.remove("hidden");
    await setupTvEpisodeControls(detail);
  } else {
    el.detailTvControls.classList.add("hidden");
    el.detailSeasonSelect.innerHTML = "";
    el.detailEpisodeList.innerHTML = "";
  }

  renderPosterCards(el.detailRecommendationsList, shuffleArray(state.trending).slice(0, 6));

  el.detailModal.classList.remove("hidden");
  document.body.style.overflow = "hidden";

  saveActivePlayerSession();

  if (autoPlay) {
    startInlinePlayer();
  }
}

/* COMPLETE TEARDOWN AND STOPPING OF PLAYER */
function closeDetailModal() {
  sessionStorage.removeItem("suikamovie_active_detail");
  if (window.location.hash.startsWith("#detail-")) {
    history.pushState("", document.title, window.location.pathname + window.location.search);
  }

  // STOP PLAYER IMMEDIATELY
  el.detailInlinePlayer.src = "about:blank";
  el.detailInlinePlayer.classList.add("hidden");
  el.btnDetailPlayCover.classList.remove("hidden");

  el.detailModal.classList.add("hidden");
  document.body.style.overflow = "";
  state.currentDetail = null;
}

function startInlinePlayer(awardExp = true) {
  if (!state.currentDetail) return;
  const { id, type, title } = state.currentDetail;
  const serverInfo = SERVERS[state.activeServer] || SERVERS.vidlink || SERVERS.vidsrc;
  const streamUrl = type === "movie" 
    ? serverInfo.movie.replace("{id}", id) 
    : serverInfo.tv.replace("{id}", id).replace("{s}", state.activeSeason).replace("{e}", state.activeEpisode);

  el.detailInlinePlayer.src = streamUrl;
  el.detailInlinePlayer.classList.remove("hidden");
  el.btnDetailPlayCover.classList.add("hidden");
  saveActivePlayerSession();
  showToast(`Memutar: ${title} (${serverInfo.name})`);

  // Kasih EXP tiap kali user mulai nonton film/episode BARU
  // (bukan sekadar ganti server buat film/episode yang sama).
  if (awardExp) {
    window.SuikaAuth?.awardWatchExp?.();
  }
}

/**
 * Player streaming (iframe server pihak ketiga) punya tombol fullscreen
 * bawaannya sendiri. Kita nggak bisa nyuntik JS ke dalam iframe-nya (beda
 * origin), tapi event fullscreenchange di document tetap kebaca sama parent
 * page walau elemen yang full-screen-nya cross-origin. Jadi begitu user
 * pencet tombol fullscreen di dalam player, kita deteksi lewat event itu
 * lalu paksa rotasi ke landscape otomatis biar enak nontonnya. Waktu keluar
 * dari fullscreen, orientasi dibalikin bebas (unlock) lagi.
 *
 * Fitur ini bisa dimatiin user lewat toggle "Auto Rotate Layar" di halaman
 * Akun > Preferensi (state.autoRotateEnabled, disimpan di localStorage).
 */
function setupFullscreenOrientationLock() {
  const handleFullscreenChange = () => {
    const fsEl = document.fullscreenElement || document.webkitFullscreenElement;
    const isPlayerFullscreen = fsEl && (fsEl === el.detailInlinePlayer || fsEl.contains?.(el.detailInlinePlayer));

    if (isPlayerFullscreen && state.autoRotateEnabled && screen.orientation && screen.orientation.lock) {
      screen.orientation.lock("landscape").catch(() => {
        /* sebagian browser/WebView nolak lock kalau bukan trusted user gesture; aman diabaikan */
      });
    } else if (!fsEl && screen.orientation && screen.orientation.unlock) {
      screen.orientation.unlock();
    }
  };

  document.addEventListener("fullscreenchange", handleFullscreenChange);
  document.addEventListener("webkitfullscreenchange", handleFullscreenChange);
}

async function setupTvEpisodeControls(detail) {
  let seasonsDetail = detail.seasonsDetail || [];
  
  if (seasonsDetail.length === 0) {
    const totalSeasons = detail.seasons || 1;
    seasonsDetail = Array.from({ length: totalSeasons }, (_, i) => ({
      seasonNumber: i + 1,
      name: `Season ${i + 1}`,
      episodeCount: 10
    }));
  }

  el.detailSeasonSelect.innerHTML = seasonsDetail
    .map(s => `<option value="${s.seasonNumber}">${s.name || 'Season ' + s.seasonNumber} (${s.episodeCount} Eps)</option>`)
    .join("");

  const availableSeasons = seasonsDetail.map(s => s.seasonNumber);
  if (!state.activeSeason || !availableSeasons.includes(state.activeSeason)) {
    state.activeSeason = availableSeasons[0] || 1;
  }
  el.detailSeasonSelect.value = state.activeSeason;

  await updateSeasonEpisodes(detail.id, state.activeSeason, seasonsDetail);

  el.detailSeasonSelect.onchange = async (e) => {
    state.activeSeason = parseInt(e.target.value);
    state.activeEpisode = 1;
    saveActivePlayerSession();
    await updateSeasonEpisodes(detail.id, state.activeSeason, seasonsDetail);
    if (!el.detailInlinePlayer.classList.contains("hidden")) {
      startInlinePlayer();
    }
  };
}

async function updateSeasonEpisodes(tvId, seasonNum, seasonsDetail) {
  let count = 10;
  const currentSeasonObj = seasonsDetail.find(s => s.seasonNumber === seasonNum);
  if (currentSeasonObj && currentSeasonObj.episodeCount) {
    count = currentSeasonObj.episodeCount;
  }

  try {
    const seasonData = await api.getTvSeasonDetails(tvId, seasonNum);
    if (seasonData && seasonData.episodes && seasonData.episodes.length > 0) {
      count = seasonData.episodes.length;
    }
  } catch (err) {
    console.warn("Could not fetch season episode details from TMDB:", err);
  }

  renderTvEpisodes(count);
}

/* LARGE TOUCH EPISODE BUTTONS */
function renderTvEpisodes(count) {
  if (state.activeEpisode > count) {
    state.activeEpisode = 1;
  }

  el.detailEpisodeList.innerHTML = Array.from({ length: count }, (_, i) => i + 1)
    .map(ep => `<button class="ep-btn ${ep === state.activeEpisode ? 'active' : ''}" data-ep="${ep}">Eps ${ep}</button>`)
    .join("");

  el.detailEpisodeList.querySelectorAll(".ep-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      state.activeEpisode = parseInt(btn.dataset.ep);
      el.detailEpisodeList.querySelectorAll(".ep-btn").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      saveActivePlayerSession();
      showToast(`Memutar Season ${state.activeSeason} Episode ${state.activeEpisode}`);
      if (!el.detailInlinePlayer.classList.contains("hidden")) {
        startInlinePlayer();
      }
    });
  });
}

function toggleFavorite(item) {
  const idx = state.favorites.findIndex(f => f.id === item.id);
  if (idx >= 0) {
    state.favorites.splice(idx, 1);
    localStorage.setItem("suikamovie_favorites", JSON.stringify(state.favorites));
    return false;
  } else {
    state.favorites.push(item);
    localStorage.setItem("suikamovie_favorites", JSON.stringify(state.favorites));
    return true;
  }
}

function showToast(msg) {
  el.toastMessage.textContent = msg;
  el.toast.classList.remove("hidden");
  setTimeout(() => {
    el.toast.classList.add("hidden");
  }, 2500);
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
