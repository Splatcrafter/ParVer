import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import { BrowserRouter } from "react-router-dom"
import { AuthProvider } from "@/hooks/use-auth"
import { PWAGate } from "@/components/pwa-gate"
import App from "./App"
import "./index.css"

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <PWAGate>
          <App />
        </PWAGate>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
