'use client';

import { useTheme } from 'next-themes';
import { usePathname, useRouter } from 'next/navigation';
import {
  MessageSquare,
  History,
  Settings,
  FileText,
  ChevronLeft,
  ChevronRight,
  Moon,
  Sun,
  Plane,
  Plus,
  Upload,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
}

export function Sidebar({ collapsed, onToggleCollapse }: SidebarProps) {
  const { theme, setTheme } = useTheme();
  const pathname = usePathname();
  const router = useRouter();

  const menuItems = [
    { id: 'chat', icon: MessageSquare, label: 'Chat Tư Vấn', href: '/' },
    { id: 'history', icon: History, label: 'Lịch Sử Chat', href: '/history' },
    { id: 'ingestion', icon: Upload, label: 'Quản Lý Dữ Liệu', href: '/ingestion' },
    { id: 'upload', icon: FileText, label: 'Upload Tài Liệu', href: '/upload' },
    { id: 'terms', icon: FileText, label: 'Điều Khoản', href: '/terms' },
  ];

  return (
    <aside
      className={cn(
        'flex flex-col border-r bg-card transition-all duration-300',
        collapsed ? 'w-16' : 'w-60',
        'hidden md:flex'
      )}
    >
      <div className="flex h-14 items-center justify-between border-b px-4">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <Plane className="h-6 w-6 text-primary" />
            <span className="font-semibold text-lg">Airline AI Servant</span>
          </div>
        )}
        {collapsed && <Plane className="h-6 w-6 text-primary mx-auto" />}
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8"
          onClick={onToggleCollapse}
        >
          {collapsed ? (
            <ChevronRight className="h-4 w-4" />
          ) : (
            <ChevronLeft className="h-4 w-4" />
          )}
        </Button>
      </div>

      <div className="flex-1 overflow-y-auto py-4">
        <div className="px-2 space-y-1">
          {!collapsed && (
            <Button
              variant="default"
              className="w-full justify-start gap-2 mb-4"
              size="sm"
            >
              <Plus className="h-4 w-4" />
              Chat Mới
            </Button>
          )}
          {collapsed && (
            <Button variant="default" size="icon" className="w-full mb-4">
              <Plus className="h-4 w-4" />
            </Button>
          )}

          {menuItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Button
                key={item.id}
                variant={isActive ? 'secondary' : 'ghost'}
                className={cn(
                  'w-full',
                  collapsed ? 'justify-center px-0' : 'justify-start gap-3'
                )}
                size="sm"
                onClick={() => router.push(item.href)}
              >
                <item.icon className="h-4 w-4 shrink-0" />
                {!collapsed && <span>{item.label}</span>}
              </Button>
            );
          })}
        </div>
      </div>

      <div className="border-t p-2 space-y-2">
        <Button
          variant="ghost"
          size="sm"
          className={cn(
            'w-full',
            collapsed ? 'justify-center px-0' : 'justify-start gap-3'
          )}
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
        >
          {theme === 'dark' ? (
            <>
              <Sun className="h-4 w-4" />
              {!collapsed && <span>Sáng</span>}
            </>
          ) : (
            <>
              <Moon className="h-4 w-4" />
              {!collapsed && <span>Tối</span>}
            </>
          )}
        </Button>

        <Button
          variant={pathname === '/settings' ? 'secondary' : 'ghost'}
          size="sm"
          className={cn(
            'w-full',
            collapsed ? 'justify-center px-0' : 'justify-start gap-3'
          )}
          onClick={() => router.push('/settings')}
        >
          <Settings className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Cài Đặt</span>}
        </Button>

        <Button
          variant="ghost"
          size="sm"
          className={cn(
            'w-full',
            collapsed ? 'justify-center px-0' : 'justify-start gap-3'
          )}
        >
          {collapsed ? (
            <Avatar className="h-6 w-6">
              <AvatarFallback className="text-xs">U</AvatarFallback>
            </Avatar>
          ) : (
            <>
              <Avatar className="h-6 w-6">
                <AvatarFallback className="text-xs">U</AvatarFallback>
              </Avatar>
              <span className="flex-1 text-left">User</span>
            </>
          )}
        </Button>
      </div>
    </aside>
  );
}
