'use client';

import { X } from 'lucide-react';
import { useUIStore } from '@/lib/store/ui-store';

export function RightPanel() {
  const { rightPanelVisible, rightPanelContent, closeRightPanel } = useUIStore();

  if (!rightPanelVisible) return null;

  return (
    <aside
      className="fixed right-0 top-0 h-full w-[400px] bg-[var(--panel-bg)] border-l border-[var(--panel-border)] z-30 panel-transition flex flex-col shadow-lg"
      style={{
        transform: rightPanelVisible ? 'translateX(0)' : 'translateX(100%)',
        opacity: rightPanelVisible ? 1 : 0,
      }}
    >
      {/* Header */}
      <div className="h-16 flex items-center justify-between px-4 border-b border-[var(--panel-border)]">
        <h2 className="text-lg font-semibold">
          {rightPanelContent === 'flights' && 'Kết quả tìm kiếm'}
          {rightPanelContent === 'policy' && 'Chi tiết chính sách'}
          {rightPanelContent === 'tips' && 'Mẹo hữu ích'}
        </h2>
        <button
          onClick={closeRightPanel}
          className="p-1.5 rounded-md hover:bg-[var(--muted)] transition-colors"
          aria-label="Close panel"
        >
          <X className="w-5 h-5 text-[var(--muted-foreground)]" />
        </button>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto p-4">
        {rightPanelContent === 'flights' && (
          <div className="text-[var(--muted-foreground)] text-sm">
            Flight results will appear here
          </div>
        )}
        {rightPanelContent === 'policy' && (
          <div className="text-[var(--muted-foreground)] text-sm">
            Policy details will appear here
          </div>
        )}
        {rightPanelContent === 'tips' && (
          <div className="space-y-3">
            <div className="p-3 rounded-lg bg-[var(--accent)] text-[var(--accent-foreground)]">
              <p className="text-sm font-medium">💡 Bạn có thể hỏi:</p>
              <p className="text-sm mt-1">"Tìm vé rẻ nhất SGN-HAN"</p>
            </div>
            <div className="p-3 rounded-lg bg-[var(--accent)] text-[var(--accent-foreground)]">
              <p className="text-sm font-medium">💡 So sánh giá:</p>
              <p className="text-sm mt-1">"So sánh giá giữa các hãng bay"</p>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}

