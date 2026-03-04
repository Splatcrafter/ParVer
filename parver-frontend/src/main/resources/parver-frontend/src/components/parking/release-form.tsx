import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { parkingApi } from "@/lib/api"
import { toLocalISOString } from "@/lib/utils"

interface ReleaseFormProps {
  spotNumber: number
  onCreated: () => void
}

export function ReleaseForm({ spotNumber, onCreated }: ReleaseFormProps) {
  const [availableFrom, setAvailableFrom] = useState("")
  const [availableTo, setAvailableTo] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!availableFrom || !availableTo) {
      setError("Bitte beide Zeitpunkte angeben.")
      return
    }

    const from = toLocalISOString(availableFrom)
    const to = toLocalISOString(availableTo)

    setSubmitting(true)
    try {
      const response = await parkingApi.createRelease(spotNumber, from, to)
      if (!response.ok) {
        const text = await response.text()
        try {
          const data = JSON.parse(text)
          setError(data.message || data.detail || "Freigabe konnte nicht erstellt werden.")
        } catch {
          setError("Freigabe konnte nicht erstellt werden.")
        }
        return
      }
      setAvailableFrom("")
      setAvailableTo("")
      onCreated()
    } catch {
      setError("Verbindungsfehler. Bitte prüfen Sie Ihre Internetverbindung.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <h4 className="text-sm font-semibold">Parkplatz freigeben</h4>
      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-1">
          <Label htmlFor="release-from" className="text-xs">Von</Label>
          <Input
            id="release-from"
            type="datetime-local"
            value={availableFrom}
            onChange={(e) => setAvailableFrom(e.target.value)}
            className="text-xs"
          />
        </div>
        <div className="space-y-1">
          <Label htmlFor="release-to" className="text-xs">Bis</Label>
          <Input
            id="release-to"
            type="datetime-local"
            value={availableTo}
            onChange={(e) => setAvailableTo(e.target.value)}
            className="text-xs"
          />
        </div>
      </div>
      {error && <p className="text-xs text-destructive">{error}</p>}
      <Button type="submit" size="sm" className="w-full" disabled={submitting}>
        {submitting ? "Wird freigegeben…" : "Freigeben"}
      </Button>
    </form>
  )
}
