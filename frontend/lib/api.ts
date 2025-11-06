const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost"

export interface User {
  id: string
  username: string
  role: "ADMIN" | "CLIENT"
  fullName: string
  email: string
}

export interface CreateUserData {
  username: string
  password: string
  role: "ADMIN" | "CLIENT"
  fullName: string
  email: string
}

export interface UpdateUserData {
  username?: string
  password?: string
  role?: "ADMIN" | "CLIENT"
  fullName?: string
  email?: string
}

export interface Device {
  id: string
  description: string
  address: string
  maximumHourlyConsumption: number
  userId: string | null
  username: string | null
  userEmail: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateDeviceData {
  description: string
  address: string
  maximumHourlyConsumption: number
  userId?: string
}

export interface UpdateDeviceData {
  description?: string
  address?: string
  maximumHourlyConsumption?: number
  userId?: string
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  type: string
  userId: string
  username: string
  role: "ADMIN" | "CLIENT"
}

export interface RegisterData {
  username: string
  email: string
  password: string
  role: "ADMIN" | "CLIENT"
}

class ApiClient {
  private getAuthHeader(): HeadersInit {
    if (typeof window === "undefined") return {}
    const token = localStorage.getItem("token")
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    })

    if (!response.ok) {
      throw new Error("Login failed")
    }

    return response.json()
  }

  async register(data: RegisterData): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Registration failed")
    }

    return response.json()
  }

  async validateToken(token: string): Promise<boolean> {
    const response = await fetch(`${API_BASE_URL}/api/auth/validate?token=${token}`, {
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      return false
    }

    return response.json()
  }

  //CLIENT -->server getAllUsers
  async getAllUsers(): Promise<User[]> {
    const response = await fetch(`${API_BASE_URL}/api/users`, {
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to fetch users")
    }

    return response.json()
  }

  async getUserById(id: string): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/api/users/${id}`, {
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to fetch user")
    }

    return response.json()
  }

  async createUser(data: CreateUserData): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/api/users`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeader(),
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      throw new Error("Failed to create user")
    }

    return response.json()
  }

  async updateUser(id: string, data: UpdateUserData): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/api/users/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeader(),
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      throw new Error("Failed to update user")
    }

    return response.json()
  }

  async deleteUser(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/users/${id}`, {
      method: "DELETE",
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to delete user")
    }
  }

  async getAllDevices(): Promise<Device[]> {
    console.log("API getAllDevices called")
    console.log("Auth header:", this.getAuthHeader())
    console.log("API URL:", `${API_BASE_URL}/api/devices`)
    
    const response = await fetch(`${API_BASE_URL}/api/devices`, {
      headers: this.getAuthHeader(),
    })

    console.log("Response status:", response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error("Error response:", errorText)
      throw new Error(`Failed to fetch devices: ${response.status} - ${errorText}`)
    }

    const result = await response.json()
    console.log("Devices fetched:", result)
    return result
  }

  async getDeviceById(id: string): Promise<Device> {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to fetch device")
    }

    return response.json()
  }

  async getDevicesByUserId(userId: string): Promise<Device[]> {
    const response = await fetch(`${API_BASE_URL}/api/devices/user/${userId}`, {
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to fetch user devices")
    }

    return response.json()
  }

  async createDevice(data: CreateDeviceData): Promise<Device> {
    console.log("API createDevice called with:", data)
    console.log("Auth header:", this.getAuthHeader())
    console.log("API URL:", `${API_BASE_URL}/api/devices`)
    
    const response = await fetch(`${API_BASE_URL}/api/devices`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeader(),
      },
      body: JSON.stringify(data),
    })

    console.log("Response status:", response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error("Error response:", errorText)
      throw new Error(`Failed to create device: ${response.status} - ${errorText}`)
    }

    const result = await response.json()
    console.log("Device created successfully:", result)
    return result
  }

  async updateDevice(id: string, data: UpdateDeviceData): Promise<Device> {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeader(),
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      throw new Error("Failed to update device")
    }

    return response.json()
  }

  async deleteDevice(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
      method: "DELETE",
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to delete device")
    }
  }

  async assignDeviceToUser(deviceId: string, userId: string): Promise<Device> {
    const response = await fetch(`${API_BASE_URL}/api/devices/${deviceId}/assign/${userId}`, {
      method: "PUT",
      headers: this.getAuthHeader(),
    })

    if (!response.ok) {
      throw new Error("Failed to assign device to user")
    }

    return response.json()
  }
}

export const api = new ApiClient()
