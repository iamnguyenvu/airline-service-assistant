'use client';

import { MessageSquare, History, Settings, User } from 'lucide-react';
import { cn } from '@/lib/utils';

interface MobileNavProps {
  activeItem: string;
  onItemClick: (item: string) => void;
}

export function MobileNav({ activeItem, onItemClick }: MobileNavProps) {
  const items = [
    { id: 'chat', icon: MessageSquare, label: 'Chat' },
    { id: 'history', icon: History, label: 'Lịch sử' },
    { id: 'settings', icon: Settings, label: 'Cài đặt' },
    { id: 'profile', icon: User, label: 'Tài khoản' },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t bg-background md:hidden">
      <div className="flex items-center justify-around">
        {items.map((item) => (
          <button
            key={item.id}
            onClick={() => onItemClick(item.id)}
            className={cn(
              'flex flex-1 flex-col items-center gap-1 py-3 text-xs transition-colors',
              activeItem === item.id
                ? 'text-primary'
                : 'text-muted-foreground'
            )}
          >
            <item.icon className="h-5 w-5" />
            <span>{item.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}
