'use client';

import { ThemeProvider } from 'next-themes';
import { Toaster as Sonner } from '@/components/ui/sonner';
import { AuthProvider } from '@/lib/auth/AuthContext';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <AuthProvider>
        {children}
        <Sonner />
      </AuthProvider>
    </ThemeProvider>
  );
}
