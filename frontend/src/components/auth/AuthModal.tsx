'use client';

import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAuth } from '@/lib/auth/AuthContext';
import { Loader2, Mail, Github, Chrome } from 'lucide-react';
import { InputOTP, InputOTPGroup, InputOTPSlot } from '@/components/ui/input-otp';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function AuthModal({ isOpen, onClose }: AuthModalProps) {
  const { login, loginWithOAuth, sendOTP, verifyOTP, register, isLoading } = useAuth();
  const [tab, setTab] = useState<'login' | 'register' | 'otp'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [otpEmail, setOtpEmail] = useState('');
  const [otpSent, setOtpSent] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await login(email, password);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Đăng nhập thất bại');
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await register(email, password, name);
      setOtpEmail(email);
      setTab('otp');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Đăng ký thất bại');
    }
  };

  const handleOAuth = async (provider: 'google' | 'github') => {
    setError(null);
    try {
      await loginWithOAuth(provider);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Đăng nhập thất bại');
    }
  };

  const handleSendOTP = async () => {
    setError(null);
    try {
      await sendOTP(email);
      setOtpEmail(email);
      setOtpSent(true);
      setTab('otp');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Gửi OTP thất bại');
    }
  };

  const handleVerifyOTP = async () => {
    setError(null);
    try {
      await verifyOTP(otpEmail, otp);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Xác thực OTP thất bại');
    }
  };

  const resetForm = () => {
    setEmail('');
    setPassword('');
    setName('');
    setOtp('');
    setError(null);
    setOtpSent(false);
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => { if (!open) { resetForm(); onClose(); } }}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-center text-xl">
            {tab === 'otp' ? 'Xác thực OTP' : 'Chào mừng đến Airline Assistant'}
          </DialogTitle>
        </DialogHeader>

        {tab === 'otp' ? (
          <div className="space-y-6 py-4">
            <p className="text-center text-muted-foreground">
              Nhập mã OTP đã gửi đến email <span className="font-medium">{otpEmail}</span>
            </p>
            
            <div className="flex justify-center">
              <InputOTP value={otp} onChange={setOtp} maxLength={6}>
                <InputOTPGroup>
                  <InputOTPSlot index={0} />
                  <InputOTPSlot index={1} />
                  <InputOTPSlot index={2} />
                  <InputOTPSlot index={3} />
                  <InputOTPSlot index={4} />
                  <InputOTPSlot index={5} />
                </InputOTPGroup>
              </InputOTP>
            </div>

            {error && (
              <p className="text-center text-sm text-destructive">{error}</p>
            )}

            <Button 
              onClick={handleVerifyOTP} 
              disabled={otp.length !== 6 || isLoading}
              className="w-full"
            >
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Xác thực
            </Button>

            <Button 
              variant="ghost" 
              onClick={() => { setTab('login'); resetForm(); }}
              className="w-full"
            >
              Quay lại đăng nhập
            </Button>
          </div>
        ) : (
          <Tabs value={tab} onValueChange={(v) => setTab(v as 'login' | 'register')}>
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="login">Đăng nhập</TabsTrigger>
              <TabsTrigger value="register">Đăng ký</TabsTrigger>
            </TabsList>

            <TabsContent value="login" className="space-y-4 pt-4">
              {/* OAuth Buttons */}
              <div className="space-y-2">
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => handleOAuth('google')}
                  disabled={isLoading}
                >
                  <Chrome className="mr-2 h-4 w-4" />
                  Tiếp tục với Google
                </Button>
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => handleOAuth('github')}
                  disabled={isLoading}
                >
                  <Github className="mr-2 h-4 w-4" />
                  Tiếp tục với GitHub
                </Button>
              </div>

              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-background px-2 text-muted-foreground">hoặc</span>
                </div>
              </div>

              {/* Email/Password Login */}
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="email@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">Mật khẩu</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                {error && (
                  <p className="text-sm text-destructive">{error}</p>
                )}

                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                  Đăng nhập
                </Button>
              </form>

              {/* OTP Login */}
              <div className="text-center">
                <Button 
                  variant="link" 
                  onClick={handleSendOTP}
                  disabled={!email || isLoading}
                  className="text-sm"
                >
                  <Mail className="mr-1 h-3 w-3" />
                  Gửi mã OTP qua email
                </Button>
              </div>
            </TabsContent>

            <TabsContent value="register" className="space-y-4 pt-4">
              <form onSubmit={handleRegister} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="reg-name">Họ và tên</Label>
                  <Input
                    id="reg-name"
                    type="text"
                    placeholder="Nguyễn Văn A"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reg-email">Email</Label>
                  <Input
                    id="reg-email"
                    type="email"
                    placeholder="email@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reg-password">Mật khẩu</Label>
                  <Input
                    id="reg-password"
                    type="password"
                    placeholder="Tối thiểu 6 ký tự"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    minLength={6}
                    required
                  />
                </div>

                {error && (
                  <p className="text-sm text-destructive">{error}</p>
                )}

                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                  Đăng ký
                </Button>
              </form>

              <p className="text-center text-xs text-muted-foreground">
                Bằng việc đăng ký, bạn đồng ý với{' '}
                <a href="/terms" className="underline">Điều khoản dịch vụ</a>
              </p>
            </TabsContent>
          </Tabs>
        )}
      </DialogContent>
    </Dialog>
  );
}
