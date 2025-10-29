import { jwtDecode } from "jwt-decode"

interface JwtPayload {
  sub: string
  username: string
  role: string
  exp: number
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("token")
}

export function setToken(token: string): void {
  localStorage.setItem("token", token)
}

export function removeToken(): void {
  localStorage.removeItem("token")
}

export function decodeToken(token: string): JwtPayload | null {
  try {
    return jwtDecode<JwtPayload>(token)
  } catch {
    return null
  }
}

export function isTokenValid(token: string): boolean {
  const decoded = decodeToken(token)
  if (!decoded) return false

  const currentTime = Date.now() / 1000
  return decoded.exp > currentTime
}

export function getCurrentUser(): { id: string; username: string; role: string } | null {
  const token = getToken()
  if (!token || !isTokenValid(token)) {
    return null
  }

  const decoded = decodeToken(token)
  if (!decoded) return null

  return {
    id: decoded.sub,
    username: decoded.username,
    role: decoded.role,
  }
}
