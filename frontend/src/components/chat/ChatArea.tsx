"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { Send, Paperclip, Mic } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { ScrollArea } from "@/components/ui/scroll-area";
import { ChatMessage } from "./ChatMessage";
import { apiClient } from "@/lib/api/client";
import { ChatMessage as ChatMessageType } from "@/lib/api/types";
import { useToast } from "@/hooks/use-toast";

interface ChatAreaProps {
  onShowRightPanel: (content: { type: "flights" | "policy" | "tips"; data?: unknown }) => void;
  onNewChat?: () => void;
  newChatTrigger?: number;
}

export function ChatArea({ onShowRightPanel, newChatTrigger }: ChatAreaProps) {
  const welcomeMessage = "Xin chào! Tôi là trợ lý AI của hãng hàng không. Tôi có thể giúp bạn:\n\n• Tìm kiếm chuyến bay\n• Tư vấn về chính sách hành lý\n• Tra cứu thông tin dịch vụ\n• Giải đáp thắc mắc\n\nBạn cần tôi hỗ trợ điều gì?";
  
  const [messages, setMessages] = useState<ChatMessageType[]>([]);
  const [isTypingWelcome, setIsTypingWelcome] = useState(false);
  const [hasShownWelcome, setHasShownWelcome] = useState(false);
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [sessionId] = useState(() => `session-${Date.now()}`);
  const scrollRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { toast } = useToast();

  // Function to start new chat
  const handleNewChat = useCallback(() => {
    setMessages([]);
    setIsTypingWelcome(true);
    setHasShownWelcome(false);
    setInput("");
  }, []);

  // Reset chat when newChatTrigger changes
  useEffect(() => {
    if (newChatTrigger && newChatTrigger > 0) {
      handleNewChat();
    }
  }, [newChatTrigger, handleNewChat]);

  // Show welcome message with typing animation on mount or new chat
  useEffect(() => {
    if (!hasShownWelcome && messages.length === 0) {
      setIsTypingWelcome(true);
      setHasShownWelcome(true);
    }
  }, [hasShownWelcome, messages.length]);

  // Typing animation for welcome message
  useEffect(() => {
    if (isTypingWelcome && messages.length === 0) {
      let currentIndex = 0;
      const typingInterval = setInterval(() => {
        if (currentIndex < welcomeMessage.length) {
          setMessages([{
            id: "1",
            role: "assistant",
            content: welcomeMessage.substring(0, currentIndex + 1),
            timestamp: new Date(),
          }]);
          currentIndex++;
        } else {
          setIsTypingWelcome(false);
          clearInterval(typingInterval);
        }
      }, 30); // 30ms per character for smooth typing effect

      return () => clearInterval(typingInterval);
    }
  }, [isTypingWelcome, welcomeMessage, messages.length]);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, isTypingWelcome]);

  const handleSend = useCallback(async () => {
    if (!input.trim() || isTyping) return;

    const userMessage: ChatMessageType = {
      id: Date.now().toString(),
      role: "user",
      content: input.trim(),
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    const currentInput = input.trim();
    setInput("");
    setIsTyping(true);

    try {
      // Prepare conversation history (last 10 messages excluding current)
      const history = messages
        .slice(-10)
        .map(msg => ({
          role: msg.role,
          content: msg.content,
        }));
      
      const response = await apiClient.chatAsk(currentInput, sessionId, "vi", history);

      const aiMessage: ChatMessageType = {
        id: (Date.now() + 1).toString(),
        role: "assistant",
        content: response.answer,
        timestamp: new Date(),
        usedTools: response.usedTools,
        model: response.model,
      };

      setMessages((prev) => [...prev, aiMessage]);

      if (response.usedTools) {
        const lowerContent = response.answer.toLowerCase();
        if (response.flightResults) {
          // Show flight results from API
          onShowRightPanel({ type: "flights", data: response.flightResults });
        } else if (lowerContent.includes("chuyến bay") || lowerContent.includes("flight")) {
          onShowRightPanel({ type: "flights", data: { message: response.answer } });
        } else if (lowerContent.includes("chính sách") || lowerContent.includes("policy")) {
          onShowRightPanel({ type: "policy", data: { message: response.answer } });
        }
      }
    } catch (error) {
      console.error("Chat error:", error);
      const errorMessage: ChatMessageType = {
        id: (Date.now() + 1).toString(),
        role: "assistant",
        content: "Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
      toast({
        title: "Lỗi",
        description: error instanceof Error ? error.message : "Không thể kết nối đến server",
        variant: "destructive",
      });
    } finally {
      setIsTyping(false);
    }
  }, [input, isTyping, sessionId, onShowRightPanel, toast]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <main className="flex flex-1 flex-col overflow-hidden">
      <div className="hidden h-14 items-center justify-between border-b px-6 md:flex">
        <h1 className="text-lg font-semibold">Chat Tư Vấn</h1>
      </div>

      <ScrollArea className="flex-1 px-4 pb-20 md:px-6 md:pb-0" ref={scrollRef}>
        <div className="mx-auto max-w-3xl space-y-4 py-4 md:space-y-6 md:py-6">
          {messages.map((message) => (
            <ChatMessage key={message.id} message={message} onShowRightPanel={onShowRightPanel} />
          ))}
          {isTyping && (
            <div className="flex gap-3">
              <div className="bg-primary text-primary-foreground flex h-8 w-8 items-center justify-center rounded-full">
                AI
              </div>
              <div className="bg-muted flex items-center gap-1 rounded-2xl px-4 py-3">
                <div className="bg-foreground h-2 w-2 animate-bounce rounded-full [animation-delay:-0.3s]"></div>
                <div className="bg-foreground h-2 w-2 animate-bounce rounded-full [animation-delay:-0.15s]"></div>
                <div className="bg-foreground h-2 w-2 animate-bounce rounded-full"></div>
              </div>
            </div>
          )}
        </div>
      </ScrollArea>

      <div className="bg-background border-t p-4">
        <div className="mx-auto max-w-3xl">
          <div className="flex items-center gap-2">
            <div className="bg-card flex flex-1 items-center gap-2 rounded-2xl border px-3 py-2 shadow-sm">
              <div className="flex items-center gap-1">
                <Button variant="ghost" size="icon" className="h-8 w-8">
                  <Paperclip className="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon" className="h-8 w-8">
                  <Mic className="h-4 w-4" />
                </Button>
              </div>

              <Textarea
                ref={textareaRef}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Nhập câu hỏi hoặc yêu cầu của bạn..."
                className="placeholder:text-muted-foreground/60 max-h-[200px] min-h-11 flex-1 resize-none 
                !border-0 border-0 bg-transparent p-0 !ring-0 !ring-offset-0 focus:border-transparent focus:ring-0 
                focus:outline-none focus-visible:ring-0 focus-visible:ring-offset-0 focus-visible:outline-none"
                rows={1}
              />

              <span className="text-muted-foreground shrink-0 text-xs">{input.length}/2000</span>
            </div>

            <Button
              onClick={handleSend}
              disabled={!input.trim() || isTyping}
              size="icon"
              className="h-12 w-14 shrink-0 rounded-2xl"
              aria-label="Gửi"
            >
              <Send className="h-5 w-5" />
            </Button>
          </div>

          <p className="text-muted-foreground mt-2 text-center text-xs">
            AI có thể mắc lỗi. Vui lòng kiểm tra thông tin quan trọng.
          </p>
        </div>
      </div>
    </main>
  );
}
