"use client";

import { useEffect, useRef, useState, useCallback } from "react";

export interface ChatMessage {
  fromUserId: string;
  toUserId: string;
  message: string;
  isAdmin: boolean;
  timestamp: string;
}

interface UseWebSocketChatProps {
  userId: string;
  isAdmin?: boolean;
  enabled?: boolean; // New prop to control when to connect
  onMessageReceived?: (message: ChatMessage) => void;
}

export function useWebSocketChat({ userId, isAdmin = false, enabled = true, onMessageReceived }: UseWebSocketChatProps) {
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);
  const reconnectAttemptsRef = useRef(0);
  const isClosingRef = useRef(false);
  const maxReconnectAttempts = 3;

  const disconnect = useCallback(() => {
    isClosingRef.current = true;
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = undefined;
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    setIsConnected(false);
    reconnectAttemptsRef.current = 0;
  }, []);

  const connect = useCallback(() => {
    if (!enabled || isClosingRef.current) return;
    
    // Don't connect if already connected
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      return;
    }

    try {
      const wsHost = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
      const wsUrl = isAdmin
        ? `ws://${wsHost}:8085/ws/chat?adminId=${userId}`
        : `ws://${wsHost}:8085/ws/chat?userId=${userId}`;

      console.log(`Connecting to WebSocket: ${wsUrl}`);
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log("WebSocket connected");
        setIsConnected(true);
        setError(null);
        reconnectAttemptsRef.current = 0;
      };

      ws.onmessage = (event) => {
        try {
          const message: ChatMessage = JSON.parse(event.data);
          console.log("Received message:", message);
          if (onMessageReceived) {
            onMessageReceived(message);
          }
        } catch (err) {
          console.error("Error parsing message:", err);
        }
      };

      ws.onerror = (event) => {
        console.error("WebSocket error:", event);
        setError("Connection error");
      };

      ws.onclose = () => {
        console.log("WebSocket disconnected");
        setIsConnected(false);
        wsRef.current = null;

        // Only auto-reconnect if not intentionally closing and enabled
        if (!isClosingRef.current && enabled && reconnectAttemptsRef.current < maxReconnectAttempts) {
          reconnectAttemptsRef.current++;
          const delay = Math.min(2000 * reconnectAttemptsRef.current, 10000);
          console.log(`Reconnecting in ${delay}ms (attempt ${reconnectAttemptsRef.current})`);
          
          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, delay);
        } else if (reconnectAttemptsRef.current >= maxReconnectAttempts) {
          setError("Failed to connect after multiple attempts");
        }
      };

      wsRef.current = ws;
    } catch (err) {
      console.error("Error creating WebSocket:", err);
      setError("Failed to create connection");
    }
  }, [userId, isAdmin, enabled, onMessageReceived]);

  const sendMessage = useCallback((toUserId: string, message: string) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      const chatMessage: ChatMessage = {
        fromUserId: userId,
        toUserId,
        message,
        isAdmin: isAdmin,
        timestamp: new Date().toISOString(),
      };
      
      wsRef.current.send(JSON.stringify(chatMessage));
      console.log("Sent message:", chatMessage);
      return true;
    } else {
      console.error("WebSocket is not connected");
      setError("Not connected");
      return false;
    }
  }, [userId, isAdmin]);

  useEffect(() => {
    isClosingRef.current = false;
    
    if (enabled) {
      connect();
    } else {
      disconnect();
    }

    return () => {
      disconnect();
    };
  }, [enabled]); // Only re-run when enabled changes

  return {
    isConnected,
    error,
    sendMessage,
    disconnect,
    reconnect: connect,
  };
}
