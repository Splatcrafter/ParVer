import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { motion, AnimatePresence } from "framer-motion"
import { Shield, UserPlus, CheckCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { apiClient } from "@/lib/api"

type SetupStep = "loading" | "otp" | "create-admin" | "done"

export default function SetupPage() {
  const [step, setStep] = useState<SetupStep>("loading")
  const [otp, setOtp] = useState("")
  const [setupToken, setSetupToken] = useState("")
  const [username, setUsername] = useState("")
  const [displayName, setDisplayName] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    apiClient
      .fetch("/setup/status")
      .then((res) => res.json())
      .then((data) => {
        if (!data.setupRequired) {
          navigate("/login", { replace: true })
        } else {
          setStep("otp")
        }
      })
      .catch(() => setStep("otp"))
  }, [navigate])

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsSubmitting(true)

    try {
      const response = await apiClient.fetch("/setup/verify-otp", {
        method: "POST",
        body: JSON.stringify({ otp }),
      })

      if (response.status === 409) {
        navigate("/login", { replace: true })
        return
      }

      if (!response.ok) {
        setError("OTP ist ung\u00FCltig oder abgelaufen.")
        return
      }

      const data = await response.json()
      setSetupToken(data.setupToken)
      setStep("create-admin")
    } catch {
      setError("Verbindung zum Server fehlgeschlagen.")
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleCreateAdmin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (password !== confirmPassword) {
      setError("Passw\u00F6rter stimmen nicht \u00FCberein.")
      return
    }

    if (password.length < 8) {
      setError("Passwort muss mindestens 8 Zeichen lang sein.")
      return
    }

    setIsSubmitting(true)

    try {
      const response = await apiClient.fetch("/setup/create-admin", {
        method: "POST",
        body: JSON.stringify({ setupToken, username, displayName, password }),
      })

      if (response.status === 409) {
        navigate("/login", { replace: true })
        return
      }

      if (response.status === 401) {
        setError(
          "Setup-Token abgelaufen. Bitte starten Sie den Vorgang erneut.",
        )
        setStep("otp")
        setOtp("")
        return
      }

      if (!response.ok) {
        setError("Fehler beim Erstellen des Administrators.")
        return
      }

      setStep("done")
      setTimeout(() => navigate("/login", { replace: true }), 2000)
    } catch {
      setError("Verbindung zum Server fehlgeschlagen.")
    } finally {
      setIsSubmitting(false)
    }
  }

  if (step === "loading") {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-muted-foreground">Laden...</p>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold tracking-tight">ParVer Setup</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Ersteinrichtung des Systems
          </p>
        </div>

        {/* Step indicator */}
        <div className="mb-6 flex items-center justify-center gap-3">
          <div
            className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold ${
              step === "otp"
                ? "bg-primary text-primary-foreground"
                : "bg-muted text-muted-foreground"
            }`}
          >
            1
          </div>
          <div className="h-px w-8 bg-border" />
          <div
            className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold ${
              step === "create-admin"
                ? "bg-primary text-primary-foreground"
                : step === "done"
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted text-muted-foreground"
            }`}
          >
            2
          </div>
        </div>

        <AnimatePresence mode="wait">
          {step === "otp" && (
            <motion.div
              key="otp"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.3 }}
              className="rounded-xl border border-border bg-card p-6 shadow-sm"
            >
              <div className="mb-4 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <Shield className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <h2 className="font-semibold">OTP-Verifizierung</h2>
                  <p className="text-xs text-muted-foreground">
                    Geben Sie den Code aus der Server-Konsole ein
                  </p>
                </div>
              </div>

              <form onSubmit={handleVerifyOtp} className="space-y-4">
                <div>
                  <label
                    htmlFor="otp"
                    className="mb-1.5 block text-sm font-medium"
                  >
                    Einmalpasswort (OTP)
                  </label>
                  <input
                    id="otp"
                    type="text"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    required
                    autoFocus
                    maxLength={6}
                    pattern="[0-9]{6}"
                    placeholder="000000"
                    className="w-full rounded-lg border border-input bg-background px-3 py-2 text-center font-mono text-2xl tracking-[0.5em] outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/20"
                  />
                </div>

                {error && (
                  <motion.p
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="text-sm text-destructive"
                  >
                    {error}
                  </motion.p>
                )}

                <Button
                  type="submit"
                  className="w-full"
                  disabled={isSubmitting || otp.length !== 6}
                >
                  {isSubmitting ? "Pr\u00FCfe..." : "Verifizieren"}
                </Button>
              </form>
            </motion.div>
          )}

          {step === "create-admin" && (
            <motion.div
              key="create-admin"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.3 }}
              className="rounded-xl border border-border bg-card p-6 shadow-sm"
            >
              <div className="mb-4 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <UserPlus className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <h2 className="font-semibold">Administrator erstellen</h2>
                  <p className="text-xs text-muted-foreground">
                    Legen Sie den ersten Admin-Benutzer an
                  </p>
                </div>
              </div>

              <form onSubmit={handleCreateAdmin} className="space-y-4">
                <div>
                  <label
                    htmlFor="admin-username"
                    className="mb-1.5 block text-sm font-medium"
                  >
                    Benutzername
                  </label>
                  <input
                    id="admin-username"
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    autoFocus
                    minLength={3}
                    maxLength={50}
                    autoComplete="username"
                    className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/20"
                  />
                </div>

                <div>
                  <label
                    htmlFor="admin-displayname"
                    className="mb-1.5 block text-sm font-medium"
                  >
                    Anzeigename
                  </label>
                  <input
                    id="admin-displayname"
                    type="text"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    required
                    minLength={1}
                    maxLength={100}
                    className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/20"
                  />
                </div>

                <div>
                  <label
                    htmlFor="admin-password"
                    className="mb-1.5 block text-sm font-medium"
                  >
                    Passwort
                  </label>
                  <input
                    id="admin-password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                    className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/20"
                  />
                </div>

                <div>
                  <label
                    htmlFor="admin-confirm-password"
                    className="mb-1.5 block text-sm font-medium"
                  >
                    Passwort best&auml;tigen
                  </label>
                  <input
                    id="admin-confirm-password"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                    className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/20"
                  />
                </div>

                {error && (
                  <motion.p
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="text-sm text-destructive"
                  >
                    {error}
                  </motion.p>
                )}

                <Button
                  type="submit"
                  className="w-full"
                  disabled={
                    isSubmitting ||
                    !username ||
                    !displayName ||
                    !password ||
                    !confirmPassword
                  }
                >
                  <UserPlus className="mr-2 h-4 w-4" />
                  {isSubmitting
                    ? "Erstelle..."
                    : "Administrator erstellen"}
                </Button>
              </form>
            </motion.div>
          )}

          {step === "done" && (
            <motion.div
              key="done"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.3 }}
              className="rounded-xl border border-border bg-card p-8 shadow-sm"
            >
              <div className="flex flex-col items-center gap-4 text-center">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
                  <CheckCircle className="h-8 w-8 text-green-600" />
                </div>
                <h2 className="text-lg font-semibold">Setup abgeschlossen</h2>
                <p className="text-sm text-muted-foreground">
                  Sie werden zum Login weitergeleitet...
                </p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        <p className="mt-6 text-center text-xs text-muted-foreground/50">
          &copy; {new Date().getFullYear()} Splatgames.de Software
        </p>
      </div>
    </div>
  )
}
