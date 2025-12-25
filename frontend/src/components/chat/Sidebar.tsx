'use client';

import { useTheme } from 'next-themes';
import { usePathname, useRouter } from 'next/navigation';
import {
  MessageSquare,
  Settings,
  ChevronLeft,
  ChevronRight,
  Moon,
  Sun,
  Plane,
  Plus,
  HelpCircle,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
  onNewChat?: () => void;
}

export function Sidebar({ collapsed, onToggleCollapse, onNewChat }: SidebarProps) {
  const { theme, setTheme } = useTheme();
  const pathname = usePathname();
  const router = useRouter();

  const menuItems = [
    { id: 'chat', icon: MessageSquare, label: 'Chat Tư Vấn', href: '/' },
    { id: 'today-flights', icon: Plane, label: 'Chuyến Bay Hôm Nay', href: '/today-flights' },
  ];

  return (
    <aside
      className={cn(
        'flex flex-col border-r bg-card transition-all duration-300',
        collapsed ? 'w-16' : 'w-64',
        'hidden md:flex'
      )}
    >
      {/* Logo Header */}
      <div className="flex h-14 items-center justify-between border-b px-4">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-gradient-to-br from-primary to-primary/70">
              <Plane className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-semibold text-lg">Airline AI</span>
          </div>
        )}
        {collapsed && (
          <div className="mx-auto p-1.5 rounded-lg bg-gradient-to-br from-primary to-primary/70">
            <Plane className="h-5 w-5 text-primary-foreground" />
          </div>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 shrink-0"
          onClick={onToggleCollapse}
        >
          {collapsed ? (
            <ChevronRight className="h-4 w-4" />
          ) : (
            <ChevronLeft className="h-4 w-4" />
          )}
        </Button>
      </div>

      {/* Main Navigation */}
      <div className="flex-1 overflow-hidden flex flex-col">
        <div className="px-2 py-3 space-y-1">
          {/* New Chat Button */}
          {!collapsed ? (
            <Button
              variant="default"
              className="w-full justify-start gap-2 rounded-lg"
              size="sm"
              onClick={onNewChat}
            >
              <Plus className="h-4 w-4" />
              Chat Mới
            </Button>
          ) : (
            <Button 
              variant="default" 
              size="icon" 
              className="w-full rounded-lg"
              onClick={onNewChat}
            >
              <Plus className="h-4 w-4" />
            </Button>
          )}
        </div>

        {/* Navigation Items */}
        <div className="px-2 space-y-1">
          {menuItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Button
                key={item.id}
                variant={isActive ? 'secondary' : 'ghost'}
                className={cn(
                  'w-full rounded-lg',
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

        {/* Spacer */}
        <div className="flex-1" />

        {/* Quick Tips when not collapsed */}
        {!collapsed && (
          <div className="px-3 pb-3">
            <div className="rounded-lg bg-muted/50 p-3 text-xs text-muted-foreground space-y-1">
              <div className="flex items-center gap-1.5 font-medium">
                <HelpCircle className="h-3 w-3" />
                Mẹo sử dụng
              </div>
              <p>Hỏi về chuyến bay, chính sách hành lý, hoặc yêu cầu tìm vé máy bay.</p>
            </div>
          </div>
        )}
      </div>

      {/* Footer Actions */}
      <div className="border-t p-2 space-y-1">
        {/* Theme Toggle */}
        <Button
          variant="ghost"
          size="sm"
          className={cn(
            'w-full rounded-lg',
            collapsed ? 'justify-center px-0' : 'justify-start gap-3'
          )}
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
        >
          {theme === 'dark' ? (
            <>
              <Sun className="h-4 w-4" />
              {!collapsed && <span>Chế độ sáng</span>}
            </>
          ) : (
            <>
              <Moon className="h-4 w-4" />
              {!collapsed && <span>Chế độ tối</span>}
            </>
          )}
        </Button>

        {/* Settings */}
        <Button
          variant={pathname === '/settings' ? 'secondary' : 'ghost'}
          size="sm"
          className={cn(
            'w-full rounded-lg',
            collapsed ? 'justify-center px-0' : 'justify-start gap-3'
          )}
          onClick={() => router.push('/settings')}
        >
          <Settings className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Cài đặt</span>}
        </Button>
      </div>
    </aside>
  );
}
