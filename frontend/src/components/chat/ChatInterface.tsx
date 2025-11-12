'use client';

import { useState } from 'react';
import { Sidebar } from './Sidebar';
import { ChatArea } from './ChatArea';
import { RightPanel } from './RightPanel';
import { MobileNav } from './MobileNav';
import { MobileHeader } from './MobileHeader';

export function ChatInterface() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [rightPanelOpen, setRightPanelOpen] = useState(false);
  const [rightPanelContent, setRightPanelContent] = useState<{
    type: 'flights' | 'policy' | 'tips';
    data?: unknown;
  } | null>(null);
  const [mobileActiveItem, setMobileActiveItem] = useState('chat');
  const [newChatTrigger, setNewChatTrigger] = useState(0);

  return (
    <>
      <div className="flex h-screen w-full overflow-hidden bg-background">
        <Sidebar
          collapsed={sidebarCollapsed}
          onToggleCollapse={() => setSidebarCollapsed(!sidebarCollapsed)}
          onNewChat={() => setNewChatTrigger(prev => prev + 1)}
        />

        <div className="flex flex-1 flex-col">
          <MobileHeader />
          <ChatArea
            onShowRightPanel={(content) => {
              setRightPanelContent(content);
              setRightPanelOpen(true);
            }}
            newChatTrigger={newChatTrigger}
          />
        </div>

        <RightPanel
          isOpen={rightPanelOpen}
          content={rightPanelContent}
          onClose={() => setRightPanelOpen(false)}
        />
      </div>

      <MobileNav
        activeItem={mobileActiveItem}
        onItemClick={setMobileActiveItem}
      />
    </>
  );
}
