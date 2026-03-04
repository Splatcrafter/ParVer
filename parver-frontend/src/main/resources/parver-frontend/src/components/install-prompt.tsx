import { motion } from "framer-motion"
import { Download, Monitor, Smartphone, Globe, ParkingSquare } from "lucide-react"
import { Button } from "@/components/ui/button"
import type { Platform } from "@/hooks/use-pwa"

interface InstallPromptProps {
  platform: Platform
  canInstall: boolean
  onInstall: () => void
  onDismiss: () => void
}

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.12,
      delayChildren: 0.2,
    },
  },
}

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
      ease: [0.25, 0.46, 0.45, 0.94] as [number, number, number, number],
    },
  },
}

const platformInstructions: Record<
  Platform,
  { icon: typeof Monitor; steps: string[] }
> = {
  ios: {
    icon: Smartphone,
    steps: [
      "Tippen Sie auf das Teilen-Symbol (\u{25A1}\u{2191}) in der Navigationsleiste",
      'Scrollen Sie nach unten und tippen Sie auf "Zum Home-Bildschirm"',
      'Tippen Sie auf "Hinzuf\u00FCgen" um die Installation abzuschlie\u00DFen',
    ],
  },
  android: {
    icon: Smartphone,
    steps: [
      "Tippen Sie auf das Men\u00FC-Symbol (\u22EE) oben rechts",
      'W\u00E4hlen Sie "App installieren" oder "Zum Startbildschirm hinzuf\u00FCgen"',
      "Best\u00E4tigen Sie die Installation im Dialog",
    ],
  },
  desktop: {
    icon: Monitor,
    steps: [
      "Klicken Sie auf das Installieren-Symbol (\u2295) in der Adressleiste",
      'Alternativ: Men\u00FC \u2192 "ParVer installieren"',
      "Best\u00E4tigen Sie die Installation im Dialog",
    ],
  },
}

export function InstallPrompt({
  platform,
  canInstall,
  onInstall,
  onDismiss,
}: InstallPromptProps) {
  const instructions = platformInstructions[platform]
  const PlatformIcon = instructions.icon

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background">
      <motion.div
        className="flex w-full max-w-lg flex-col items-center px-6 py-12"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {/* App Icon */}
        <motion.div variants={itemVariants}>
          <motion.div
            className="mb-8 flex size-24 items-center justify-center rounded-2xl bg-primary shadow-lg"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <ParkingSquare
              className="size-12 text-primary-foreground"
              strokeWidth={1.5}
            />
          </motion.div>
        </motion.div>

        {/* App Name */}
        <motion.h1
          className="text-3xl font-bold tracking-tight"
          variants={itemVariants}
        >
          ParVer
        </motion.h1>

        {/* Subtitle */}
        <motion.p
          className="mt-1 text-sm text-muted-foreground"
          variants={itemVariants}
        >
          Parkplatzverwaltung
        </motion.p>

        {/* Organization */}
        <motion.p
          className="mt-1 text-xs text-muted-foreground/70"
          variants={itemVariants}
        >
          AFBB &ndash; Akademie f&uuml;r berufliche Bildung gGmbH
        </motion.p>

        {/* Description Card */}
        <motion.div
          className="mt-8 rounded-lg border bg-card p-4 text-center"
          variants={itemVariants}
        >
          <p className="text-sm leading-relaxed text-card-foreground">
            Installieren Sie ParVer als App auf Ihrem Ger&auml;t f&uuml;r
            schnellen Zugriff, Offline-Unterst&uuml;tzung und eine optimierte
            Benutzererfahrung &ndash; direkt von Ihrem Startbildschirm.
          </p>
        </motion.div>

        {/* Platform Instructions */}
        <motion.div
          className="mt-6 w-full rounded-lg border bg-muted/50 p-4"
          variants={itemVariants}
        >
          <div className="mb-3 flex items-center gap-2">
            <PlatformIcon
              className="size-4 text-muted-foreground"
              strokeWidth={1.5}
            />
            <span className="text-sm font-medium">
              {platform === "ios"
                ? "Installation auf iOS"
                : platform === "android"
                  ? "Installation auf Android"
                  : "Installation auf dem Desktop"}
            </span>
          </div>
          <ol className="space-y-2">
            {instructions.steps.map((step, i) => (
              <motion.li
                key={i}
                className="flex gap-3 text-sm text-muted-foreground"
                variants={itemVariants}
              >
                <span className="flex size-5 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-medium text-primary-foreground">
                  {i + 1}
                </span>
                <span>{step}</span>
              </motion.li>
            ))}
          </ol>
        </motion.div>

        {/* Action Buttons */}
        <motion.div
          className="mt-8 flex w-full flex-col gap-3"
          variants={itemVariants}
        >
          {canInstall ? (
            <Button size="lg" className="w-full" onClick={onInstall}>
              <Download className="size-4" />
              App installieren
            </Button>
          ) : (
            <Button size="lg" className="w-full" disabled>
              <Globe className="size-4" />
              {platform === "ios"
                ? "Siehe Anleitung oben"
                : "Installation vorbereiten\u2026"}
            </Button>
          )}

          <Button
            variant="ghost"
            size="lg"
            className="w-full text-muted-foreground"
            onClick={onDismiss}
          >
            Weiter im Browser
          </Button>
        </motion.div>

        {/* Footer */}
        <motion.div
          className="mt-10 flex flex-col items-center gap-1"
          variants={itemVariants}
        >
          <p className="text-xs text-muted-foreground/50">
            &copy; {new Date().getFullYear()} Splatgames.de Software
          </p>
          <p className="font-mono text-[10px] text-muted-foreground/40">
            Build {__BUILD_TIMESTAMP__}
          </p>
        </motion.div>
      </motion.div>
    </div>
  )
}
