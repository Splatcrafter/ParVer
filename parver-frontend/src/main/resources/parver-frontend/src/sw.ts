/// <reference lib="webworker" />
import { cleanupOutdatedCaches, precacheAndRoute } from "workbox-precaching"
import { registerRoute } from "workbox-routing"
import { NetworkFirst } from "workbox-strategies"
import { ExpirationPlugin } from "workbox-expiration"
import { CacheableResponsePlugin } from "workbox-cacheable-response"

declare let self: ServiceWorkerGlobalScope

// Precache assets injected by workbox
cleanupOutdatedCaches()
precacheAndRoute(self.__WB_MANIFEST)

// Runtime caching for API requests (exclude SSE endpoint)
registerRoute(
  ({ url }) => url.pathname.startsWith("/api/") && !url.pathname.includes("/events"),
  new NetworkFirst({
    cacheName: "api-cache",
    plugins: [
      new ExpirationPlugin({
        maxEntries: 50,
        maxAgeSeconds: 60 * 60 * 24,
      }),
      new CacheableResponsePlugin({
        statuses: [0, 200],
      }),
    ],
  }),
)

// Push notification handler
self.addEventListener("push", (event) => {
  if (!event.data) return

  let data: { title?: string; body?: string; url?: string }
  try {
    data = event.data.json()
  } catch {
    data = { title: "ParVer", body: event.data.text() }
  }

  const title = data.title ?? "ParVer"
  const options: NotificationOptions = {
    body: data.body ?? "",
    icon: "/pwa-192x192.png",
    badge: "/pwa-192x192.png",
    data: { url: data.url ?? "/" },
  }

  event.waitUntil(self.registration.showNotification(title, options))
})

// Notification click handler
self.addEventListener("notificationclick", (event) => {
  event.notification.close()

  const url = (event.notification.data?.url as string) ?? "/"

  event.waitUntil(
    self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
      // Focus existing window if available
      for (const client of clientList) {
        if (client.url.includes(self.location.origin) && "focus" in client) {
          return client.focus()
        }
      }
      // Otherwise open new window
      return self.clients.openWindow(url)
    }),
  )
})
