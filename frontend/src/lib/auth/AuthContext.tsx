'use client';

import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';

// User type
export interface User {
  id: string;
  email: string;
  name?: string;
  avatar?: string;
  provider?: 'google' | 'github' | 'email';
}

// Auth state type
interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

// Auth context type
interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  loginWithOAuth: (provider: 'google' | 'github') => Promise<void>;
  sendOTP: (email: string) => Promise<void>;
  verifyOTP: (email: string, otp: string) => Promise<void>;
  register: (email: string, password: string, name: string) => Promise<void>;
  logout: () => void;
  error: string | null;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

// Local storage keys
const USER_KEY = 'airline_assistant_user';
const SESSIONS_KEY = 'airline_assistant_sessions';

// Mock users database (in production, this would be backend)
const MOCK_USERS: Record<string, { password: string; user: User }> = {};
const OTP_STORE: Record<string, { otp: string; expires: number }> = {};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    isLoading: true,
    isAuthenticated: false,
  });
  const [error, setError] = useState<string | null>(null);

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem(USER_KEY);
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        setState({ user, isLoading: false, isAuthenticated: true });
      } catch {
        localStorage.removeItem(USER_KEY);
        setState({ user: null, isLoading: false, isAuthenticated: false });
      }
    } else {
      setState({ user: null, isLoading: false, isAuthenticated: false });
    }
  }, []);

  const saveUser = (user: User) => {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    setState({ user, isLoading: false, isAuthenticated: true });
  };

  const login = useCallback(async (email: string, password: string) => {
    setError(null);
    setState(prev => ({ ...prev, isLoading: true }));
    
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const stored = MOCK_USERS[email];
    if (stored && stored.password === password) {
      saveUser(stored.user);
    } else {
      setState(prev => ({ ...prev, isLoading: false }));
      throw new Error('Email hoặc mật khẩu không đúng');
    }
  }, []);

  const loginWithOAuth = useCallback(async (provider: 'google' | 'github') => {
    setError(null);
    setState(prev => ({ ...prev, isLoading: true }));
    
    // Simulate OAuth flow
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // Create mock OAuth user
    const mockUser: User = {
      id: `${provider}_${Date.now()}`,
      email: `user_${Date.now()}@${provider}.com`,
      name: provider === 'google' ? 'Google User' : 'GitHub User',
      avatar: provider === 'google' 
        ? 'https://lh3.googleusercontent.com/a/default-user=s96-c'
        : 'https://github.com/identicons/user.png',
      provider,
    };
    
    saveUser(mockUser);
  }, []);

  const sendOTP = useCallback(async (email: string) => {
    setError(null);
    
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Generate 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    OTP_STORE[email] = { 
      otp, 
      expires: Date.now() + 5 * 60 * 1000 // 5 minutes
    };
    
    // In production, send email here
    console.log(`📧 OTP for ${email}: ${otp}`);
    
    // For demo, show OTP in alert (remove in production!)
    alert(`🔐 Mã OTP của bạn: ${otp}\n(Trong môi trường production, mã này sẽ được gửi qua email)`);
  }, []);

  const verifyOTP = useCallback(async (email: string, otp: string) => {
    setError(null);
    setState(prev => ({ ...prev, isLoading: true }));
    
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const stored = OTP_STORE[email];
    if (!stored) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw new Error('Không tìm thấy mã OTP. Vui lòng yêu cầu gửi lại.');
    }
    
    if (Date.now() > stored.expires) {
      delete OTP_STORE[email];
      setState(prev => ({ ...prev, isLoading: false }));
      throw new Error('Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.');
    }
    
    if (stored.otp !== otp) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw new Error('Mã OTP không đúng');
    }
    
    // OTP verified - create/login user
    delete OTP_STORE[email];
    
    let user = MOCK_USERS[email]?.user;
    if (!user) {
      // New user from OTP verification
      user = {
        id: `email_${Date.now()}`,
        email,
        name: email.split('@')[0],
        provider: 'email',
      };
      MOCK_USERS[email] = { password: '', user };
    }
    
    saveUser(user);
  }, []);

  const register = useCallback(async (email: string, password: string, name: string) => {
    setError(null);
    setState(prev => ({ ...prev, isLoading: true }));
    
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    if (MOCK_USERS[email]) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw new Error('Email này đã được đăng ký');
    }
    
    const user: User = {
      id: `email_${Date.now()}`,
      email,
      name,
      provider: 'email',
    };
    
    MOCK_USERS[email] = { password, user };
    
    // Send OTP for verification
    await sendOTP(email);
    setState(prev => ({ ...prev, isLoading: false }));
  }, [sendOTP]);

  const logout = useCallback(() => {
    localStorage.removeItem(USER_KEY);
    setState({ user: null, isLoading: false, isAuthenticated: false });
  }, []);

  const clearError = useCallback(() => setError(null), []);

  return (
    <AuthContext.Provider
      value={{
        ...state,
        login,
        loginWithOAuth,
        sendOTP,
        verifyOTP,
        register,
        logout,
        error,
        clearError,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
