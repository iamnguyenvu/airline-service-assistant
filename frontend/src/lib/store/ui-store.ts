'use client';

import { create } from 'zustand';

interface UIStore {
  sidebarExpanded: boolean;
  rightPanelVisible: boolean;
  rightPanelContent: 'flights' | 'policy' | 'tips' | null;
  
  toggleSidebar: () => void;
  setRightPanel: (content: 'flights' | 'policy' | 'tips' | null) => void;
  closeRightPanel: () => void;
}

export const useUIStore = create<UIStore>((set) => ({
  sidebarExpanded: true,
  rightPanelVisible: false,
  rightPanelContent: null,
  
  toggleSidebar: () => set((state) => ({ sidebarExpanded: !state.sidebarExpanded })),
  
  setRightPanel: (content) => set({ 
    rightPanelVisible: true, 
    rightPanelContent: content 
  }),
  
  closeRightPanel: () => set({ 
    rightPanelVisible: false, 
    rightPanelContent: null 
  }),
}));

