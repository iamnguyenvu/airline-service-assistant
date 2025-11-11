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
        {content?.type === 'flights' && <FlightResults data={content.data} />}
        {content?.type === 'policy' && <PolicyDetails data={content.data} />}
        {content?.type === 'tips' && <QuickTips />}
      </ScrollArea>
    </aside>
  );
}

interface FlightResultsProps {
  data?: unknown;
}

function FlightResults({ data }: FlightResultsProps) {
  // Parse flight data from API response
  let flights: Array<{
    id: string;
    carrier: string;
    flightNo: string;
    depIata: string;
    arrIata: string;
    depTime: string | null;
    arrTime: string | null;
    durationMin: number | null;
    priceCents: number | null;
  }> = [];

  if (data && typeof data === 'object' && 'flights' in data) {
    const flightData = data as { flights?: unknown[] };
    if (Array.isArray(flightData.flights)) {
      flights = flightData.flights.map((f: any, index: number) => ({
        id: f.id?.toString() || index.toString(),
        carrier: f.carrier || '',
        flightNo: f.flightNo || '',
        depIata: f.depIata || '',
        arrIata: f.arrIata || '',
        depTime: f.depTime || null,
        arrTime: f.arrTime || null,
        durationMin: f.durationMin || null,
        priceCents: f.priceCents || null,
      }));
    }
  }

  if (flights.length === 0) {
    return (
      <div className="text-center text-muted-foreground py-8">
        <p>Chưa có kết quả chuyến bay.</p>
        <p className="text-sm mt-2">Hãy hỏi về chuyến bay để xem kết quả.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {flights.map((flight) => {
        const depTime = flight.depTime ? new Date(flight.depTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'N/A';
        const arrTime = flight.arrTime ? new Date(flight.arrTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'N/A';
        const duration = flight.durationMin ? `${Math.floor(flight.durationMin / 60)}h ${flight.durationMin % 60}m` : 'N/A';
        const price = flight.priceCents ? `${(flight.priceCents / 100).toLocaleString('vi-VN')} ₫` : 'N/A';
        
        return (
          <Card key={flight.id} className="overflow-hidden">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium">
                {flight.carrier} - {flight.flightNo}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 pb-4">
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-lg font-semibold">{depTime}</p>
                  <p className="text-sm text-muted-foreground">
                    {flight.depIata}
                  </p>
                </div>
                <div className="flex flex-col items-center">
                  <Plane className="h-4 w-4 text-muted-foreground mb-1" />
                  <p className="text-xs text-muted-foreground">{duration}</p>
                </div>
                <div className="space-y-1 text-right">
                  <p className="text-lg font-semibold">{arrTime}</p>
                  <p className="text-sm text-muted-foreground">
                    {flight.arrIata}
                  </p>
                </div>
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-1 text-sm">
                  <DollarSign className="h-4 w-4" />
                  <span className="font-semibold text-primary">{price}</span>
                </div>
                <Button size="sm">Chọn chuyến bay</Button>
              </div>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}

interface PolicyDetailsProps {
  data?: unknown;
}

function PolicyDetails({ data }: PolicyDetailsProps) {
  if (!data || (typeof data === 'object' && !('message' in data))) {
    return (
      <div className="text-center text-muted-foreground py-8">
        <p>Chưa có thông tin chính sách.</p>
        <p className="text-sm mt-2">Hãy hỏi về chính sách để xem thông tin.</p>
      </div>
    );
  }

  const message = typeof data === 'object' && 'message' in data 
    ? String(data.message) 
    : '';

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Thông Tin Chính Sách</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground space-y-2 whitespace-pre-wrap">
          {message || 'Không có thông tin chi tiết.'}
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
