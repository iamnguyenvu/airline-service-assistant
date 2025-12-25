'use client';

import { useTheme } from 'next-themes';
import { usePathname, useRouter } from 'next/navigation';
import {
  MessageSquare,
  History,
  Settings,
  ChevronLeft,
  ChevronRight,
  Moon,
  Sun,
  Plane,
  Plus,
  Trash2,
  LogIn,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/lib/auth/AuthContext';
import { useChatHistory } from '@/lib/store/chatHistory';
import { UserButton } from '@/components/auth/UserButton';
import { ScrollArea } from '@/components/ui/scroll-area';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import { useState } from 'react';
import { AuthModal } from '@/components/auth/AuthModal';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
  onNewChat?: () => void;
}

export function Sidebar({ collapsed, onToggleCollapse, onNewChat }: SidebarProps) {
  const { theme, setTheme } = useTheme();
  const { isAuthenticated } = useAuth();
  const { sessions, currentSessionId, setCurrentSession, deleteSession, createSession } = useChatHistory();
  const pathname = usePathname();
  const router = useRouter();
  const [showAuthModal, setShowAuthModal] = useState(false);

  const menuItems = [
    { id: 'chat', icon: MessageSquare, label: 'Chat Tư Vấn', href: '/' },
    { id: 'today-flights', icon: Plane, label: 'Chuyến Bay Hôm Nay', href: '/today-flights' },
  ];

  const handleNewChat = () => {
    if (isAuthenticated) {
      createSession();
    }
    onNewChat?.();
  };

  const handleSelectSession = (sessionId: string) => {
    setCurrentSession(sessionId);
    router.push('/');
  };

  return (
    <>
      <aside
        className={cn(
          'flex flex-col border-r bg-card transition-all duration-300',
          collapsed ? 'w-16' : 'w-64',
          'hidden md:flex'
        )}
      >
        <div className="flex h-14 items-center justify-between border-b px-4">
          {!collapsed && (
            <div className="flex items-center gap-2">
              <Plane className="h-6 w-6 text-primary" />
              <span className="font-semibold text-lg">Airline AI</span>
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

        <div className="flex-1 overflow-hidden flex flex-col">
          <div className="px-2 py-3 space-y-1">
            {!collapsed ? (
              <Button
                variant="default"
                className="w-full justify-start gap-2"
                size="sm"
                onClick={handleNewChat}
              >
                <Plus className="h-4 w-4" />
                Chat Mới
              </Button>
            ) : (
              <Button 
                variant="default" 
                size="icon" 
                className="w-full"
                onClick={handleNewChat}
              >
                <Plus className="h-4 w-4" />
              </Button>
            )}
          </div>

          <div className="px-2 space-y-1">
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

          {/* Chat History - Only for authenticated users */}
          {!collapsed && (
            <div className="flex-1 overflow-hidden flex flex-col mt-4">
              <div className="px-3 flex items-center justify-between">
                <span className="text-xs font-medium text-muted-foreground uppercase">
                  {isAuthenticated ? 'Lịch sử chat' : 'Đăng nhập để lưu lịch sử'}
                </span>
                {isAuthenticated && sessions.length > 0 && (
                  <History className="h-3 w-3 text-muted-foreground" />
                )}
              </div>
              
              {isAuthenticated ? (
                <ScrollArea className="flex-1 px-2 mt-2">
                  <div className="space-y-1">
                    {sessions.length === 0 ? (
                      <p className="text-xs text-muted-foreground px-2 py-4 text-center">
                        Chưa có cuộc trò chuyện nào
                      </p>
                    ) : (
                      sessions.slice(0, 10).map((session) => (
                        <div
                          key={session.id}
                          className={cn(
                            'group flex items-center gap-2 rounded-md px-2 py-1.5 text-sm cursor-pointer hover:bg-accent',
                            currentSessionId === session.id && 'bg-accent'
                          )}
                          onClick={() => handleSelectSession(session.id)}
                        >
                          <MessageSquare className="h-3 w-3 shrink-0 text-muted-foreground" />
                          <div className="flex-1 truncate">
                            <p className="truncate text-xs">{session.title}</p>
                            <p className="text-[10px] text-muted-foreground">
                              {format(new Date(session.updatedAt), 'dd/MM HH:mm', { locale: vi })}
                            </p>
                          </div>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-5 w-5 opacity-0 group-hover:opacity-100"
                            onClick={(e) => {
                              e.stopPropagation();
                              deleteSession(session.id);
                            }}
                          >
                            <Trash2 className="h-3 w-3 text-destructive" />
                          </Button>
                        </div>
                      ))
                    )}
                  </div>
                </ScrollArea>
              ) : (
                <div className="px-2 mt-2">
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full gap-2"
                    onClick={() => setShowAuthModal(true)}
                  >
                    <LogIn className="h-3 w-3" />
                    Đăng nhập
                  </Button>
                </div>
              )}
            </div>
          )}
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

          {/* User Button */}
          <div className={cn('flex', collapsed ? 'justify-center' : 'px-1')}>
            <UserButton />
          </div>
        </div>
      </aside>

      <AuthModal isOpen={showAuthModal} onClose={() => setShowAuthModal(false)} />
    </>
  );
}
