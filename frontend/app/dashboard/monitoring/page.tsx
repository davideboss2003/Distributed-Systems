"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { CalendarIcon, BarChart3, TrendingUp } from "lucide-react";
import { format } from "date-fns";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { api } from "@/lib/api";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost";

interface Device {
  id: string;
  description: string;
  address: string;
  maxHourlyConsumption: number;
}

interface ConsumptionData {
  timestamp: string;
  consumption: number;
  measurementCount: number;
}

export default function MonitoringPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedDevice, setSelectedDevice] = useState<string>("");
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());
  const [consumptionData, setConsumptionData] = useState<ConsumptionData[]>([]);
  const [chartType, setChartType] = useState<"line" | "bar">("line");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mounted, setMounted] = useState(false);

  // Fix hydration issue
  useEffect(() => {
    setMounted(true);
  }, []);

  // Fetch devices on mount
  useEffect(() => {
    if (mounted) {
      fetchDevices();
    }
  }, [mounted]); // eslint-disable-line react-hooks/exhaustive-deps

  const fetchDevices = async () => {
    try {
      const data = await api.getAllDevices();
      console.log("Devices loaded in monitoring page:", data);
      setDevices(data);
      if (data.length > 0 && !selectedDevice) {
        setSelectedDevice(data[0].id);
      }
    } catch (err) {
      console.error("Error fetching devices:", err);
      setError("Failed to load devices");
    }
  };

  const fetchConsumptionData = async () => {
    console.log("=== fetchConsumptionData called ===");
    console.log("selectedDevice:", selectedDevice);
    console.log("selectedDate:", selectedDate);
    console.log("selectedDate type:", typeof selectedDate);
    
    if (!selectedDevice || !selectedDate) {
      console.log("No device or date selected - RETURNING");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem("token");
      const startDate = new Date(selectedDate);
      console.log("startDate before setHours:", startDate);
      startDate.setHours(0, 0, 0, 0);
      console.log("startDate after setHours:", startDate);
      
      const endDate = new Date(selectedDate);
      endDate.setHours(23, 59, 59, 999);
      console.log("endDate:", endDate);

      const url = `${API_BASE_URL}/api/monitoring/devices/${selectedDevice}/consumption?startDate=${startDate.toISOString()}&endDate=${endDate.toISOString()}`;
      console.log("=== FETCHING FROM URL ===");
      console.log(url);

      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log("Response status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        throw new Error(`Failed to fetch consumption data: ${response.status}`);
      }
      
      const data: ConsumptionData[] = await response.json();
      console.log("Consumption data received:", data);
      setConsumptionData(data);
    } catch (err) {
      console.error("Error fetching consumption:", err);
      setError("Failed to load consumption data");
    } finally {
      setLoading(false);
    }
  };

  // Transform data for chart (group by hour)
  const chartData = consumptionData.map((item) => {
    const date = new Date(item.timestamp);
    return {
      hour: date.getHours(),
      consumption: item.consumption,
      time: `${date.getHours()}:00`,
    };
  });

  // Calculate statistics
  const totalConsumption = consumptionData.reduce((sum, item) => sum + item.consumption, 0);
  const avgConsumption = consumptionData.length > 0 ? totalConsumption / consumptionData.length : 0;
  const maxConsumption = Math.max(...consumptionData.map((item) => item.consumption), 0);

  const selectedDeviceInfo = devices.find((d) => d.id === selectedDevice);

  if (!mounted) {
    return <div className="container mx-auto p-6">Loading...</div>;
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Energy Consumption Monitoring</h1>
          <p className="text-muted-foreground">View historical energy consumption data</p>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Select device and date to view consumption data</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Device Selector */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Device</label>
              <Select value={selectedDevice} onValueChange={setSelectedDevice}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a device" />
                </SelectTrigger>
                <SelectContent>
                  {devices.map((device) => (
                    <SelectItem key={device.id} value={device.id}>
                      {device.description} - {device.address}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Date Picker */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Date</label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-full justify-start text-left font-normal">
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {selectedDate ? format(selectedDate, "PPP") : "Pick a date"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <Calendar mode="single" selected={selectedDate} onSelect={setSelectedDate} initialFocus />
                </PopoverContent>
              </Popover>
            </div>

            {/* Chart Type */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Chart Type</label>
              <Select value={chartType} onValueChange={(value: "line" | "bar") => setChartType(value)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="line">Line Chart</SelectItem>
                  <SelectItem value="bar">Bar Chart</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <Button onClick={fetchConsumptionData} disabled={loading || !selectedDevice} className="w-full md:w-auto">
            {loading ? "Loading..." : "Load Data"}
          </Button>
        </CardContent>
      </Card>

      {/* Statistics */}
      {consumptionData.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Consumption</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{totalConsumption.toFixed(2)} kWh</div>
              <p className="text-xs text-muted-foreground">For selected day</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Average Consumption</CardTitle>
              <BarChart3 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{avgConsumption.toFixed(2)} kWh</div>
              <p className="text-xs text-muted-foreground">Per hour</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Peak Consumption</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{maxConsumption.toFixed(2)} kWh</div>
              <p className="text-xs text-muted-foreground">Maximum hourly</p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Energy Consumption Chart</CardTitle>
          <CardDescription>
            {selectedDeviceInfo && selectedDate && `${selectedDeviceInfo.description} - ${format(selectedDate, "PPP")}`}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="text-center py-8 text-red-500">
              <p>{error}</p>
            </div>
          )}

          {!error && consumptionData.length === 0 && !loading && (
            <div className="text-center py-12 text-muted-foreground">
              <BarChart3 className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No data available for the selected date</p>
              <p className="text-sm">Try selecting a different date or device</p>
            </div>
          )}

          {!error && consumptionData.length > 0 && (
            <ResponsiveContainer width="100%" height={400}>
              {chartType === "line" ? (
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" label={{ value: "Hour", position: "insideBottom", offset: -5 }} />
                  <YAxis label={{ value: "Energy (kWh)", angle: -90, position: "insideLeft" }} />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="consumption" stroke="#8884d8" strokeWidth={2} name="Consumption (kWh)" />
                </LineChart>
              ) : (
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" label={{ value: "Hour", position: "insideBottom", offset: -5 }} />
                  <YAxis label={{ value: "Energy (kWh)", angle: -90, position: "insideLeft" }} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="consumption" fill="#8884d8" name="Consumption (kWh)" />
                </BarChart>
              )}
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
