import { useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { usersApi } from "@/lib/api"
import type { components } from "@/lib/api-types"

type UserResponse = components["schemas"]["UserResponse"]

interface DeleteUserDialogProps {
  user: UserResponse | null
  onOpenChange: (open: boolean) => void
  onDeleted: () => void
}

export function DeleteUserDialog({ user, onOpenChange, onDeleted }: DeleteUserDialogProps) {
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleDelete = async () => {
    if (!user) return
    setError(null)
    setSubmitting(true)
    try {
      const response = await usersApi.delete(user.id)
      if (response.status === 204 || response.ok) {
        onOpenChange(false)
        onDeleted()
      } else {
        const text = await response.text()
        try {
          const data = JSON.parse(text)
          setError(data.message || data.detail || "Benutzer konnte nicht gelöscht werden.")
        } catch {
          setError("Benutzer konnte nicht gelöscht werden.")
        }
      }
    } catch {
      setError("Verbindungsfehler. Bitte prüfen Sie Ihre Internetverbindung.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog open={user !== null} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Benutzer löschen</DialogTitle>
          <DialogDescription>
            Möchten Sie den Benutzer &bdquo;{user?.displayName}&ldquo; ({user?.username}) wirklich
            löschen? Diese Aktion kann nicht rückgängig gemacht werden.
          </DialogDescription>
        </DialogHeader>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Abbrechen
          </Button>
          <Button variant="destructive" onClick={handleDelete} disabled={submitting}>
            {submitting ? "Lösche\u2026" : "Löschen"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
