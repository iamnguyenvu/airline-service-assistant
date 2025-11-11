'use client';

import { MessageSquare, History, Settings, FileText, ChevronLeft, User, Moon } from 'lucide-react';
import { useUIStore } from '@/lib/store/ui-store';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

const navItems = [
  { icon: MessageSquare, label: 'Chat Tư Vấn', route: '/' },
  { icon: History, label: 'Lịch Sử', route: '/history' },
  { icon: Settings, label: 'Cài Đặt', route: '/settings' },
  { icon: FileText, label: 'Điều Khoản', route: '/terms' },
];

export function Sidebar() {
  const { sidebarExpanded, toggleSidebar } = useUIStore();
  const pathname = usePathname();

  return (
    <aside
      className="fixed left-0 top-0 h-full bg-[var(--sidebar-bg)] border-r border-[var(--sidebar-border)] z-40 sidebar-transition flex flex-col"
      style={{ width: sidebarExpanded ? '240px' : '64px' }}
    >
      {/* Logo */}
      <div className="h-16 flex items-center justify-between px-4 border-b border-[var(--sidebar-border)]">
        {sidebarExpanded ? (
          <h1 className="text-xl font-bold text-[var(--primary)]">Airline AI</h1>
        ) : (
          <div className="w-8 h-8 rounded-lg bg-[var(--primary)] flex items-center justify-center">
            <span className="text-white font-bold text-sm">A</span>
          </div>
        )}
        <button
          onClick={toggleSidebar}
          className="p-1.5 rounded-md hover:bg-[var(--muted)] transition-colors"
          aria-label={sidebarExpanded ? 'Collapse sidebar' : 'Expand sidebar'}
        >
          <ChevronLeft
            className={`w-4 h-4 text-[var(--muted-foreground)] transition-transform ${
              !sidebarExpanded ? 'rotate-180' : ''
            }`}
          />
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-4 px-2">
        <ul className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.route;
            
            return (
              <li key={item.route}>
                <Link
                  href={item.route}
                  className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-[var(--primary)] text-[var(--primary-foreground)]'
                      : 'text-[var(--muted-foreground)] hover:bg-[var(--muted)] hover:text-[var(--foreground)]'
                  }`}
                >
                  <Icon className="w-5 h-5 flex-shrink-0" />
                  {sidebarExpanded && (
                    <span className="text-sm font-medium">{item.label}</span>
                  )}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* User Section */}
      <div className="border-t border-[var(--sidebar-border)] p-4 space-y-2">
        <button
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-[var(--muted-foreground)] hover:bg-[var(--muted)] hover:text-[var(--foreground)] transition-colors"
        >
          <User className="w-5 h-5 flex-shrink-0" />
          {sidebarExpanded && <span className="text-sm">Profile</span>}
        </button>
        <button
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-[var(--muted-foreground)] hover:bg-[var(--muted)] hover:text-[var(--foreground)] transition-colors"
        >
          <Moon className="w-5 h-5 flex-shrink-0" />
          {sidebarExpanded && <span className="text-sm">Theme</span>}
        </button>
      </div>
    </aside>
  );
}

