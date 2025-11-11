'use client';

import { X, Plane, Clock, MapPin, DollarSign } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';

interface RightPanelProps {
  isOpen: boolean;
  content: {
    type: 'flights' | 'policy' | 'tips';
    data?: unknown;
  } | null;
  onClose: () => void;
}

export function RightPanel({ isOpen, content, onClose }: RightPanelProps) {
  if (!isOpen) return null;

  return (
    <aside
      className={cn(
        'w-[400px] border-l bg-card transition-all duration-300',
        'hidden lg:flex flex-col',
        !isOpen && 'hidden'
      )}
    >
      <div className="flex h-14 items-center justify-between border-b px-4">
        <h2 className="font-semibold">
          {content?.type === 'flights' && 'Kết Quả Chuyến Bay'}
          {content?.type === 'policy' && 'Chính Sách & Quy Định'}
          {content?.type === 'tips' && 'Gợi Ý Hữu Ích'}
        </h2>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-8 w-8">
          <X className="h-4 w-4" />
        </Button>
      </div>

      <ScrollArea className="flex-1 p-4">
        {content?.type === 'flights' && <FlightResults />}
        {content?.type === 'policy' && <PolicyDetails />}
        {content?.type === 'tips' && <QuickTips />}
      </ScrollArea>
    </aside>
  );
}

function FlightResults() {
  const flights = [
    {
      id: 1,
      airline: 'Vietnam Airlines',
      flightNumber: 'VN123',
      departure: { city: 'Hà Nội', time: '08:00', airport: 'HAN' },
      arrival: { city: 'TP.HCM', time: '10:15', airport: 'SGN' },
      price: '1.500.000 VND',
      duration: '2h 15m',
    },
    {
      id: 2,
      airline: 'VietJet Air',
      flightNumber: 'VJ456',
      departure: { city: 'Hà Nội', time: '10:30', airport: 'HAN' },
      arrival: { city: 'TP.HCM', time: '12:45', airport: 'SGN' },
      price: '1.200.000 VND',
      duration: '2h 15m',
    },
  ];

  return (
    <div className="space-y-4">
      {flights.map((flight) => (
        <Card key={flight.id} className="overflow-hidden">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">
              {flight.airline} - {flight.flightNumber}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 pb-4">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-lg font-semibold">{flight.departure.time}</p>
                <p className="text-sm text-muted-foreground">
                  {flight.departure.city}
                </p>
                <p className="text-xs text-muted-foreground">
                  {flight.departure.airport}
                </p>
              </div>
              <div className="flex flex-col items-center">
                <Plane className="h-4 w-4 text-muted-foreground mb-1" />
                <p className="text-xs text-muted-foreground">{flight.duration}</p>
              </div>
              <div className="space-y-1 text-right">
                <p className="text-lg font-semibold">{flight.arrival.time}</p>
                <p className="text-sm text-muted-foreground">{flight.arrival.city}</p>
                <p className="text-xs text-muted-foreground">
                  {flight.arrival.airport}
                </p>
              </div>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-1 text-sm">
                <DollarSign className="h-4 w-4" />
                <span className="font-semibold text-primary">{flight.price}</span>
              </div>
              <Button size="sm">Chọn chuyến bay</Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function PolicyDetails() {
  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Hành Lý Xách Tay</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground space-y-2">
          <p>• Kích thước tối đa: 56cm x 36cm x 23cm</p>
          <p>• Trọng lượng tối đa: 7kg</p>
          <p>• Số lượng: 1 túi xách tay + 1 túi cá nhân</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Hành Lý Ký Gửi</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground space-y-2">
          <p>• Hạng Phổ Thông: 23kg</p>
          <p>• Hạng Thương Gia: 32kg</p>
          <p>• Hạng Nhất: 40kg</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Check-in</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground space-y-2">
          <p>• Online: 24h - 1h trước giờ bay</p>
          <p>• Quầy: 3h - 40 phút trước giờ bay</p>
          <p>• Đóng cửa lên máy bay: 15 phút trước giờ bay</p>
        </CardContent>
      </Card>
    </div>
  );
}

function QuickTips() {
  const tips = [
    {
      title: 'Đặt vé sớm',
      description: 'Đặt vé trước 2-3 tháng để có giá tốt nhất',
    },
    {
      title: 'Bay vào giữa tuần',
      description: 'Thứ 3 và Thứ 4 thường có giá vé rẻ hơn',
    },
    {
      title: 'So sánh giá',
      description: 'So sánh giá trên nhiều nền tảng khác nhau',
    },
    {
      title: 'Kiểm tra hành lý',
      description: 'Xác nhận hành lý miễn phí của hãng bay',
    },
    {
      title: 'Web check-in',
      description: 'Làm thủ tục online để tiết kiệm thời gian',
    },
  ];

  return (
    <div className="space-y-3">
      {tips.map((tip, index) => (
        <Card key={index}>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm">{tip.title}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">{tip.description}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
