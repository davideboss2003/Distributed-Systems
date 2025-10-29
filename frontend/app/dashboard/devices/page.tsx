"use client"

import { useState } from "react"
import useSWR from "swr"
import { ProtectedRoute } from "@/components/protected-route"
import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { api, type Device, type CreateDeviceData, type User } from "@/lib/api"
import { toast } from "sonner"
import { Plus, Pencil, Trash2, UserPlus } from "lucide-react"

export default function DevicesPage() {
  const { data: devices, mutate } = useSWR<Device[]>("/api/devices", () => api.getAllDevices())
  const { data: users } = useSWR<User[]>("/api/users", () => api.getAllUsers())
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isEditOpen, setIsEditOpen] = useState(false)
  const [isDeleteOpen, setIsDeleteOpen] = useState(false)
  const [isAssignOpen, setIsAssignOpen] = useState(false)
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null)
  const [selectedUserId, setSelectedUserId] = useState<string>("")
  const [formData, setFormData] = useState<CreateDeviceData>({
    description: "",
    address: "",
    maximumHourlyConsumption: 0,
    userId: undefined,
  })

  const handleCreate = async () => {
    try {
      console.log("Creating device with data:", formData)
      const result = await api.createDevice(formData)
      console.log("Device created:", result)
      mutate()
      setIsCreateOpen(false)
      setFormData({
        description: "",
        address: "",
        maximumHourlyConsumption: 0,
        userId: undefined,
      })
      toast.success("Device created successfully")
    } catch (error) {
      console.error("Failed to create device:", error)
      toast.error("Failed to create device")
    }
  }

  const handleUpdate = async () => {
    if (!selectedDevice) return

    try {
      await api.updateDevice(selectedDevice.id, {
        description: formData.description,
        address: formData.address,
        maximumHourlyConsumption: formData.maximumHourlyConsumption,
        userId: formData.userId,
      })
      mutate()
      setIsEditOpen(false)
      setSelectedDevice(null)
      toast.success("Device updated successfully")
    } catch (error) {
      toast.error("Failed to update device")
    }
  }

  const handleDelete = async () => {
    if (!selectedDevice) return

    try {
      await api.deleteDevice(selectedDevice.id)
      mutate()
      setIsDeleteOpen(false)
      setSelectedDevice(null)
      toast.success("Device deleted successfully")
    } catch (error) {
      toast.error("Failed to delete device")
    }
  }

  const handleAssign = async () => {
    if (!selectedDevice || !selectedUserId) return

    try {
      await api.assignDeviceToUser(selectedDevice.id, selectedUserId)
      mutate()
      setIsAssignOpen(false)
      setSelectedDevice(null)
      setSelectedUserId("")
      toast.success("Device assigned successfully")
    } catch (error) {
      toast.error("Failed to assign device")
    }
  }

  const openEditDialog = (device: Device) => {
    setSelectedDevice(device)
    setFormData({
      description: device.description,
      address: device.address,
      maximumHourlyConsumption: device.maximumHourlyConsumption,
      userId: device.userId || undefined,
    })
    setIsEditOpen(true)
  }

  const openDeleteDialog = (device: Device) => {
    setSelectedDevice(device)
    setIsDeleteOpen(true)
  }

  const openAssignDialog = (device: Device) => {
    setSelectedDevice(device)
    setSelectedUserId(device.userId || "")
    setIsAssignOpen(true)
  }

  return (
    <ProtectedRoute requiredRole="ADMIN">
      <DashboardLayout>
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-3xl font-bold tracking-tight">Devices</h2>
              <p className="text-muted-foreground">Manage connected devices and monitor their status</p>
            </div>
            <Button onClick={() => setIsCreateOpen(true)}>
              <Plus className="mr-2 h-4 w-4" />
              Add Device
            </Button>
          </div>

          <div className="rounded-lg border border-border bg-card">
            <Table>
              <TableHeader>
                <TableRow className="border-border hover:bg-muted/50">
                  <TableHead>Description</TableHead>
                  <TableHead>Address</TableHead>
                  <TableHead>Max Consumption (kWh)</TableHead>
                  <TableHead>Assigned User</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {devices?.map((device) => (
                  <TableRow key={device.id} className="border-border hover:bg-muted/50">
                    <TableCell className="font-medium">{device.description}</TableCell>
                    <TableCell>{device.address}</TableCell>
                    <TableCell>{device.maximumHourlyConsumption} kWh</TableCell>
                    <TableCell>
                      {device.username ? (
                        <span className="text-sm">{device.username}</span>
                      ) : (
                        <span className="text-sm text-muted-foreground">Unassigned</span>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => openAssignDialog(device)}>
                        <UserPlus className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" onClick={() => openEditDialog(device)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" onClick={() => openDeleteDialog(device)}>
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
                {!devices?.length && (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center text-muted-foreground">
                      No devices found
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
        </div>

        {/* Create Device Dialog */}
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogContent className="border-border bg-card">
            <DialogHeader>
              <DialogTitle>Create New Device</DialogTitle>
              <DialogDescription>Add a new device to the system</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="create-description">Description</Label>
                <Input
                  id="create-description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="bg-input border-border"
                  placeholder="Smart Meter Living Room"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="create-address">Address</Label>
                <Input
                  id="create-address"
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  className="bg-input border-border"
                  placeholder="123 Main St, Apt 4B"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="create-consumption">Maximum Hourly Consumption (kWh)</Label>
                <Input
                  id="create-consumption"
                  type="number"
                  step="0.1"
                  value={formData.maximumHourlyConsumption}
                  onChange={(e) =>
                    setFormData({ ...formData, maximumHourlyConsumption: Number.parseFloat(e.target.value) || 0 })
                  }
                  className="bg-input border-border"
                  placeholder="5.0"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="create-user">Assign to User (Optional)</Label>
                <Select
                  value={formData.userId || "none"}
                  onValueChange={(value) => setFormData({ ...formData, userId: value === "none" ? undefined : value })}
                >
                  <SelectTrigger className="bg-input border-border">
                    <SelectValue placeholder="Select a user" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">No user</SelectItem>
                    {users?.map((user) => (
                      <SelectItem key={user.id} value={user.id}>
                        {user.username} ({user.fullName})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreate}>Create Device</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Edit Device Dialog */}
        <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
          <DialogContent className="border-border bg-card">
            <DialogHeader>
              <DialogTitle>Edit Device</DialogTitle>
              <DialogDescription>Update device information</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="edit-description">Description</Label>
                <Input
                  id="edit-description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="bg-input border-border"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-address">Address</Label>
                <Input
                  id="edit-address"
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  className="bg-input border-border"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-consumption">Maximum Hourly Consumption (kWh)</Label>
                <Input
                  id="edit-consumption"
                  type="number"
                  step="0.1"
                  value={formData.maximumHourlyConsumption}
                  onChange={(e) =>
                    setFormData({ ...formData, maximumHourlyConsumption: Number.parseFloat(e.target.value) || 0 })
                  }
                  className="bg-input border-border"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-user">Assign to User</Label>
                <Select
                  value={formData.userId || "none"}
                  onValueChange={(value) => setFormData({ ...formData, userId: value === "none" ? undefined : value })}
                >
                  <SelectTrigger className="bg-input border-border">
                    <SelectValue placeholder="Select a user" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">No user</SelectItem>
                    {users?.map((user) => (
                      <SelectItem key={user.id} value={user.id}>
                        {user.username} ({user.fullName})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsEditOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleUpdate}>Update Device</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Assign Device Dialog */}
        <Dialog open={isAssignOpen} onOpenChange={setIsAssignOpen}>
          <DialogContent className="border-border bg-card">
            <DialogHeader>
              <DialogTitle>Assign Device to User</DialogTitle>
              <DialogDescription>Assign {selectedDevice?.description} to a user</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="assign-user">Select User</Label>
                <Select value={selectedUserId} onValueChange={setSelectedUserId}>
                  <SelectTrigger className="bg-input border-border">
                    <SelectValue placeholder="Select a user" />
                  </SelectTrigger>
                  <SelectContent>
                    {users?.map((user) => (
                      <SelectItem key={user.id} value={user.id}>
                        {user.username} ({user.fullName})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsAssignOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleAssign}>Assign Device</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Delete Device Dialog */}
        <Dialog open={isDeleteOpen} onOpenChange={setIsDeleteOpen}>
          <DialogContent className="border-border bg-card">
            <DialogHeader>
              <DialogTitle>Delete Device</DialogTitle>
              <DialogDescription>
                Are you sure you want to delete this device? This action cannot be undone.
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsDeleteOpen(false)}>
                Cancel
              </Button>
              <Button variant="destructive" onClick={handleDelete}>
                Delete
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </DashboardLayout>
    </ProtectedRoute>
  )
}
