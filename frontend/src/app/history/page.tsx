'use client';

import { useState } from 'react';
import { Search, Clock, Trash2, MoreVertical } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { PageLayout } from '@/components/layout/PageLayout';

export default function HistoryPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [conversations] = useState<Array<{
    id: string;
    title: string;
    preview: string;
    date: string;
    messageCount: number;
  }>>([]);

  return (
    <PageLayout>
      <div className="flex h-screen flex-col">
        <div className="border-b bg-card p-4">
          <h1 className="mb-4 text-2xl font-semibold">Lịch Sử Chat</h1>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm cuộc hội thoại..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <ScrollArea className="flex-1">
          <div className="p-4 space-y-3">
            {conversations.length === 0 ? (
              <Card>
                <CardContent className="p-8 text-center text-muted-foreground">
                  <p>Chưa có lịch sử chat nào.</p>
                  <p className="text-sm mt-2">Các cuộc hội thoại sẽ được lưu ở đây.</p>
                </CardContent>
              </Card>
            ) : (
              conversations.map((conv) => (
                <Card
                  key={conv.id}
                  className="cursor-pointer transition-colors hover:bg-accent"
                >
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-medium mb-1">{conv.title}</h3>
                        <p className="text-sm text-muted-foreground line-clamp-2 mb-2">
                          {conv.preview}
                        </p>
                        <div className="flex items-center gap-4 text-xs text-muted-foreground">
                          <div className="flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            <span>{conv.date}</span>
                          </div>
                          <span>{conv.messageCount} tin nhắn</span>
                        </div>
                      </div>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem>Mở</DropdownMenuItem>
                          <DropdownMenuItem>Đổi tên</DropdownMenuItem>
                          <DropdownMenuItem className="text-destructive">
                            <Trash2 className="mr-2 h-4 w-4" />
                            Xóa
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </ScrollArea>
      </div>
    </PageLayout>
  );
}
