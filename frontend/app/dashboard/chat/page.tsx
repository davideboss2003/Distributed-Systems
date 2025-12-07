"use client";

import { useState, useRef, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Send, Bot, User, Sparkles, MessageSquare, Cpu, Users } from "lucide-react";
import { useWebSocketChat, ChatMessage as WSChatMessage } from "@/hooks/use-websocket-chat";
import { WallEMascot } from "@/components/wall-e-mascot";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost";

interface Message {
    id: string;
    content: string;
    isUser: boolean;
    isRuleBased?: boolean;
    ruleName?: string;
    timestamp: Date;
}

export default function ChatPage() {
    const [chatMode, setChatMode] = useState<"bot" | "admin">("bot");
    const [messages, setMessages] = useState<Message[]>([
        {
            id: "welcome",
            content: "🤖 Welcome to Energy Support AI! I'm here to help you with devices, consumption monitoring, billing, and more. Type 'help' to see what I can do!",
            isUser: false,
            isRuleBased: true,
            ruleName: "System",
            timestamp: new Date(),
        },
    ]);
    const [inputValue, setInputValue] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [waitingForAdmin, setWaitingForAdmin] = useState(false);
    const scrollAreaRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    // WebSocket for admin chat - only connect when in admin mode
    const { isConnected, sendMessage: sendWSMessage } = useWebSocketChat({
        userId: "client-user", // Unique client identifier
        isAdmin: false,
        enabled: chatMode === "admin", // Only connect when in admin mode
        onMessageReceived: (message: WSChatMessage) => {
            const newMessage: Message = {
                id: Date.now().toString(),
                content: message.message,
                isUser: false,
                isRuleBased: false,
                ruleName: "Admin",
                timestamp: new Date(message.timestamp),
            };
            setMessages((prev) => [...prev, newMessage]);
            setWaitingForAdmin(false);
        },
    });

    // Auto-scroll to bottom when new messages arrive
    useEffect(() => {
        if (scrollAreaRef.current) {
            scrollAreaRef.current.scrollTop = scrollAreaRef.current.scrollHeight;
        }
    }, [messages]);

    const sendMessage = async () => {
        if (!inputValue.trim() || isLoading) return;

        const userMessage: Message = {
            id: Date.now().toString(),
            content: inputValue.trim(),
            isUser: true,
            timestamp: new Date(),
        };

        setMessages((prev) => [...prev, userMessage]);
        setInputValue("");

        if (chatMode === "admin") {
            // Send via WebSocket to admin
            setWaitingForAdmin(true);
            const success = sendWSMessage("admin", userMessage.content);
            if (!success) {
                const errorMessage: Message = {
                    id: (Date.now() + 1).toString(),
                    content: "⚠️ Failed to connect to admin. Please try bot mode.",
                    isUser: false,
                    isRuleBased: false,
                    ruleName: "Error",
                    timestamp: new Date(),
                };
                setMessages((prev) => [...prev, errorMessage]);
                setWaitingForAdmin(false);
            }
        } else {
            // Bot mode - use existing API
            setIsLoading(true);
            try {
                const response = await fetch(`${API_BASE_URL}/api/chat/message`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ message: userMessage.content }),
                });

                if (!response.ok) {
                    throw new Error("Failed to get response");
                }

                const data = await response.json();

                const botMessage: Message = {
                    id: (Date.now() + 1).toString(),
                    content: data.response,
                    isUser: false,
                    isRuleBased: data.isRuleBased,
                    ruleName: data.ruleName,
                    timestamp: new Date(),
                };

                setMessages((prev) => [...prev, botMessage]);
            } catch (error) {
                console.error("Error sending message:", error);
                const errorMessage: Message = {
                    id: (Date.now() + 1).toString(),
                    content: "⚠️ Sorry, I couldn't process your request. Please try again.",
                    isUser: false,
                    isRuleBased: false,
                    ruleName: "Error",
                    timestamp: new Date(),
                };
                setMessages((prev) => [...prev, errorMessage]);
            } finally {
                setIsLoading(false);
                inputRef.current?.focus();
            }
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const quickActions = [
        { label: "Help", message: "help" },
        { label: "Devices", message: "how to add a device?" },
        { label: "Consumption", message: "how to view consumption?" },
        { label: "Alerts", message: "what is overconsumption?" },
    ];

    return (
        <div className="container mx-auto p-6 h-[calc(100vh-8rem)]">
            <div className="flex flex-col h-full gap-6">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="relative">
                            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-cyan-500 to-purple-600 flex items-center justify-center">
                                {chatMode === "bot" ? (
                                    <Bot className="w-6 h-6 text-white" />
                                ) : (
                                    <Users className="w-6 h-6 text-white" />
                                )}
                            </div>
                            <div className={`absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 border-black animate-pulse ${chatMode === "admin" && isConnected ? "bg-green-500" :
                                chatMode === "admin" ? "bg-yellow-500" : "bg-green-500"
                                }`} />
                        </div>
                        <div>
                            <h1 className="text-2xl font-bold bg-gradient-to-r from-cyan-400 to-purple-500 bg-clip-text text-transparent">
                                {chatMode === "bot" ? "Energy Support AI" : "Admin Chat"}
                            </h1>
                            <p className="text-sm text-gray-400 flex items-center gap-1">
                                {chatMode === "bot" ? (
                                    <>
                                        <Cpu className="w-3 h-3" />
                                        Neural Network Online • 11 Rules Active
                                    </>
                                ) : (
                                    <>
                                        <Users className="w-3 h-3" />
                                        {isConnected ? "Connected to Admin" : "Connecting..."}
                                    </>
                                )}
                            </p>
                        </div>
                    </div>
                    <div className="flex items-center gap-3">
                        {/* Mode Toggle */}
                        <div className="flex gap-2 bg-black/40 p-1 rounded-lg border border-white/10">
                            <Button
                                size="sm"
                                variant={chatMode === "bot" ? "default" : "ghost"}
                                onClick={() => setChatMode("bot")}
                                className={chatMode === "bot" ? "bg-cyan-600 hover:bg-cyan-500" : ""}
                            >
                                <Bot className="w-4 h-4 mr-1" />
                                Bot
                            </Button>
                            <Button
                                size="sm"
                                variant={chatMode === "admin" ? "default" : "ghost"}
                                onClick={() => setChatMode("admin")}
                                className={chatMode === "admin" ? "bg-purple-600 hover:bg-purple-500" : ""}
                            >
                                <Users className="w-4 h-4 mr-1" />
                                Admin
                            </Button>
                        </div>
                        <Badge variant="outline" className="border-cyan-500/50 text-cyan-400 bg-cyan-500/10">
                            <Sparkles className="w-3 h-3 mr-1" />
                            {chatMode === "bot" ? "Rule-Based AI" : "Live Chat"}
                        </Badge>
                    </div>
                </div>

                {/* Chat Container */}
                <Card className="flex-1 flex flex-col border-cyan-500/20 bg-black/40 backdrop-blur-xl overflow-hidden">
                    <CardHeader className="border-b border-cyan-500/20 py-3">
                        <CardTitle className="text-sm font-medium text-gray-400 flex items-center gap-2">
                            <MessageSquare className="w-4 h-4" />
                            Chat Session
                        </CardTitle>
                    </CardHeader>

                    <CardContent className="flex-1 flex flex-col p-0 overflow-hidden">
                        {/* Messages Area */}
                        <ScrollArea className="flex-1 p-4" ref={scrollAreaRef}>
                            <div className="space-y-4">
                                {messages.map((message) => (
                                    <div
                                        key={message.id}
                                        className={`flex ${message.isUser ? "justify-end" : "justify-start"}`}
                                    >
                                        <div
                                            className={`max-w-[80%] rounded-2xl px-4 py-3 ${message.isUser
                                                ? "bg-gradient-to-r from-cyan-600 to-cyan-500 text-white"
                                                : "bg-white/5 border border-white/10 text-gray-200"
                                                }`}
                                        >
                                            {/* Message Header */}
                                            <div className="flex items-center gap-2 mb-1">
                                                {message.isUser ? (
                                                    <User className="w-4 h-4" />
                                                ) : (
                                                    <Bot className="w-4 h-4 text-cyan-400" />
                                                )}
                                                <span className="text-xs opacity-70">
                                                    {message.isUser ? "You" : "AI Assistant"}
                                                </span>
                                                {!message.isUser && message.ruleName && (
                                                    <Badge
                                                        variant="outline"
                                                        className={`text-[10px] px-1.5 py-0 flex items-center gap-1 ${message.isRuleBased
                                                            ? "border-green-500/50 text-green-400 bg-green-500/10"
                                                            : message.ruleName === "GeminiAI"
                                                                ? "border-blue-500/50 text-blue-400 bg-blue-500/10"
                                                                : "border-purple-500/50 text-purple-400 bg-purple-500/10"
                                                            }`}
                                                    >
                                                        {message.isRuleBased ? (
                                                            "Rule"
                                                        ) : message.ruleName === "GeminiAI" ? (
                                                            <>
                                                                <Sparkles className="w-2.5 h-2.5" />
                                                                AI
                                                            </>
                                                        ) : (
                                                            "Default"
                                                        )}
                                                    </Badge>
                                                )}
                                            </div>

                                            {/* Message Content */}
                                            <div className="text-sm whitespace-pre-wrap leading-relaxed">
                                                {message.content}
                                            </div>

                                            {/* Timestamp */}
                                            <div className="text-[10px] opacity-50 mt-2 text-right">
                                                {message.timestamp.toLocaleTimeString()}
                                            </div>
                                        </div>
                                    </div>
                                ))}

                                {/* Loading indicator */}
                                {(isLoading || waitingForAdmin) && (
                                    <div className="flex justify-start">
                                        <div className="bg-white/5 border border-white/10 rounded-2xl px-4 py-3">
                                            <div className="flex items-center gap-2">
                                                {chatMode === "bot" ? (
                                                    <Bot className="w-4 h-4 text-cyan-400" />
                                                ) : (
                                                    <Users className="w-4 h-4 text-purple-400" />
                                                )}
                                                <div className="flex gap-1">
                                                    <div className="w-2 h-2 bg-cyan-400 rounded-full animate-bounce" style={{ animationDelay: "0ms" }} />
                                                    <div className="w-2 h-2 bg-cyan-400 rounded-full animate-bounce" style={{ animationDelay: "150ms" }} />
                                                    <div className="w-2 h-2 bg-cyan-400 rounded-full animate-bounce" style={{ animationDelay: "300ms" }} />
                                                </div>
                                                {waitingForAdmin && (
                                                    <span className="text-xs text-gray-400 ml-2">
                                                        Waiting for admin response...
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </ScrollArea>

                        {/* Quick Actions - Only show in bot mode */}
                        {chatMode === "bot" && (
                            <div className="px-4 py-2 border-t border-white/5">
                                <div className="flex gap-2 flex-wrap">
                                    {quickActions.map((action) => (
                                        <Button
                                            key={action.label}
                                            variant="outline"
                                            size="sm"
                                            onClick={() => {
                                                setInputValue(action.message);
                                                inputRef.current?.focus();
                                            }}
                                            className="text-xs border-white/10 bg-white/5 hover:bg-white/10 hover:border-cyan-500/50 text-gray-400 hover:text-cyan-400"
                                        >
                                            {action.label}
                                        </Button>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Input Area */}
                        <div className="p-4 border-t border-cyan-500/20 bg-black/20">
                            <div className="flex gap-3">
                                <div className="flex-1 relative">
                                    <Input
                                        ref={inputRef}
                                        value={inputValue}
                                        onChange={(e) => setInputValue(e.target.value)}
                                        onKeyPress={handleKeyPress}
                                        placeholder="Type your message..."
                                        disabled={isLoading}
                                        className="bg-white/5 border-white/10 focus:border-cyan-500/50 text-white placeholder:text-gray-500 pr-12"
                                    />
                                    <div className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-500">
                                        ↵
                                    </div>
                                </div>
                                <Button
                                    onClick={sendMessage}
                                    disabled={!inputValue.trim() || isLoading}
                                    className="bg-gradient-to-r from-cyan-600 to-cyan-500 hover:from-cyan-500 hover:to-cyan-400 text-white px-6"
                                >
                                    <Send className="w-4 h-4" />
                                </Button>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Wall-E Mascot - Only on chat page */}
            <WallEMascot />
        </div>
    );
}
