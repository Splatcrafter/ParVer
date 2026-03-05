import { useCallback, useEffect, useState } from "react"
import { BellOff, Search } from "lucide-react"
import { Button } from "@/components/ui/button"
import { pushApi } from "@/lib/api"

interface NotificationSettingsProps {
  className?: string
}

export function NotificationSettings({ className }: NotificationSettingsProps) {
  const [seekingParking, setSeekingParking] = useState(false)
  const [pushEnabled, setPushEnabled] = useState(false)
  const [loading, setLoading] = useState(true)
  const [permissionState, setPermissionState] = useState<NotificationPermission>("default")

  const loadStatus = useCallback(async () => {
    try {
      const response = await pushApi.getStatus()
      if (response.ok) {
        const data = await response.json()
        setSeekingParking(data.seekingParking)
        setPushEnabled(data.subscribed)
      }
    } catch {
      // ignore
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadStatus()
    if ("Notification" in window) {
      setPermissionState(Notification.permission)
    }
  }, [loadStatus])

  const subscribeToPush = async () => {
    try {
      // Request notification permission
      if ("Notification" in window && Notification.permission === "default") {
        const permission = await Notification.requestPermission()
        setPermissionState(permission)
        if (permission !== "granted") return
      } else if ("Notification" in window && Notification.permission === "denied") {
        return
      }

      // Get VAPID public key
      const vapidResponse = await pushApi.getVapidKey()
      if (!vapidResponse.ok) return
      const { publicKey } = await vapidResponse.json()

      // Subscribe via Push API
      const registration = await navigator.serviceWorker.ready
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(publicKey),
      })

      const subJson = subscription.toJSON()
      await pushApi.subscribe({
        endpoint: subJson.endpoint!,
        p256dh: subJson.keys!.p256dh!,
        auth: subJson.keys!.auth!,
        seekingParking: true,
      })

      setPushEnabled(true)
      setSeekingParking(true)
    } catch {
      // ignore errors
    }
  }

  const unsubscribeFromPush = async () => {
    try {
      const registration = await navigator.serviceWorker.ready
      const subscription = await registration.pushManager.getSubscription()
      if (subscription) {
        await subscription.unsubscribe()
      }
      await pushApi.unsubscribe()
      setPushEnabled(false)
      setSeekingParking(false)
    } catch {
      // ignore
    }
  }

  const toggleSeeking = async () => {
    const newValue = !seekingParking
    if (newValue && !pushEnabled) {
      await subscribeToPush()
      return
    }

    if (!newValue) {
      await unsubscribeFromPush()
      return
    }

    try {
      await pushApi.updateSeeking(newValue)
      setSeekingParking(newValue)
    } catch {
      // ignore
    }
  }

  if (loading) return null

  const denied = permissionState === "denied"

  return (
    <div className={className}>
      <div className="rounded-lg border bg-card p-4">
        <div className="flex items-center gap-3">
          <div className="flex-1">
            <p className="text-sm font-medium">
              {seekingParking ? "Du suchst einen Parkplatz" : "Parkplatz suchen"}
            </p>
            <p className="text-xs text-muted-foreground">
              {seekingParking
                ? "Du wirst benachrichtigt, wenn ein Platz frei wird."
                : "Aktiviere Benachrichtigungen, um informiert zu werden."}
            </p>
            {denied && (
              <p className="mt-1 text-xs text-destructive">
                Benachrichtigungen sind im Browser blockiert. Bitte erlaube sie in den Browser-Einstellungen.
              </p>
            )}
          </div>
          <Button
            variant={seekingParking ? "default" : "outline"}
            size="sm"
            onClick={toggleSeeking}
            disabled={denied && !seekingParking}
          >
            {seekingParking ? (
              <>
                <BellOff className="mr-1.5 h-3.5 w-3.5" />
                Deaktivieren
              </>
            ) : (
              <>
                <Search className="mr-1.5 h-3.5 w-3.5" />
                Aktivieren
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  )
}

function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4)
  const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/")
  const rawData = window.atob(base64)
  const outputArray = new Uint8Array(rawData.length)
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i)
  }
  return outputArray
}
