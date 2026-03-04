import { AnimatePresence, motion } from "framer-motion"
import { usePWA } from "@/hooks/use-pwa"
import { InstallPrompt } from "@/components/install-prompt"

interface PWAGateProps {
  children: React.ReactNode
}

export function PWAGate({ children }: PWAGateProps) {
  const {
    showInstallScreen,
    canInstall,
    platform,
    triggerInstall,
    dismissInstallScreen,
  } = usePWA()

  return (
    <AnimatePresence mode="wait">
      {showInstallScreen ? (
        <motion.div
          key="install-prompt"
          initial={{ opacity: 1 }}
          exit={{ opacity: 0, transition: { duration: 0.3 } }}
        >
          <InstallPrompt
            platform={platform}
            canInstall={canInstall}
            onInstall={triggerInstall}
            onDismiss={dismissInstallScreen}
          />
        </motion.div>
      ) : (
        <motion.div
          key="app-content"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1, transition: { duration: 0.3, delay: 0.1 } }}
        >
          {children}
        </motion.div>
      )}
    </AnimatePresence>
  )
}
