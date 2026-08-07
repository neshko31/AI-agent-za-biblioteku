import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
const routes = [
  { path: '/',              redirect: '/login' },
  { path: '/login',         component: () => import('../views/LoginView.vue')     },
  { path: '/register',      component: () => import('../views/RegisterStep1.vue') },
  { path: '/register/step2',component: () => import('../views/RegisterStep2.vue') },
  { path: '/register/step3',component: () => import('../views/RegisterStep3.vue') },
  { path: '/register/genres',component: () => import('../views/FavouriteGenres.vue') },
  {
      path: '/home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
      path: '/dugovanja',
      component: () => import('../views/DugovanjaView.vue'),
      meta: { requiresAuth: true }
    },
  {
    path: '/knjige',
    component: () => import('../views/BooksView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vracanje-knjiga',
    component: () => import('../views/VracanjeKnjigaView.vue'),
    beforeEnter: () => {
      const auth = useAuthStore()
      return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
    }
  },
  {
    path: '/knjige/nova',
    component: () => import('../views/BookCreateView.vue'),
    beforeEnter: () => {
      const auth = useAuthStore()
      return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
    }
  },
  {
      path: '/produzenja-na-cekanju',
      component: () => import('../views/ProduzenjaNaCekanjuView.vue'),
      beforeEnter: () => {
          const auth = useAuthStore()
          return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
      }
  },
  {
    path: '/knjige/:isbn',
    component: () => import('../views/BookDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knjige/:isbn/citaj',
    component: () => import('../views/BookReadView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knjige/:isbn/slusaj',
    component: () => import('../views/BookListenView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/pozajmice',
    component: () => import('../views/PozajmiceView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/obavestenja',
    component: () => import('../views/ObavestenjaView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/baze-podataka',
    component: () => import('../views/ElektronskeBazeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/baze-podataka/nova',
    component: () => import('../views/ElektronskaBazaCreateView.vue'),
    beforeEnter: () => {
      const auth = useAuthStore()
      return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
    }
  },
  {
    path: '/baze-podataka/:id/izmena',
    component: () => import('../views/ElektronskaBazaEditView.vue'),
    beforeEnter: () => {
      const auth = useAuthStore()
      return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
    }
  },
    { path: '/katalog/novi',  component: () => import('../views/NoviKatalog.vue'), beforeEnter: () => {
      const auth = useAuthStore()
      return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
    }},
    {
      path: '/moji-predlozi',
      component: () => import('../views/MojiPredloziView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/notifikacije',
      component: () => import('../views/NotifikacijeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/predlozi-na-cekanju',
      component: () => import('../views/PredloziNaCekanjuView.vue'),
      meta: { requiresAuth: true }
    },
  {
      path: '/genres/edit',
      component: () => import('../views/FavouriteGenresEdit.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/katalog',
      component: () => import('../views/KatalogView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/katalog/:id',
      component: () => import('../views/KatalogDetaljiView.vue'),
      meta: { requiresAuth: true }
    },
    {
    path: '/ocr',
    component: () => import('../views/OcrView.vue'),
    beforeEnter: () => {
      const auth = useAuthStore()
      return auth.isLibrarianOrAdmin() ? true : '/'
    }
  },
  {
    path: '/zahtevi',
    component: () => import('../views/RequestInboxView.vue'),
    beforeEnter: () => {
      const auth = useAuthStore()
      return auth.isLibrarianOrAdmin() ? true : '/'
        }
    },
{
      path: '/asistent',
      component: () => import('../views/ChatAssistantView.vue'),
      meta: { requiresAuth: true }
    },

  {
  path: '/menadzer',
  component: () => import('../views/menadzer/AppLayout.vue'),
  meta: { requiresAuth: true },
  meta: { requiresAuth: true, requiresRole: 'MENADZER' },
  children: [
    { path: '',                    redirect: '/menadzer/dashboard' },
    { path: 'dashboard',           component: () => import('../views/menadzer/DashboardView.vue') },
    { path: 'dobavljaci',          component: () => import('../views/menadzer/DobavljaciView.vue') },
    { path: 'dobavljaci/novi',     component: () => import('../views/menadzer/DodajDobavljacaView.vue') },
    { path: 'dobavljaci/:id',      component: () => import('../views/menadzer/DobavljacDetaljiView.vue') },
    { path: 'dobavljaci/:id/izmena', component: () => import('../views/menadzer/IzmenaDobavljacaView.vue') },
    { path: 'dobavljaci/:id/ugovor', component: () => import('../views/menadzer/DodajUgovorView.vue') },
    { path: 'predlozi',            component: () => import('../views/menadzer/OdobreniPredloziView.vue') },
    { path: 'sistemske-preporuke', component: () => import('../views/menadzer/SistemskePreporukeView.vue') },
    { path: 'budzet', component: () => import('../views/menadzer/BudzetView.vue') },
    { path: 'narudzbine',          component: () => import('../views/menadzer/NarudzbineView.vue') },
    { path: 'narudzbine/nova',     component: () => import('../views/menadzer/KreirajNarudzbinuView.vue') },
    { path: 'narudzbine/:id',      component: () => import('../views/menadzer/NarudzbinaDetaljiView.vue') },
    { path: 'reklamacije',         component: () => import('../views/menadzer/ReklamacijeView.vue') },
    { path: 'izvestaj',            component: () => import('../views/menadzer/IzvestajView.vue') },
 
  ]
},
{
    path:   '/searchpoc',
    name:   'fulltextsearch',
    component: () => import('../views/SearchPOC.vue')
},
{
    path: '/izvestaj',
    name: 'izvestaj',
    component: () => import('../views/IzvestajView.vue'),
    meta: { requiresAuth: true, roles: ['BIBLIOTEKAR', 'MENADZER', 'ADMINISTRATOR'] }
  },
  {
    path: '/izvestaj-katalog',
    name: 'izvestaj-katalog',
    component: () => import('../views/IzvestajFondView.vue'),
    meta: { requiresAuth: true, roles: ['BIBLIOTEKAR', 'MENADZER', 'ADMINISTRATOR'] }
  },
  {
    path: '/izvestaj-ai-agent',
    name: 'izvestaj-ai-agent',
    component: () => import('../views/IzvestajAIAgent.vue'),
    meta: { requiresAuth: true, roles: ['BIBLIOTEKAR', 'MENADZER', 'ADMINISTRATOR']}
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }

  if (to.meta.requiresRole && auth.user?.role !== to.meta.requiresRole) {
    return '/login'
  }
})

export default router
