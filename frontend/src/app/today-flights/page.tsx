'use client';

import { useState, useEffect } from 'react';
import { Plane, RefreshCw, Clock, MapPin, DollarSign } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { PageLayout } from '@/components/layout/PageLayout';
import { apiClient } from '@/lib/api/client';
import { FlightSearchResult } from '@/lib/api/types';
import { Loader2 } from 'lucide-react';
import { Separator } from '@/components/ui/separator';

export default function TodayFlightsPage() {
  const [flights, setFlights] = useState<FlightSearchResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadFlights = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await apiClient.getTodayFlights(50); // Load top 50 flights
      setFlights(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải chuyến bay');
      console.error('Error loading today flights:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadFlights();
  }, []);

  return (
    <PageLayout>
      <div className="flex h-screen flex-col">
        <div className="border-b bg-card p-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="mb-2 text-2xl font-semibold flex items-center gap-2">
                <Plane className="h-6 w-6 text-primary" />
                Chuyến Bay Hôm Nay
              </h1>
              <p className="text-sm text-muted-foreground">
                Danh sách các chuyến bay hôm nay (ưu tiên nội địa Việt Nam, sau đó là các chuyến bay từ Việt Nam đi nước ngoài)
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={loadFlights}
              disabled={isLoading}
            >
              <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
              Làm mới
            </Button>
          </div>
        </div>

        <ScrollArea className="flex-1">
          <div className="p-4">
            {isLoading && !flights ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                <span className="ml-3 text-muted-foreground">Đang tải chuyến bay...</span>
              </div>
            ) : error && !flights ? (
              <Card>
                <CardContent className="p-8 text-center">
                  <p className="text-destructive mb-4">{error}</p>
                  <Button variant="outline" onClick={loadFlights}>
                    Thử lại
                  </Button>
                </CardContent>
              </Card>
            ) : flights && flights.flights.length === 0 ? (
              <Card>
                <CardContent className="p-8 text-center text-muted-foreground">
                  <Plane className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p>Không có chuyến bay hôm nay</p>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-4">
                {flights && (
                  <div className="mb-4 text-sm text-muted-foreground">
                    Tìm thấy {flights.totalElements} chuyến bay
                  </div>
                )}
                <div className="grid gap-4">
                  {flights?.flights.map((flight, index) => {
                    const depTime = flight.depTime
                      ? new Date(flight.depTime).toLocaleTimeString('vi-VN', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : 'N/A';
                    const arrTime = flight.arrTime
                      ? new Date(flight.arrTime).toLocaleTimeString('vi-VN', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : 'N/A';
                    const duration = flight.durationMin
                      ? `${Math.floor(flight.durationMin / 60)}h ${flight.durationMin % 60}m`
                      : 'N/A';
                    const price = flight.priceCents
                      ? `${(flight.priceCents / 100).toLocaleString('vi-VN')} ₫`
                      : 'N/A';

                    return (
                      <Card key={flight.id || index} className="overflow-hidden">
                        <CardHeader className="pb-3">
                          <div className="flex items-center justify-between">
                            <CardTitle className="text-lg font-semibold">
                              {flight.carrier} - {flight.flightNo}
                            </CardTitle>
                            <div className="flex items-center gap-1 text-sm text-muted-foreground">
                              <Clock className="h-4 w-4" />
                              <span>{duration}</span>
                            </div>
                          </div>
                        </CardHeader>
                        <CardContent className="space-y-4">
                          <div className="flex items-center justify-between">
                            <div className="space-y-1">
                              <p className="text-2xl font-bold">{depTime}</p>
                              <div className="flex items-center gap-1 text-sm text-muted-foreground">
                                <MapPin className="h-3 w-3" />
                                <span>{flight.depIata}</span>
                              </div>
                            </div>
                            <div className="flex flex-col items-center">
                              <Plane className="h-5 w-5 text-muted-foreground mb-1" />
                              <p className="text-xs text-muted-foreground">{duration}</p>
                            </div>
                            <div className="space-y-1 text-right">
                              <p className="text-2xl font-bold">{arrTime}</p>
                              <div className="flex items-center gap-1 text-sm text-muted-foreground justify-end">
                                <MapPin className="h-3 w-3" />
                                <span>{flight.arrIata}</span>
                              </div>
                            </div>
                          </div>
                          <Separator />
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-1 text-lg font-semibold text-primary">
                              <DollarSign className="h-5 w-5" />
                              <span>{price}</span>
                            </div>
                            <Button size="sm">Xem chi tiết</Button>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        </ScrollArea>
      </div>
    </PageLayout>
  );
}

