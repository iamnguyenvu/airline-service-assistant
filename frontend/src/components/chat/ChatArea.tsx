'use client';

import { useState, useRef, useEffect, useCallback } from 'react';
import { Send, Paperclip, Mic } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { ScrollArea } from '@/components/ui/scroll-area';
import { ChatMessage } from './ChatMessage';
import { apiClient } from '@/lib/api/client';
import { ChatMessage as ChatMessageType } from '@/lib/api/types';
import { useToast } from '@/hooks/use-toast';

interface ChatAreaProps {
  onShowRightPanel: (content: {
    type: 'flights' | 'policy' | 'tips';
    data?: any;
  }) => void;
}

export function ChatArea({ onShowRightPanel }: ChatAreaProps) {
  const [messages, setMessages] = useState<ChatMessageType[]>([
    {
      id: '1',
      role: 'assistant',
      content:
        'Xin chào! Tôi là trợ lý AI của hãng hàng không. Tôi có thể giúp bạn:\n\n• Tìm kiếm chuyến bay\n• Tư vấn về chính sách hành lý\n• Tra cứu thông tin dịch vụ\n• Giải đáp thắc mắc\n\nBạn cần tôi hỗ trợ điều gì?',
      timestamp: new Date(),
    },
  ]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [sessionId] = useState(() => `session-${Date.now()}`);
  const scrollRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { toast } = useToast();

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSend = useCallback(async () => {
    if (!input.trim() || isTyping) return;

    const userMessage: ChatMessageType = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim(),
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    const currentInput = input.trim();
    setInput('');
    setIsTyping(true);

    try {
      const response = await apiClient.chatAsk(currentInput, sessionId, 'vi');
      
      const aiMessage: ChatMessageType = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
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
          onShowRightPanel({ type: 'flights', data: response.flightResults });
        } else if (lowerContent.includes('chuyến bay') || lowerContent.includes('flight')) {
          onShowRightPanel({ type: 'flights', data: { message: response.answer } });
        } else if (lowerContent.includes('chính sách') || lowerContent.includes('policy')) {
          onShowRightPanel({ type: 'policy', data: { message: response.answer } });
        }
      }
    } catch (error) {
      console.error('Chat error:', error);
      const errorMessage: ChatMessageType = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content:
          'Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
      toast({
        title: 'Lỗi',
        description: error instanceof Error ? error.message : 'Không thể kết nối đến server',
        variant: 'destructive',
      });
    } finally {
      setIsTyping(false);
    }
  }, [input, isTyping, sessionId, onShowRightPanel, toast]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <main className="flex flex-1 flex-col overflow-hidden">
      <div className="hidden md:flex h-14 items-center justify-between border-b px-6">
        <h1 className="font-semibold text-lg">Chat Tư Vấn</h1>
      </div>

      <ScrollArea className="flex-1 px-4 md:px-6 pb-20 md:pb-0" ref={scrollRef}>
        <div className="mx-auto max-w-3xl py-4 md:py-6 space-y-4 md:space-y-6">
          {messages.map((message) => (
            <ChatMessage
              key={message.id}
              message={message}
              onShowRightPanel={onShowRightPanel}
            />
          ))}
          {isTyping && (
            <div className="flex gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground">
                AI
              </div>
              <div className="flex items-center gap-1 rounded-2xl bg-muted px-4 py-3">
                <div className="h-2 w-2 animate-bounce rounded-full bg-foreground [animation-delay:-0.3s]"></div>
                <div className="h-2 w-2 animate-bounce rounded-full bg-foreground [animation-delay:-0.15s]"></div>
                <div className="h-2 w-2 animate-bounce rounded-full bg-foreground"></div>
              </div>
            </div>
          )}
        </div>
      </ScrollArea>

      <div className="border-t bg-background p-4">
        <div className="mx-auto max-w-3xl">
          <div className="relative flex items-end gap-2">
            <div className="flex-1 rounded-2xl border bg-card shadow-sm">
              <Textarea
                ref={textareaRef}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Nhập tin nhắn của bạn..."
                className="min-h-[56px] max-h-[200px] resize-none border-0 bg-transparent px-4 py-3 focus-visible:ring-0"
                rows={1}
              />
              <div className="flex items-center justify-between border-t px-3 py-2">
                <div className="flex gap-1">
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <Paperclip className="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <Mic className="h-4 w-4" />
                  </Button>
                </div>
                <span className="text-xs text-muted-foreground">
                  {input.length}/2000
                </span>
              </div>
            </div>
            <Button
              onClick={handleSend}
              disabled={!input.trim() || isTyping}
              size="icon"
              className="h-14 w-14 rounded-2xl"
            >
              <Send className="h-5 w-5" />
            </Button>
          </div>
          <p className="mt-2 text-center text-xs text-muted-foreground">
            AI có thể mắc lỗi. Vui lòng kiểm tra thông tin quan trọng.
          </p>
        </div>
      </div>
    </main>
  );
}
