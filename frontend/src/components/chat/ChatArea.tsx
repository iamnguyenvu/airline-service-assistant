'use client';

import { useState, useRef, useEffect } from 'react';
import { Send, Paperclip, Mic, StopCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { ScrollArea } from '@/components/ui/scroll-area';
import { ChatMessage } from './ChatMessage';
import { cn } from '@/lib/utils';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

interface ChatAreaProps {
  onShowRightPanel: (content: {
    type: 'flights' | 'policy' | 'tips';
    data?: any;
  }) => void;
}

export function ChatArea({ onShowRightPanel }: ChatAreaProps) {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      role: 'assistant',
      content:
        'Xin chào! Tôi là trợ lý AI của hãng hàng không. Tôi có thể giúp bạn:\n\n• Tìm kiếm và đặt vé máy bay\n• Kiểm tra thông tin chuyến bay\n• Tư vấn về hành lý và quy định\n• Giải đáp thắc mắc về dịch vụ\n\nBạn cần tôi hỗ trợ điều gì?',
      timestamp: new Date(),
    },
  ]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: input,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsTyping(true);

    setTimeout(() => {
      const aiMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content:
          'Tôi đã hiểu yêu cầu của bạn. Để tôi tìm kiếm các chuyến bay phù hợp nhất cho bạn...',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, aiMessage]);
      setIsTyping(false);
    }, 1500);
  };

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
