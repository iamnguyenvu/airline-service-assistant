'use client';

import { useState } from 'react';
import { Bell, Globe, Shield, User, Palette } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';

export default function SettingsPage() {
  const [notifications, setNotifications] = useState(true);
  const [soundEnabled, setSoundEnabled] = useState(false);

  return (
    <div className="flex h-screen flex-col">
      <div className="border-b bg-card p-4 md:p-6">
        <h1 className="text-2xl font-semibold">Cài Đặt</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Quản lý tài khoản và tùy chỉnh trải nghiệm của bạn
        </p>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-4 md:p-6 space-y-6 max-w-3xl">
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <User className="h-5 w-5" />
                <CardTitle>Thông Tin Cá Nhân</CardTitle>
              </div>
              <CardDescription>
                Cập nhật thông tin tài khoản của bạn
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Họ và tên</Label>
                <Input id="name" placeholder="Nguyễn Văn A" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" type="email" placeholder="email@example.com" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="phone">Số điện thoại</Label>
                <Input id="phone" placeholder="+84 xxx xxx xxx" />
              </div>
              <Button>Lưu thay đổi</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Bell className="h-5 w-5" />
                <CardTitle>Thông Báo</CardTitle>
              </div>
              <CardDescription>
                Quản lý cách bạn nhận thông báo
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Thông báo đẩy</Label>
                  <p className="text-sm text-muted-foreground">
                    Nhận thông báo về tin nhắn mới
                  </p>
                </div>
                <Switch
                  checked={notifications}
                  onCheckedChange={setNotifications}
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Âm thanh</Label>
                  <p className="text-sm text-muted-foreground">
                    Phát âm thanh khi có tin nhắn
                  </p>
                </div>
                <Switch
                  checked={soundEnabled}
                  onCheckedChange={setSoundEnabled}
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Globe className="h-5 w-5" />
                <CardTitle>Ngôn Ngữ & Vùng</CardTitle>
              </div>
              <CardDescription>
                Tùy chỉnh ngôn ngữ và múi giờ
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Ngôn ngữ</Label>
                <Select defaultValue="vi">
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="vi">Tiếng Việt</SelectItem>
                    <SelectItem value="en">English</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Múi giờ</Label>
                <Select defaultValue="asia">
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="asia">Asia/Ho_Chi_Minh (GMT+7)</SelectItem>
                    <SelectItem value="utc">UTC</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Palette className="h-5 w-5" />
                <CardTitle>Giao Diện</CardTitle>
              </div>
              <CardDescription>
                Tùy chỉnh giao diện ứng dụng
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Chế độ hiển thị</Label>
                <Select defaultValue="system">
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="light">Sáng</SelectItem>
                    <SelectItem value="dark">Tối</SelectItem>
                    <SelectItem value="system">Theo hệ thống</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                <CardTitle>Bảo Mật</CardTitle>
              </div>
              <CardDescription>
                Quản lý bảo mật tài khoản
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button variant="outline" className="w-full justify-start">
                Đổi mật khẩu
              </Button>
              <Button variant="outline" className="w-full justify-start">
                Xác thực hai yếu tố
              </Button>
              <Button variant="outline" className="w-full justify-start text-destructive">
                Xóa tài khoản
              </Button>
            </CardContent>
          </Card>
        </div>
      </ScrollArea>
    </div>
  );
}
