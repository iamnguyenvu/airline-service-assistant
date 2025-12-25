'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface ChatSession {
  id: string;
  title: string;
  messages: ChatMessage[];
  createdAt: Date;
  updatedAt: Date;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  usedTools?: boolean;
  model?: string;
}

interface ChatHistoryState {
  sessions: ChatSession[];
  currentSessionId: string | null;
  
  // Actions
  createSession: (title?: string) => string;
  deleteSession: (id: string) => void;
  setCurrentSession: (id: string | null) => void;
  addMessage: (sessionId: string, message: ChatMessage) => void;
  updateSessionTitle: (id: string, title: string) => void;
  getCurrentSession: () => ChatSession | null;
  clearHistory: () => void;
}

export const useChatHistory = create<ChatHistoryState>()(
  persist(
    (set, get) => ({
      sessions: [],
      currentSessionId: null,

      createSession: (title?: string) => {
        const id = `session_${Date.now()}`;
        const newSession: ChatSession = {
          id,
          title: title || 'Cuộc trò chuyện mới',
          messages: [],
          createdAt: new Date(),
          updatedAt: new Date(),
        };
        
        set((state) => ({
          sessions: [newSession, ...state.sessions],
          currentSessionId: id,
        }));
        
        return id;
      },

      deleteSession: (id: string) => {
        set((state) => ({
          sessions: state.sessions.filter(s => s.id !== id),
          currentSessionId: state.currentSessionId === id ? null : state.currentSessionId,
        }));
      },

      setCurrentSession: (id: string | null) => {
        set({ currentSessionId: id });
      },

      addMessage: (sessionId: string, message: ChatMessage) => {
        set((state) => ({
          sessions: state.sessions.map(session => {
            if (session.id !== sessionId) return session;
            
            const updatedSession = {
              ...session,
              messages: [...session.messages, message],
              updatedAt: new Date(),
            };
            
            // Auto-generate title from first user message
            if (session.messages.length === 0 && message.role === 'user') {
              updatedSession.title = message.content.slice(0, 50) + (message.content.length > 50 ? '...' : '');
            }
            
            return updatedSession;
          }),
        }));
      },

      updateSessionTitle: (id: string, title: string) => {
        set((state) => ({
          sessions: state.sessions.map(session =>
            session.id === id ? { ...session, title } : session
          ),
        }));
      },

      getCurrentSession: () => {
        const state = get();
        if (!state.currentSessionId) return null;
        return state.sessions.find(s => s.id === state.currentSessionId) || null;
      },

      clearHistory: () => {
        set({ sessions: [], currentSessionId: null });
      },
    }),
    {
      name: 'airline-chat-history',
      partialize: (state) => ({
        sessions: state.sessions,
        currentSessionId: state.currentSessionId,
      }),
    }
  )
);
