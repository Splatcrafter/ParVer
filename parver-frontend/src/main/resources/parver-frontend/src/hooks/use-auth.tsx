import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react"
import { apiClient } from "@/lib/api"

interface User {
  id: number
  username: string
  displayName: string
  role: "ADMIN" | "USER"
  parkingSpotNumber: number | null
}

interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  isAdmin: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const fetchCurrentUser = useCallback(async () => {
    try {
      const response = await apiClient.fetch("/auth/me")
      if (response.ok) {
        setUser(await response.json())
      } else {
        apiClient.clearTokens()
        setUser(null)
      }
    } catch {
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    if (apiClient.hasTokens()) {
      fetchCurrentUser()
    } else {
      setIsLoading(false)
    }
  }, [fetchCurrentUser])

  const login = async (username: string, password: string) => {
    const response = await apiClient.fetch("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    })
    if (!response.ok) {
      throw new Error("Login fehlgeschlagen")
    }
    const data = await response.json()
    apiClient.setTokens(data.accessToken, data.refreshToken)
    setUser(data.user)
  }

  const logout = () => {
    apiClient.clearTokens()
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: user !== null,
        isAdmin: user?.role === "ADMIN",
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext)
  if (!context) throw new Error("useAuth must be used within AuthProvider")
  return context
}
