import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})


api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const config = error.config || {}
      const isMediaRequest = config.responseType === 'blob'
      if (!isMediaRequest) {
        const auth = useAuthStore()
        auth.logout()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// ── Klijenti za nezavisne mikroservise (search, requestService) ────────
function attachInterceptors(instance) {
  instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  })
  instance.interceptors.response.use(
    response => response,
    error => Promise.reject(error)
  )
  return instance
}

// Elastic/OCR search servis (videti docker-compose: search -> port 8081)
const searchApi_ = attachInterceptors(axios.create({
  baseURL: '/searchapi/api',
  headers: { 'Content-Type': 'application/json' }
}))

// Servis za medjubibliotecke zahteve (videti docker-compose: request-service -> port 8086)
const requestApi_ = attachInterceptors(axios.create({
  baseURL: '/requestapi/api',
  headers: { 'Content-Type': 'application/json' }
}))

export const authApi = {
  login:          (data)         => api.post('/auth/login', data),
  registerStep1:  (data)         => api.post('/auth/register/step1', data),
  registerStep2:  (jmbg, data)   => api.post(`/auth/register/step2/${jmbg}`, data),
  registerStep3:  (jmbg, data)   => api.post(`/auth/register/step3/${jmbg}`, data),
  saveGenres:     (jmbg, data)   => api.post(`/auth/register/genres/${jmbg}`, data),
  renewMembership: (jmbg, nacinUplate, tipPretplate) =>
    api.post(`/auth/renew/${jmbg}`, null, {
      params: { nacinUplate, tipPretplate }
    })


}
export const publicApi = {
  getLibraries:  () => api.get('/biblioteka'),
  getGenres:     () => api.get('/genres'),
  getKategorije: () => api.get('/kategorije'),
}
export const katalogApi = {
  svi: () => api.get('/katalog/all'),
}
export const userApi = {
  getMe:             ()           => api.get('/users/me'),
  getProfile:        (jmbg)       => api.get(`/users/${jmbg}/profile`),
  updateProfile:     (jmbg, data) => api.put(`/users/${jmbg}/profile`, data),
  updateGenres:      (jmbg, data) => api.put(`/users/${jmbg}/genres`, data),
}

// ── Dobavljači ────────────────────────────────────────────────────────
export const dobavljacApi = {
  kreiraj:       (data)      => api.post('/dobavljaci/unos', data),
  svi:           ()          => api.get('/dobavljaci/prikaz-svih'),
  jedan:         (id)        => api.get(`/dobavljaci/detaljan-prikaz/${id}`),
  izmeni:        (id, data)  => api.patch(`/dobavljaci/izmena/${id}`, data),
  obrisi:        (id)        => api.patch(`/dobavljaci/brisanje/${id}`),
}

// ── Ugovori ───────────────────────────────────────────────────────────
export const ugovorApi = {
  kreiraj:       (data)      => api.post('/ugovori/kreiranje', data),
  sviZaDobavljaca: (id)      => api.get(`/ugovori/ispisi-sve/${id}`),
  raskini:       (id)        => api.patch(`/ugovori/raskid/${id}`),
}

// ── Knjige ───────────────────────────────────────────────────────────
export const knjigaApi = {
  sveOsnovno:      () => api.get('/knjiga/sve/osnovno'),
  pretraga:        (q) => api.get('/knjiga/pretraga', { params: { q } }),
  detalji:         (isbn) => api.get(`/knjiga/detalji/${isbn}`),
  dodajKompletna:  (formData) =>
    api.post('/knjiga/nova/kompletna', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  naslovna:        (isbn) => api.get(`/knjiga/naslovna/${isbn}`, { responseType: 'blob' }),
  pdf:             (isbn) => api.get(`/knjiga/eknjiga/${isbn}/pdf`, { responseType: 'blob' }),
  audio:           (isbn) => api.get(`/knjiga/audioknjiga/${isbn}/audio`, { responseType: 'blob' }),
  citanjeProgress: (isbn) => api.get(`/knjiga/eknjiga/${isbn}/progress`),
  sacuvajCitanje:  (isbn, data) => api.put(`/knjiga/eknjiga/${isbn}/progress`, data),
  slusanjeProgress: (isbn) => api.get(`/knjiga/audioknjiga/${isbn}/progress`),
  sacuvajSlusanje:  (isbn, data) => api.put(`/knjiga/audioknjiga/${isbn}/progress`, data),
  obrisi:          (isbn) => api.put(`/knjiga/delete/${isbn}`),
  obrisiEknjigu:   (isbn) => api.put(`/knjiga/${isbn}/brisanjeeknjige`),
  obrisiAudio:     (isbn) => api.put(`/knjiga/${isbn}/brisanjeaudioknjige`),
  azurirajKompletna: (isbn, formData) =>
    api.put(`/knjiga/${isbn}/kompletna`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  preporucene:      ()             => api.get('/knjiga/preporucene'),
}

// ── Elektronske baze podataka ────────────────────────────────────────
export const bazePodatakaApi = {
  sveOsnovno: () => api.get('/baze-podataka/sve/osnovno'),
  pretraga: (q) => api.get('/baze-podataka/pretraga', { params: { q } }),
  detalji: (id) => api.get(`/baze-podataka/detalji/${id}`),
  preuzmi: (id) => api.get(`/baze-podataka/${id}/preuzmi`, { responseType: 'blob' }),
  kreiraj: (formData) =>
    api.post('/baze-podataka/nova', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  azuriraj: (id, formData) =>
    api.put(`/baze-podataka/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  obrisi: (id) => api.delete(`/baze-podataka/${id}`),
}

// ── Izdavaci ─────────────────────────────────────────────────────────
export const izdavaciApi = {
  ispisiSve: () => api.get('/izdavaci/sve')
}
export const pozajmicaApi = {
  pozajmiFizicku:    (isbn)      => api.post(`/pozajmice/pozajmi/${isbn}`),
  pozajmiDigitalno:  (isbn, tip) => api.post(`/pozajmice/pozajmi-digitalno/${isbn}`, null, { params: { tip } }),
  mozePozajmiti:     ()          => api.get('/pozajmice/mozePozajmiti'),
  rezervisi:         (isbn)      => api.post(`/pozajmice/rezervisi/${isbn}`),
  getMoje:           ()          => api.get('/pozajmice/moje'),
  sveAktivne:         ()      => api.get('/pozajmice/sve-aktivne'),
  vratiKnjigu:        (idP)   => api.post(`/pozajmice/vrati/${idP}`),
  produzenje:        (idP)       => api.post(`/pozajmice/produzenje/${idP}`),
  produzenjaNaCekanju:    ()                    => api.get('/pozajmice/produzenja/na-cekanju'),
  obradiProduzenje:       (idPP, approve, razlog) => api.post(`/pozajmice/produzenja/${idPP}/obradi`, null, {
      params: { approve, razlog: razlog || '' }
  }),
  izgubljena:        (idP)       => api.post(`/pozajmice/izgubljena/${idP}`),
  izRezervacije:     (idR)       => api.post(`/pozajmice/iz-rezervacije/${idR}`),
  getDostupno:       (isbn)      => api.get(`/pozajmice/dostupno/${isbn}`),
  imamPozajmicu:     (isbn)      => api.get(`/pozajmice/imam-pozajmicu/${isbn}`),
  getObavestenja:    ()          => api.get('/pozajmice/obavestenja'),
  markRead:          (idO)       => api.put(`/pozajmice/obavestenja/${idO}/procitano`),
  deleteObavestenje: (idO)       => api.delete(`/pozajmice/obavestenja/${idO}`),
}

// ── Predlozi za nabavku ──────────────────────────────────────────────
export const predlogApi = {
  kreiraj:          (data) => api.post('/predlozi/kreiraj', data),
  mojiPredlozi:     ()     => api.get('/predlozi/moji-zahtevi'),
  predloziNaCekanju: ()    => api.get('/predlozi/na-cekanju'),
  odobreniPredlozi: ()     => api.get('/predlozi/odobreni'),
  obradiPredlog:    (id, data) => api.patch(`/predlozi/obradi/${id}`, data),
  zaNarudzbinu: ()         =>  api.get("/predlozi/za-narudzbinu"),
  obradiPredlogMenadzer(id, odobren) {
    return api.put(`/predlozi/${id}/obrada-menadzer`, {
        odobren
    })
  }
}

// ── Notifikacije ─────────────────────── ───────────────────────────
export const notifikacijaApi = {
  mojeNotifikacije:    ()    => api.get('/notifikacije/moje'),
  oznаciKaoProcitanu:  (id)  => api.patch(`/notifikacije/procitana/${id}`),
  brojNeprocitanih:    ()    => api.get('/notifikacije/broj-neprocitanih'),
}

// ── MARC ─────────────────────────────────────────────────────────────
export const marcApi = {
  zapis: (isbn) => api.get(`/marc/${isbn}`),
}

// ── Autokatalogizacija (BIBLIOTEKAR) ────────────────────────────────────
export const autokatalogApi = {
  katalogizuj: (data) => api.post('/knjiga/autokatalog', data),
}
export const kaznaApi = {
  moje:  ()                          => api.get('/kazne/moje'),
  plati: (idK, nacinPlacanja)        => api.post(`/kazne/${idK}/plati`, null, { params: { nacinPlacanja } }),
}


// ── Pretraga / Elastic (OCR + fulltext) ─────────────────────────────────
export const searchApi = {
  poIsbn:        (isbn) => searchApi_.get(`/books/by-isbn/${isbn}`),
  poRecordId:    (recordId) => searchApi_.get(`/books/${recordId}`),
  fulltext:      (query) => searchApi_.get('/books/fulltext-search', { params: { query } }),
  pokreniOcr:    (recordId, file, force = false) => {
    const formData = new FormData()
    formData.append('file', file)
    return searchApi_.post(`/books/${recordId}/ocr`, formData, {
      params: { force },
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
}

// ── Medjubibliotecke pozajmice (ILL requests) ───────────────────────────
export const requestApi = {
  incoming:  (libraryId) => requestApi_.get(`/requests/incoming/${libraryId}`),
  outgoing:  (libraryId) => requestApi_.get(`/requests/outgoing/${libraryId}`),
  jedan:     (id) => requestApi_.get(`/requests/${id}`),
  kreiraj:   (data) => requestApi_.post('/requests', data),
  azuriraj:  (id, data) => requestApi_.put(`/requests/${id}`, data),
  otkazi:    (id) => requestApi_.post(`/requests/${id}/cancel`),
}
// ── Sistemske preporuke ───────────────────────────────────────────────
export const sistemskePreporukeApi = {
  pokreniAnalizu:  ()          => api.post('/sistemske-preporuke/pokreni-analizu'),
  getAktivne:      ()          => api.get('/sistemske-preporuke/aktivne'),
  azurirajStatus:  (id, status, body)    => api.patch(`/sistemske-preporuke/${id}/status`, body, { params: { status }}),
  zaNarudzbinu: ()               => api.get("/sistemske-preporuke/za-narudzbinu")
}

// ── AI Asistent: čet sesije ──────────────────────────────────────────
// Napomena: POST /cet-sesija/nova očekuje multipart/form-data sa jednim
// poljem "podaci" čiji je sadržaj JSON string (vidi Postman kolekciju).
export const cetSesijaApi = {
  sve:      ()                          => api.get('/cet-sesija/sve'),
  jedna:    (id)                        => api.get(`/cet-sesija/${id}`),
  nova: (tipAgentaCS, sadrzajPoruke, slika) => {
    const formData = new FormData()
    const podaciBlob = new Blob(
      [JSON.stringify({ tipAgentaCS, sadrzajPoruke })],
      { type: 'application/json' }
    )
    formData.append('podaci', podaciBlob)
    if (slika) {
      formData.append('slika', slika)
    }
    return api.post('/cet-sesija/nova', formData, {
      headers: { 'Content-Type': undefined }
    })
  },
  obrisi:   (id)                        => api.delete(`/cet-sesija/${id}`),
  arhiviraj: (id) => api.patch(`/cet-sesija/${id}/arhiviraj`),
  vrati:     (id) => api.patch(`/cet-sesija/${id}/vrati`),
}

// ── AI Asistent: čet poruke ───────────────────────────────────────────
// Poruke se šalju kao multipart/form-data sa poljem "podaci" (JSON string) i opcionim poljem "slika" (fajl, samo agent za knjige).
export const cetPorukaApi = {
  nova: (idCetSesije, sadrzajPoruke, slika) => {
    const formData = new FormData()
    const podaciBlob = new Blob(
      [JSON.stringify({ sadrzajPoruke })],
      { type: 'application/json' }
    )
    formData.append('podaci', podaciBlob)
    if (slika) {
      formData.append('slika', slika)
    }
    return api.post(`/cet-poruka/cet-sesija/${idCetSesije}`, formData, {
      headers: { 'Content-Type': undefined }
    })
  },
  ocena:       (idCetPoruke)                => api.get(`/cet-poruka/${idCetPoruke}/ocena`),
  oceni:       (idCetPoruke, ocenaCP, komentarCP) =>
    api.post(`/cet-poruka/${idCetPoruke}/ocena`, { ocenaCP, komentarCP }),
  // ukloniSliku: eksplicitan signal da se postojeća slika poruke obriše.
  // slika: nova slika koja zamenjuje postojeću.
  // Ako ni slika ni ukloniSliku nisu prisutni, postojeća slika ostaje.
  azuriraj: (idCetPoruke, sadrzajPoruke, slika, ukloniSliku = false) => {
    const formData = new FormData()
    const podaciBlob = new Blob(
      [JSON.stringify({ sadrzajPoruke, ukloniSliku })],
      { type: 'application/json' }
    )
    formData.append('podaci', podaciBlob)
    if (slika) {
      formData.append('slika', slika)
    }
    return api.put(`/cet-poruka/${idCetPoruke}`, formData, {
      headers: { 'Content-Type': undefined }
    })
  },
}

// ── AI Asistent: health check (poseban servis, port 8000) ────────────
const CHAT_HEALTH_URL = 'http://localhost:8000/api/v1/chat/health'
export const chatHealthApi = {
  // Zaseban axios pozив bez baseURL/interceptora jer servis radi na
  // drugom portu (8000) i nije iza istog /api proxy-ja kao Spring backend.
  provera: () => axios.get(CHAT_HEALTH_URL),
}

// ── Budžet ────────────────────────────────────────────────────────────
export const budzetApi = {
  getSviBudzeti:  ()      => api.get('/budzet/sve-po-zanrovima'),
  postaviBudzet:  (data)  => api.post('/budzet/postavi', data),
  prerasporedi:   (data)  => api.post('/budzet/prerasporedi', data),
}

// ── Narudžbine ────────────────────────────────────────────────────────
export const narudzbinApi = {
  kreiraj:        (data)              => api.post('/narudzbine/kreiraj', data),
  getSve:         ()                  => api.get('/narudzbine/sve'),
  getJedna:       (id)      => api.get(`/narudzbine/${id}`),
  dodajStavku:    (id, data)          => api.post(`/narudzbine/${id}/stavke`, data),
  ukloniStavku:   (nId, sId)          => api.delete(`/narudzbine/${nId}/stavke/${sId}`),
  potvrdi:        (id)                => api.patch(`/narudzbine/${id}/potvrdi`),
  evidentirajIsporuku: (id, data)     => api.patch(`/narudzbine/${id}/isporuka`, data),
}

// -- Reklamacije ------------------------------------------------------------
export const reklamacijaApi = {
  kreiraj:   (narudzbinId, data) => api.post(`/reklamacije/narudzbina/${narudzbinId}`, data),
  getSve:    ()                  => api.get('/reklamacije/sve'),
  zatvori:   (id, data)          => api.patch(`/reklamacije/${id}/zatvori`, data),
}


//izvestaji
export const izvestajApi = {
  generiši: (od, datDo) =>
    api.get('/izvestaj/aktivnosti', {
      params: { od, do: datDo },
      responseType: 'blob'
    }),
  generisiFond: (od, datDo) =>
    api.get('/izvestaj/fond', {
      params: { od, do: datDo },
      responseType: 'blob'
    }),
  nabavka: (od, datDo) => api.get('/izvestaj/nabavka', { params: { od, do: datDo }, responseType: 'blob' }),
  generisiAIAgentIzvestaj: (od, datDo) => api.get('/izvestaj/ai-asistent', { params: { datumOd: od, datumDo: datDo}, responseType: 'blob' }),
}

// ── Komentari ─────────────────────────────────────────────────────────
export const komentarApi = {
  dohvati: (isbn)                  => api.get(`/knjiga/${isbn}/komentari`),
  dodaj:   (isbn, data)            => api.post(`/knjiga/${isbn}/komentari`, data),
  lajkuj:  (isbn, komentarId)      => api.post(`/knjiga/${isbn}/komentari/${komentarId}/lajk`),
}

export default api
