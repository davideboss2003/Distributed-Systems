"use client";

import { useState, useRef, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Send, User, Users, MessageCircle } from "lucide-react";
import { useWebSocketChat, ChatMessage as WSChatMessage } from "@/hooks/use-websocket-chat";

interface Message {
    id: string;
    content: string;
    fromUserId: string;
    toUserId: string;
    isAdmin: boolean;
    timestamp: Date;
}

interface UserConversation {
    userId: string;
    lastMessage: string;
    unreadCount: number;
    timestamp: Date;
}

export default function AdminChatPage() {
    const [selectedUser, setSelectedUser] = useState<string | null>(null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [conversations, setConversations] = useState<UserConversation[]>([]);
    const [inputValue, setInputValue] = useState("");
    const scrollAreaRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    // WebSocket for admin - always enabled on this page
    const { isConnected, sendMessage: sendWSMessage } = useWebSocketChat({
        userId: "admin-user", // Unique admin identifier
        isAdmin: true,
        enabled: true, // Always enabled on admin chat page
        onMessageReceived: (message: WSChatMessage) => {
            const newMessage: Message = {
                id: Date.now().toString(),
                content: message.message,
                fromUserId: message.fromUserId,
                toUserId: message.toUserId,
                isAdmin: message.isAdmin,
                timestamp: new Date(message.timestamp),
            };

            setMessages((prev) => [...prev, newMessage]);

            // Update conversations list
            setConversations((prev) => {
                const existingIndex = prev.findIndex(c => c.userId === message.fromUserId);
                const newConv: UserConversation = {
                    userId: message.fromUserId,
                    lastMessage: message.message,
                    unreadCount: selectedUser === message.fromUserId ? 0 : 1,
                    timestamp: new Date(message.timestamp),
                };

                if (existingIndex >= 0) {
                    const updated = [...prev];
                    updated[existingIndex] = {
                        ...updated[existingIndex],
                        lastMessage: message.message,
                        unreadCount: selectedUser === message.fromUserId ? 0 : updated[existingIndex].unreadCount + 1,
                        timestamp: new Date(message.timestamp),
                    };
                    return updated;
                }
                return [newConv, ...prev];
            });
        },
    });

    // Auto-scroll to bottom
    useEffect(() => {
        if (scrollAreaRef.current) {
            scrollAreaRef.current.scrollTop = scrollAreaRef.current.scrollHeight;
        }
    }, [messages]);

    const sendMessage = () => {
        if (!inputValue.trim() || !selectedUser) return;

        const message: Message = {
            id: Date.now().toString(),
            content: inputValue.trim(),
            fromUserId: "admin1",
            toUserId: selectedUser,
            isAdmin: true,
            timestamp: new Date(),
        };

        setMessages((prev) => [...prev, message]);
        sendWSMessage(selectedUser, inputValue.trim());
        setInputValue("");
        inputRef.current?.focus();
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const selectUser = (userId: string) => {
        setSelectedUser(userId);
        // Mark as read
        setConversations((prev) =>
            prev.map((c) => (c.userId === userId ? { ...c, unreadCount: 0 } : c))
        );
    };

    return (
        <div className="container mx-auto p-6 h-[calc(100vh-8rem)]">
            <div className="flex gap-6 h-full">
                {/* Sidebar - User List */}
                <Card className="w-80 border-cyan-500/20 bg-black/40 backdrop-blur-xl">
                    <CardHeader className="border-b border-cyan-500/20">
                        <CardTitle className="text-lg flex items-center gap-2">
                            <Users className="w-5 h-5 text-cyan-400" />
                            Active Conversations
                        </CardTitle>
                        <div className="flex items-center gap-2 text-sm text-gray-400">
                            <div className={`w-2 h-2 rounded-full ${isConnected ? "bg-green-500" : "bg-red-500"}`} />
                            {isConnected ? "Connected" : "Disconnected"}
                        </div>
                    </CardHeader>
                    <CardContent className="p-0">
                        <ScrollArea className="h-[calc(100vh-16rem)]">
                            {conversations.length === 0 ? (
                                <div className="p-6 text-center text-gray-400">
                                    <MessageCircle className="w-12 h-12 mx-auto mb-3 opacity-50" />
                                    <p className="text-sm">No active conversations</p>
                                </div>
                            ) : (
                                <div className="divide-y divide-white/5">
                                    {conversations.map((conv) => (
                                        <button
                                            key={conv.userId}
                                            onClick={() => selectUser(conv.userId)}
                                            className={`w-full p-4 text-left hover:bg-white/5 transition-colors ${selectedUser === conv.userId ? "bg-cyan-500/10 border-l-2 border-cyan-500" : ""
                                                }`}
                                        >
                                            <div className="flex items-start justify-between">
                                                <div className="flex items-center gap-3">
                                                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-cyan-500 to-purple-600 flex items-center justify-center">
                                                        <User className="w-5 h-5 text-white" />
                                                    </div>
                                                    <div>
                                                        <div className="font-medium text-white">{conv.userId}</div>
                                                        <div className="text-xs text-gray-400 truncate max-w-[150px]">
                                                            {conv.lastMessage}
                                                        </div>
                                                    </div>
                                                </div>
                                                {conv.unreadCount > 0 && (
                                                    <Badge className="bg-cyan-500 text-white">
                                                        {conv.unreadCount}
                                                    </Badge>
                                                )}
                                            </div>
                                            <div className="text-[10px] text-gray-500 mt-1">
                                                {conv.timestamp.toLocaleTimeString()}
                                            </div>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </ScrollArea>
                    </CardContent>
                </Card>

                {/* Chat Area */}
                <Card className="flex-1 flex flex-col border-cyan-500/20 bg-black/40 backdrop-blur-xl overflow-hidden">
                    {selectedUser ? (
                        <>
                            <CardHeader className="border-b border-cyan-500/20 py-3">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-cyan-500 to-purple-600 flex items-center justify-center">
                                            <User className="w-5 h-5 text-white" />
                                        </div>
                                        <div>
                                            <CardTitle className="text-lg">{selectedUser}</CardTitle>
                                            <p className="text-xs text-gray-400">Client</p>
                                        </div>
                                    </div>
                                    <Badge variant="outline" className="border-green-500/50 text-green-400 bg-green-500/10">
                                        Active
                                    </Badge>
                                </div>
                            </CardHeader>

                            <CardContent className="flex-1 flex flex-col p-0 overflow-hidden">
                                {/* Messages */}
                                <ScrollArea className="flex-1 p-4" ref={scrollAreaRef}>
                                    <div className="space-y-4">
                                        {messages
                                            .filter((m) => m.fromUserId === selectedUser || m.toUserId === selectedUser)
                                            .map((message) => (
                                                <div
                                                    key={message.id}
                                                    className={`flex ${message.isAdmin ? "justify-end" : "justify-start"}`}
                                                >
                                                    <div
                                                        className={`max-w-[70%] rounded-2xl px-4 py-3 ${message.isAdmin
                                                            ? "bg-gradient-to-r from-purple-600 to-purple-500 text-white"
                                                            : "bg-white/5 border border-white/10 text-gray-200"
                                                            }`}
                                                    >
                                                        <div className="text-sm whitespace-pre-wrap">{message.content}</div>
                                                        <div className="text-[10px] opacity-50 mt-1 text-right">
                                                            {message.timestamp.toLocaleTimeString()}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                    </div>
                                </ScrollArea>

                                {/* Input */}
                                <div className="p-4 border-t border-cyan-500/20 bg-black/20">
                                    <div className="flex gap-3">
                                        <Input
                                            ref={inputRef}
                                            value={inputValue}
                                            onChange={(e) => setInputValue(e.target.value)}
                                            onKeyPress={handleKeyPress}
                                            placeholder="Type your message..."
                                            className="bg-white/5 border-white/10 focus:border-cyan-500/50 text-white placeholder:text-gray-500"
                                        />
                                        <Button
                                            onClick={sendMessage}
                                            disabled={!inputValue.trim()}
                                            className="bg-gradient-to-r from-purple-600 to-purple-500 hover:from-purple-500 hover:to-purple-400 text-white px-6"
                                        >
                                            <Send className="w-4 h-4" />
                                        </Button>
                                    </div>
                                </div>
                            </CardContent>
                        </>
                    ) : (
                        <div className="flex-1 flex items-center justify-center text-gray-400">
                            <div className="text-center">
                                <MessageCircle className="w-16 h-16 mx-auto mb-4 opacity-50" />
                                <p>Select a conversation to start chatting</p>
                            </div>
                        </div>
                    )}
                </Card>
            </div>
        </div>
    );
}
