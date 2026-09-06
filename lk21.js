/*
 * Created by : febry.is-a.dev
 * GitHub     : vandebry10-star
 * Date       : 19-07-2026
 * * Do not remove the creator's watermark, please respect the creator.
 */
import axios from "axios";

// ubah menjadi a p i_k e y
const TMDB_AKI_PEY = "82524e2faef91706a2d52d52496130ac";

const TMDB_BASE = "https://api.themoviedb.org/3";
const TMDB_IMG = "https://image.tmdb.org/t/p/w500";
const SERVERS = {
  vidsrc: { movie: "https://vidsrc.me/embed/movie?tmdb={id}", tv: "https://vidsrc.me/embed/
tv?tmdb={id}&season={s}&episode={e}" },
  embedsu: { movie: "https://embed.su/embed/movie/{id}", tv: "https://embed.su/embed/tv/{id
}/{s}/{e}" },
  vidsrcpro: { movie: "https://vidsrc.pro/embed/movie/{id}", tv: "https://vidsrc.pro/embed/
tv/{id}/{s}/{e}" },
};
/**
 * LK21 Scraper - Nonton Film & TV Series Gratis Sub Indo
 * Menggunakan TMDB API untuk data + embed streaming
 */
class LK21 {
  constructor() {}
  async _tmdb(endpoint, params = {}) {
    try {
      const { data } = await axios.get(`${TMDB_BASE}${endpoint}`, {
        params: { api_key: TMDB_API_KEY, language: "id-ID", ...params },
      });
      return data;
    } catch (e) {
      console.error(`TMDB error: ${e.message}`);
      return null;
    }
  }
  async search(query) {
    const data = await this._tmdb("/search/multi", { query, page: 1 });
    if (!data?.results) return [];
    return data.results
      .filter((r) => r.media_type === "movie" || r.media_type === "tv")
      .map((r) => {
        const title = r.title || r.name;
        const year = (r.release_date || r.first_air_date || "").split("-")[0];
        return {
          id: r.id,
          title,
          year,
          type: r.media_type,
          rating: r.vote_average?.toFixed(1) || "N/A",
          poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : null,
          overview: r.overview || "",
        };
      });
  }
  async detail(id, type = "movie") {
    const data = await this._tmdb(`/${type}/${id}`);
    if (!data) return null;
    const title = data.title || data.name;
    const releaseDate = data.release_date || data.first_air_date;
    const year = releaseDate ? releaseDate.split("-")[0] : "N/A";
    const runtime = type === "movie" ? data.runtime : data.episode_run_time?.[0];
    const genres = data.genres?.map((g) => g.name) || [];
    const credits = await this._tmdb(`/${type}/${id}/credits`);
    const cast = credits?.cast?.slice(0, 10).map((a) => ({
      name: a.name,
      character: a.character,
      photo: a.profile_path ? `${TMDB_IMG}${a.profile_path}` : null,
    })) || [];
    const servers = {};
    for (const [name, tmpl] of Object.entries(SERVERS)) {
      if (type === "movie") {
        servers[name] = tmpl.movie.replace("{id}", id);
      } else {
        servers[name] = tmpl.tv.replace("{id}", id).replace("{s}", 1).replace("{e}", 1);
      }
    }
    return {
      id, title, type, year,
      rating: data.vote_average?.toFixed(1) || "N/A",
      runtime: runtime ? `${runtime} min` : "N/A",
      genres, overview: data.overview || "Sinopsis tidak tersedia.",
      poster: data.poster_path ? `${TMDB_IMG}${data.poster_path}` : null,
      backdrop: data.backdrop_path ? `${TMDB_IMG}${data.backdrop_path}` : null,
      seasons: type === "tv" ? data.number_of_seasons : undefined,
      episodes: type === "tv" ? data.number_of_episodes : undefined,
      cast, servers, streamUrl: servers.vidsrc,
    };
  }
  async streamingUrl(id, type = "movie", server = "vidsrc", season = 1, episode = 1) {
    const tmpl = SERVERS[server];
    if (!tmpl) return null;
    if (type === "movie") return tmpl.movie.replace("{id}", id);
    return tmpl.tv.replace("{id}", id).replace("{s}", season).replace("{e}", episode);
  }
  async trending() {
    const data = await this._tmdb("/trending/all/week");
    if (!data?.results) return [];
    return data.results
      .filter((r) => r.media_type === "movie" || r.media_type === "tv")
      .slice(0, 20)
      .map((r) => ({
        id: r.id,
        title: r.title || r.name,
        type: r.media_type,
        rating: r.vote_average?.toFixed(1) || "N/A",
        poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : null,
      }));
  }
  async recommendations(id, type = "movie") {
    const data = await this._tmdb(`/${type}/${id}/recommendations`);
    if (!data?.results) return [];
    return data.results
      .filter((r) => r.media_type === "movie" || r.media_type === "tv")
      .slice(0, 12)
      .map((r) => ({
        id: r.id,
        title: r.title || r.name,
        type: r.media_type,
        rating: r.vote_average?.toFixed(1) || "N/A",
        poster: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : null,
      }));
  }
}
/*
 * Created by : febry.is-a.dev
 * * Do not remove the watermark.
 */
export default LK21;
export async function handler(req, res) {
  const params = req.method === "GET" ? req.query : req.body;
  if (!params.action) return res.status(400).json({ error: 'Parameter "action" is required
(search|detail|stream|trending)' });
  const scraper = new LK21();
  try {
    let result;
    switch (params.action) {
      case "search":
        if (!params.q) return res.status(400).json({ error: 'Parameter "q" is required' });
        result = await scraper.search(params.q);
        break;
      case "detail":
        if (!params.id) return res.status(400).json({ error: 'Parameter "id" is required' }
);
        result = await scraper.detail(parseInt(params.id), params.type || "movie");
        break;
      case "stream":
        if (!params.id) return res.status(400).json({ error: 'Parameter "id" is required' }
);
        result = await scraper.streamingUrl(parseInt(params.id), params.type || "movie", pa
rams.server || "vidsrc", parseInt(params.season) || 1, parseInt(params.episode) || 1);
        break;
      case "trending":
        result = await scraper.trending();
        break;
      default:
        return res.status(400).json({ error: "Invalid action" });
    }
    return res.status(200).json({ status: true, result });
  } catch (error) {
    return res.status(500).json({ status: false, error: error.message });
  }
}
const scraper = new LK21();
const args = process.argv.slice(2);
const action = args[0];
async function main() {
  if (!action || action === "--help") {
    console.log(`
LK21 Scraper - Nonton Film Gratis Sub Indo
Usage:
  node lk21.js search <query>
  node lk21.js detail <id> [movie|tv]
  node lk21.js stream <id> [movie|tv] [vidsrc|embedsu|vidsrcpro] [season] [episode]
  node lk21.js trending
Examples:
  node lk21.js search avengers
  node lk21.js detail 299536 movie
  node lk21.js detail 1399 tv
  node lk21.js stream 299536 movie vidsrc
  node lk21.js stream 1399 tv vidsrcpro 1 1
  node lk21.js trending
`);
    return;
  }
  switch (action) {
    case "search": {
      const query = args.slice(1).join(" ");
      if (!query) return console.log("Usage: node lk21.js search <query>");
      console.log(`\nMencari: "${query}"...\n`);
      const results = await scraper.search(query);
      if (results.length === 0) {
        console.log("Tidak ada hasil ditemukan.");
      } else {
        results.forEach((r, i) => {
          console.log(`${i + 1}. [${r.type.toUpperCase()}] ${r.title} (${r.year}) - ⭐ ${r.r
ating}`);
          console.log(`   ID: ${r.id} | Poster: ${r.poster || "N/A"}`);
          if (r.overview) console.log(`   ${r.overview.substring(0, 100)}...`);
          console.log();
        });
      }
      break;
    }
    case "detail": {
      const id = parseInt(args[1]);
      const type = args[2] || "movie";
      if (!id) return console.log("Usage: node lk21.js detail <id> [movie|tv]");
      console.log(`\nMengambil detail ${type} ID ${id}...\n`);
      const d = await scraper.detail(id, type);
      if (!d) return console.log("Gagal mengambil data.");
      console.log(`Judul    : ${d.title}`);
      console.log(`Tahun    : ${d.year}`);
      console.log(`Rating   : ⭐  ${d.rating}`);
      console.log(`Durasi   : ${d.runtime}`);
      console.log(`Genre    : ${d.genres.join(", ")}`);
      console.log(`Poster   : ${d.poster}`);
      console.log(`Backdrop : ${d.backdrop}`);
      if (d.type === "tv") { console.log(`Seasons  : ${d.seasons}`); console.log(`Episodes
: ${d.episodes}`); }
      console.log(`Sinopsis : ${d.overview}`);
      console.log(`\n--- Pemeran ---`);
      d.cast.forEach((a) => console.log(`  ${a.name} sebagai ${a.character}`));
      console.log(`\n--- Server Streaming ---`);
      for (const [name, url] of Object.entries(d.servers)) { console.log(`  ${name}: ${url}
`); }
      console.log(`\nStream (default): ${d.streamUrl}`);
      break;
    }
    case "stream": {
      const id = parseInt(args[1]);
      const type = args[2] || "movie";
      const server = args[3] || "vidsrc";
      const season = parseInt(args[4]) || 1;
      const episode = parseInt(args[5]) || 1;
      if (!id) return console.log("Usage: node lk21.js stream <id> [movie|tv] [server] [sea
son] [episode]");
      const url = await scraper.streamingUrl(id, type, server, season, episode);
      if (!url) return console.log("Server tidak dikenal.");
      console.log(`\nStreaming URL (${server}):\n${url}\n`);
      break;
    }
    case "trending": {
      console.log("\nFilm Trending Minggu Ini:\n");
      const trending = await scraper.trending();
      if (trending.length === 0) return console.log("Gagal mengambil data.");
      trending.forEach((r, i) => {
        console.log(`${i + 1}. [${r.type.toUpperCase()}] ${r.title} - ⭐ ${r.rating}`);
        console.log(`   ID: ${r.id} | Poster: ${r.poster || "N/A"}\n`);
      });
      break;
    }
    default:
      console.log("Unknown command. Run: node lk21.js --help");
  }
}
main();
