import { useState } from "react"
import { AnimatePresence, motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { SmallParkingLot } from "@/components/parking/small-parking-lot"
import { cn } from "@/lib/utils"

type ParkingArea = "small" | "large"

export default function ParkingPage() {
  const [activeArea, setActiveArea] = useState<ParkingArea>("small")

  return (
    <div className="flex min-h-screen flex-col items-center px-4 py-6">
      {/* Header */}
      <div className="text-center">
        <h1 className="text-2xl font-bold tracking-tight">ParVer</h1>
        <p className="text-sm text-muted-foreground">
          Parkplatzverwaltung &ndash; AFBB
        </p>
      </div>

      {/* Toggle Buttons */}
      <div className="mt-6 flex gap-2">
        <Button
          variant={activeArea === "small" ? "default" : "outline"}
          size="sm"
          onClick={() => setActiveArea("small")}
          className={cn(
            "transition-all",
            activeArea !== "small" && "text-muted-foreground",
          )}
        >
          Kleine Parkfl&auml;che
        </Button>
        <Button
          variant={activeArea === "large" ? "default" : "outline"}
          size="sm"
          onClick={() => setActiveArea("large")}
          className={cn(
            "transition-all",
            activeArea !== "large" && "text-muted-foreground",
          )}
        >
          Gro&szlig;e Parkfl&auml;che
        </Button>
      </div>

      {/* Parking Lot Content */}
      <div className="mt-6 flex w-full max-w-2xl flex-1 justify-center">
        <AnimatePresence mode="wait">
          {activeArea === "small" ? (
            <motion.div
              key="small"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.25 }}
              className="w-full"
            >
              <SmallParkingLot />
            </motion.div>
          ) : (
            <motion.div
              key="large"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.25 }}
              className="flex w-full flex-col items-center justify-center py-20"
            >
              <p className="text-lg font-semibold text-muted-foreground">
                Coming Soon
              </p>
              <p className="mt-2 text-sm text-muted-foreground/60">
                Die gro&szlig;e Parkfl&auml;che wird bald verf&uuml;gbar sein.
              </p>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Footer */}
      <div className="mt-auto flex flex-col items-center gap-1 pt-6">
        <p className="text-xs text-muted-foreground/50">
          &copy; {new Date().getFullYear()} Splatgames.de Software
        </p>
        <p className="font-mono text-[10px] text-muted-foreground/40">
          Build {__BUILD_TIMESTAMP__}
        </p>
      </div>
    </div>
  )
}
