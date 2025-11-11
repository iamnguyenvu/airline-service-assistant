'use client';

import { ReactNode } from 'react';
import { Sidebar } from './Sidebar';
import { RightPanel } from './RightPanel';
import { useUIStore } from '@/lib/store/ui-store';

interface AppLayoutProps {
  children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  const { sidebarExpanded, rightPanelVisible } = useUIStore();

  return (
    <div className="flex h-screen overflow-hidden bg-[var(--background)]">
      {/* Sidebar */}
      <Sidebar />
      
      {/* Main Content */}
      <main 
        className="flex-1 flex flex-col overflow-hidden"
        style={{
          marginLeft: sidebarExpanded ? '240px' : '64px',
          marginRight: rightPanelVisible ? '400px' : '0px',
          transition: 'margin 300ms cubic-bezier(0.4, 0, 0.2, 1)',
        }}
      >
        {children}
      </main>
      
      {/* Right Panel */}
      <RightPanel />
    </div>
  );
}

