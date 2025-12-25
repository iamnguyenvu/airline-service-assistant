'use client';

import { useState } from 'react';
import { useAuth } from '@/lib/auth/AuthContext';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { AuthModal } from './AuthModal';
import { LogIn, LogOut, User, History, Settings, Github, Chrome, Mail, ChevronDown } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

export function UserButton() {
  const { user, isAuthenticated, logout, isLoading } = useAuth();
  const [showAuthModal, setShowAuthModal] = useState(false);

  if (isLoading) {
    return (
      <div className="flex items-center gap-2">
        <div className="h-8 w-8 animate-pulse rounded-full bg-muted" />
        <div className="hidden sm:block space-y-1">
          <div className="h-3 w-20 animate-pulse rounded bg-muted" />
          <div className="h-2 w-16 animate-pulse rounded bg-muted" />
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return (
      <>
        <Button
          variant="default"
          size="sm"
          onClick={() => setShowAuthModal(true)}
          className="gap-2 rounded-full px-4"
        >
          <LogIn className="h-4 w-4" />
          <span>Đăng nhập</span>
        </Button>
        <AuthModal isOpen={showAuthModal} onClose={() => setShowAuthModal(false)} />
      </>
    );
  }

  const initials = user.name
    ? user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
    : user.email[0].toUpperCase();

  const ProviderIcon = user.provider === 'google' ? Chrome 
    : user.provider === 'github' ? Github 
    : Mail;

  const providerLabel = user.provider === 'google' ? 'Google'
    : user.provider === 'github' ? 'GitHub'
    : 'Email';

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button 
          variant="ghost" 
          className="h-auto p-2 rounded-xl hover:bg-accent/80 transition-all w-full justify-start gap-3"
        >
          <Avatar className="h-9 w-9 ring-2 ring-primary/20">
            <AvatarImage src={user.avatar} alt={user.name || user.email} />
            <AvatarFallback className="bg-gradient-to-br from-primary to-primary/60 text-primary-foreground font-semibold">
              {initials}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 text-left hidden sm:block">
            <p className="text-sm font-medium leading-tight truncate max-w-[120px]">
              {user.name || 'User'}
            </p>
            <div className="flex items-center gap-1 mt-0.5">
              <ProviderIcon className="h-3 w-3 text-muted-foreground" />
              <span className="text-xs text-muted-foreground truncate max-w-[100px]">
                {user.email}
              </span>
            </div>
          </div>
          <ChevronDown className="h-4 w-4 text-muted-foreground hidden sm:block" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-64" align="end" forceMount>
        <DropdownMenuLabel className="font-normal p-4">
          <div className="flex items-center gap-3">
            <Avatar className="h-12 w-12 ring-2 ring-primary/20">
              <AvatarImage src={user.avatar} alt={user.name || user.email} />
              <AvatarFallback className="bg-gradient-to-br from-primary to-primary/60 text-primary-foreground text-lg font-semibold">
                {initials}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 space-y-1">
              <p className="text-sm font-semibold leading-none">{user.name || 'User'}</p>
              <p className="text-xs text-muted-foreground truncate">
                {user.email}
              </p>
              <Badge variant="secondary" className="text-[10px] px-1.5 py-0 h-4 gap-1">
                <ProviderIcon className="h-2.5 w-2.5" />
                {providerLabel}
              </Badge>
            </div>
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem className="py-2.5 cursor-pointer">
          <User className="mr-3 h-4 w-4" />
          <span>Hồ sơ cá nhân</span>
        </DropdownMenuItem>
        <DropdownMenuItem className="py-2.5 cursor-pointer">
          <History className="mr-3 h-4 w-4" />
          <span>Lịch sử trò chuyện</span>
        </DropdownMenuItem>
        <DropdownMenuItem className="py-2.5 cursor-pointer">
          <Settings className="mr-3 h-4 w-4" />
          <span>Cài đặt tài khoản</span>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem 
          onClick={logout} 
          className="py-2.5 cursor-pointer text-destructive focus:text-destructive focus:bg-destructive/10"
        >
          <LogOut className="mr-3 h-4 w-4" />
          <span>Đăng xuất</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
