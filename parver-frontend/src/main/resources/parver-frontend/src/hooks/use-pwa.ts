import { useState, useEffect, useCallback } from "react"

interface BeforeInstallPromptEvent extends Event {
  readonly platforms: string[]
  readonly userChoice: Promise<{
    outcome: "accepted" | "dismissed"
    platform: string
  }>
  prompt(): Promise<void>
}

declare global {
  interface WindowEventMap {
    beforeinstallprompt: BeforeInstallPromptEvent
  }
}

const STORAGE_KEY = "parver-pwa-browser-preference"

export type Platform = "ios" | "android" | "desktop"

function detectPlatform(): Platform {
  const ua = navigator.userAgent.toLowerCase()
  if (/iphone|ipad|ipod/.test(ua)) return "ios"
  if (/android/.test(ua)) return "android"
  return "desktop"
}

function isStandaloneMode(): boolean {
  if (window.matchMedia("(display-mode: standalone)").matches) return true
  if (
    "standalone" in navigator &&
    (navigator as unknown as { standalone: boolean }).standalone
  )
    return true
  if (document.referrer.startsWith("android-app://")) return true
  return false
}

function getBrowserPreference(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === "dismissed"
  } catch {
    return false
  }
}

function setBrowserPreference(): void {
  localStorage.setItem(STORAGE_KEY, "dismissed")
}

export function usePWA() {
  const [installPromptEvent, setInstallPromptEvent] =
    useState<BeforeInstallPromptEvent | null>(null)
  const [isInstalled, setIsInstalled] = useState(false)
  const [isStandalone, setIsStandalone] = useState(false)
  const [showInstallScreen, setShowInstallScreen] = useState(false)
  const [platform, setPlatform] = useState<Platform>("desktop")

  useEffect(() => {
    setPlatform(detectPlatform())

    const standalone = isStandaloneMode()
    setIsStandalone(standalone)

    if (standalone) {
      setShowInstallScreen(false)
      return
    }

    if (getBrowserPreference()) {
      setShowInstallScreen(false)
      return
    }

    setShowInstallScreen(true)

    const handleBeforeInstallPrompt = (e: BeforeInstallPromptEvent) => {
      e.preventDefault()
      setInstallPromptEvent(e)
    }

    const handleAppInstalled = () => {
      setIsInstalled(true)
      setShowInstallScreen(false)
      setInstallPromptEvent(null)
    }

    window.addEventListener("beforeinstallprompt", handleBeforeInstallPrompt)
    window.addEventListener("appinstalled", handleAppInstalled)

    const mediaQuery = window.matchMedia("(display-mode: standalone)")
    const handleDisplayModeChange = (e: MediaQueryListEvent) => {
      if (e.matches) {
        setIsStandalone(true)
        setShowInstallScreen(false)
      }
    }
    mediaQuery.addEventListener("change", handleDisplayModeChange)

    return () => {
      window.removeEventListener(
        "beforeinstallprompt",
        handleBeforeInstallPrompt,
      )
      window.removeEventListener("appinstalled", handleAppInstalled)
      mediaQuery.removeEventListener("change", handleDisplayModeChange)
    }
  }, [])

  const triggerInstall = useCallback(async () => {
    if (!installPromptEvent) return false
    await installPromptEvent.prompt()
    const { outcome } = await installPromptEvent.userChoice
    setInstallPromptEvent(null)
    if (outcome === "accepted") {
      setIsInstalled(true)
      setShowInstallScreen(false)
      return true
    }
    return false
  }, [installPromptEvent])

  const dismissInstallScreen = useCallback(() => {
    setBrowserPreference()
    setShowInstallScreen(false)
  }, [])

  return {
    canInstall: installPromptEvent !== null,
    isInstalled,
    isStandalone,
    showInstallScreen,
    platform,
    triggerInstall,
    dismissInstallScreen,
  }
}
