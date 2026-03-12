const CACHE_NAME = 'project-manager-v1';
const STATIC_CACHE = 'static-v1';
const DYNAMIC_CACHE = 'dynamic-v1';

const STATIC_ASSETS = [
  '/',
  '/login',
  '/manifest.json',
  'https://cdn.tailwindcss.com/3.3.0'
];

// Install event - cache static assets
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then(cache => {
        return cache.addAll(STATIC_ASSETS);
      })
      .catch(err => console.log('Cache install failed:', err))
  );
  self.skipWaiting();
});

// Activate event - clean up old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== STATIC_CACHE && cacheName !== DYNAMIC_CACHE) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  self.clients.claim();
});

// Fetch event - serve from cache with network fallback
self.addEventListener('fetch', event => {
  const { request } = event;
  
  // Skip non-GET requests
  if (request.method !== 'GET') {
    return;
  }

  // Handle static assets
  if (request.url.includes('tailwindcss.com') || 
      request.url.includes('/manifest.json') ||
      request.url.includes('/icons/')) {
    event.respondWith(
      caches.match(request)
        .then(response => {
          return response || fetch(request).then(fetchResponse => {
            return caches.open(STATIC_CACHE).then(cache => {
              cache.put(request, fetchResponse.clone());
              return fetchResponse;
            });
          });
        })
        .catch(() => {
          // Offline fallback for critical resources
          if (request.url.includes('tailwindcss.com')) {
            return new Response('/* Offline - Tailwind CSS unavailable */', {
              headers: { 'Content-Type': 'text/css' }
            });
          }
        })
    );
    return;
  }

  // Handle HTML pages - network first with cache fallback
  if (request.headers.get('accept').includes('text/html')) {
    event.respondWith(
      fetch(request)
        .then(response => {
          // Cache successful responses
          if (response.status === 200) {
            const responseClone = response.clone();
            caches.open(DYNAMIC_CACHE).then(cache => {
              cache.put(request, responseClone);
            });
          }
          return response;
        })
        .catch(() => {
          // Serve from cache if network fails
          return caches.match(request).then(response => {
            return response || caches.match('/').then(fallback => {
              return fallback || new Response('Offline', {
                status: 200,
                headers: { 'Content-Type': 'text/html' }
              });
            });
          });
        })
    );
    return;
  }

  // Handle images and uploads
  if (request.url.includes('/uploads/')) {
    event.respondWith(
      caches.match(request)
        .then(response => {
          return response || fetch(request).then(fetchResponse => {
            // Only cache successful image responses
            if (fetchResponse.status === 200) {
              const responseClone = fetchResponse.clone();
              caches.open(DYNAMIC_CACHE).then(cache => {
                cache.put(request, responseClone);
              });
            }
            return fetchResponse;
          });
        })
        .catch(() => {
          // Return placeholder for failed image loads
          return new Response('', { status: 404 });
        })
    );
    return;
  }

  // Default: network first
  event.respondWith(
    fetch(request).catch(() => caches.match(request))
  );
});
