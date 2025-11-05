"use client"

import { ProtectedRoute } from "@/components/protected-route"
import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Users, HardDrive, Activity, TrendingUp } from "lucide-react"
import useSWR from "swr"
import { api } from "@/lib/api"
import { useAuth } from "@/components/auth-provider"
import RotatingEarth from "@/components/rotating-earth"

export default function DashboardPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === "ADMIN"

  // Fetch data for dashboard stats
  const { data: users } = useSWR(
    isAdmin ? "/api/users" : null,
    () => api.getAllUsers()
  )

  const { data: devices } = useSWR(
    "/api/devices",
    () => api.getAllDevices()
  )

  const totalUsers = users?.length || 0
  const totalDevices = devices?.length || 0
  const activeSessions = 1 // Current user is always active

  return (
    <ProtectedRoute>
      <DashboardLayout>
        <div className="space-y-6">
          <div>
            <h2 className="text-3xl font-bold tracking-tight text-white">Dashboard Overview</h2>
            <p className="text-gray-400">Welcome to your energy management dashboard. Monitor and manage your system.</p>
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card className="border-white/10 bg-black/40 backdrop-blur-xl">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-white">Total Users</CardTitle>
                <Users className="h-4 w-4 text-gray-400" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-white">{totalUsers}</div>
                <p className="text-xs text-gray-400">Registered users</p>
              </CardContent>
            </Card>

            <Card className="border-white/10 bg-black/40 backdrop-blur-xl">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-white">Total Devices</CardTitle>
                <HardDrive className="h-4 w-4 text-gray-400" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-white">{totalDevices}</div>
                <p className="text-xs text-gray-400">Connected devices</p>
              </CardContent>
            </Card>

            <Card className="border-white/10 bg-black/40 backdrop-blur-xl">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-white">Active Sessions</CardTitle>
                <Activity className="h-4 w-4 text-gray-400" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-white">{activeSessions}</div>
                <p className="text-xs text-gray-400">Currently active</p>
              </CardContent>
            </Card>

            <Card className="border-white/10 bg-black/40 backdrop-blur-xl">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-white">System Status</CardTitle>
                <TrendingUp className="h-4 w-4 text-gray-400" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-white">Operational</div>
                <p className="text-xs text-gray-400">All systems running</p>
              </CardContent>
            </Card>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <Card className="border-white/10 bg-black/40 backdrop-blur-xl">
              <CardHeader>
                <CardTitle className="text-white">Energy Consumption</CardTitle>
                <CardDescription className="text-gray-400">Maximum hourly consumption by device</CardDescription>
              </CardHeader>
              <CardContent>
                {devices && devices.length > 0 ? (
                  <div className="space-y-3">
                    {devices.slice(0, 5).map((device) => (
                      <div key={device.id} className="space-y-1">
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-gray-300 truncate max-w-[200px]">{device.description}</span>
                          <span className="text-white font-semibold">{device.maximumHourlyConsumption} kWh</span>
                        </div>
                        <div className="w-full bg-gray-700 rounded-full h-2">
                          <div
                            className="bg-gradient-to-r from-cyan-500 to-blue-500 h-2 rounded-full transition-all duration-500"
                            style={{
                              width: `${Math.min((device.maximumHourlyConsumption / 50) * 100, 100)}%`,
                            }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-400">No devices to display</p>
                )}
              </CardContent>
            </Card>

            <Card className="border-white/10 bg-black/40 backdrop-blur-xl overflow-hidden">
              <CardContent className="p-4 flex items-center justify-center">
                <RotatingEarth width={350} height={350} />
              </CardContent>
            </Card>
          </div>
        </div>
      </DashboardLayout>
    </ProtectedRoute>
  )
}
