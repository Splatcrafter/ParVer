import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { parkingApi } from "@/lib/api"
import { toLocalISOString } from "@/lib/utils"
import type { components } from "@/lib/api-types"

type ParkingSpotRelease = components["schemas"]["ParkingSpotRelease"]

interface BookingFormProps {
  spotNumber: number
  releases: ParkingSpotRelease[]
  onBooked: () => void
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  })
}

export function BookingForm({ spotNumber, releases, onBooked }: BookingFormProps) {
  const [selectedReleaseId, setSelectedReleaseId] = useState<number | null>(
    releases.length === 1 ? releases[0].id : null
  )
  const [bookedFrom, setBookedFrom] = useState("")
  const [bookedTo, setBookedTo] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (selectedReleaseId === null) {
      setError("Bitte eine Freigabe auswählen.")
      return
    }
    if (!bookedFrom || !bookedTo) {
      setError("Bitte beide Zeitpunkte angeben.")
      return
    }

    const from = toLocalISOString(bookedFrom)
    const to = toLocalISOString(bookedTo)

    setSubmitting(true)
    try {
      const response = await parkingApi.createBooking(spotNumber, selectedReleaseId, from, to)
      if (!response.ok) {
        const text = await response.text()
        try {
          const data = JSON.parse(text)
          setError(data.message || data.detail || "Buchung konnte nicht erstellt werden.")
        } catch {
          setError("Buchung konnte nicht erstellt werden.")
        }
        return
      }
      setBookedFrom("")
      setBookedTo("")
      onBooked()
    } catch {
      setError("Verbindungsfehler. Bitte prüfen Sie Ihre Internetverbindung.")
    } finally {
      setSubmitting(false)
    }
  }

  if (releases.length === 0) {
    return (
      <p className="text-xs text-muted-foreground">
        Derzeit keine Freigaben verfügbar.
      </p>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <h4 className="text-sm font-semibold">Parkplatz buchen</h4>

      {releases.length > 1 && (
        <div className="space-y-1">
          <Label className="text-xs">Freigabe wählen</Label>
          <div className="space-y-1">
            {releases.map((r) => (
              <button
                key={r.id}
                type="button"
                onClick={() => setSelectedReleaseId(r.id)}
                className={`w-full rounded-md border px-3 py-2 text-left text-xs transition-colors ${
                  selectedReleaseId === r.id
                    ? "border-primary bg-primary/5"
                    : "border-border hover:bg-accent"
                }`}
              >
                {formatDateTime(r.availableFrom)} – {formatDateTime(r.availableTo)}
              </button>
            ))}
          </div>
        </div>
      )}

      {releases.length === 1 && (
        <p className="text-xs text-muted-foreground">
          Freigabe: {formatDateTime(releases[0].availableFrom)} – {formatDateTime(releases[0].availableTo)}
        </p>
      )}

      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-1">
          <Label htmlFor="booking-from" className="text-xs">Von</Label>
          <Input
            id="booking-from"
            type="datetime-local"
            value={bookedFrom}
            onChange={(e) => setBookedFrom(e.target.value)}
            className="text-xs"
          />
        </div>
        <div className="space-y-1">
          <Label htmlFor="booking-to" className="text-xs">Bis</Label>
          <Input
            id="booking-to"
            type="datetime-local"
            value={bookedTo}
            onChange={(e) => setBookedTo(e.target.value)}
            className="text-xs"
          />
        </div>
      </div>
      {error && <p className="text-xs text-destructive">{error}</p>}
      <Button type="submit" size="sm" className="w-full" disabled={submitting}>
        {submitting ? "Wird gebucht…" : "Buchen"}
      </Button>
    </form>
  )
}
