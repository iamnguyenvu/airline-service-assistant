'use client';

import { useState, useEffect } from 'react';
import { Plane, RefreshCw, Clock, ArrowRight, Calendar, TrendingUp } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { PageLayout } from '@/components/layout/PageLayout';
import { apiClient } from '@/lib/api/client';
import { FlightSearchResult } from '@/lib/api/types';
import { Loader2 } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

export default function TodayFlightsPage() {
  const [flights, setFlights] = useState<FlightSearchResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadFlights = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await apiClient.getTodayFlights(50);
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

  const today = new Date().toLocaleDateString('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  return (
    <PageLayout>
      <div className="flex h-screen flex-col bg-gradient-to-b from-background to-muted/20">
        {/* Modern Header */}
        <div className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border-b p-6">
          <div className="max-w-6xl mx-auto">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-xl bg-gradient-to-br from-primary to-primary/70 text-primary-foreground shadow-lg shadow-primary/25">
                    <Plane className="h-6 w-6" />
                  </div>
                  <div>
                    <h1 className="text-2xl font-bold tracking-tight">Chuyến Bay Hôm Nay</h1>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground mt-0.5">
                      <Calendar className="h-3.5 w-3.5" />
                      <span>{today}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                {flights && (
                  <Badge variant="secondary" className="text-sm py-1.5 px-3">
                    <TrendingUp className="h-3.5 w-3.5 mr-1.5" />
                    {flights.totalElements} chuyến bay
                  </Badge>
                )}
                <Button
                  variant="outline"
                  size="sm"
                  onClick={loadFlights}
                  disabled={isLoading}
                  className="gap-2"
                >
                  <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
                  Làm mới
                </Button>
              </div>
            </div>
          </div>
        </div>

        <ScrollArea className="flex-1">
          <div className="p-6 max-w-6xl mx-auto">
            {isLoading && !flights ? (
              <div className="flex flex-col items-center justify-center py-16">
                <div className="p-4 rounded-full bg-muted mb-4">
                  <Loader2 className="h-8 w-8 animate-spin text-primary" />
                </div>
                <span className="text-muted-foreground">Đang tải chuyến bay...</span>
              </div>
            ) : error && !flights ? (
              <Card className="border-destructive/50">
                <CardContent className="p-8 text-center">
                  <div className="p-3 rounded-full bg-destructive/10 w-fit mx-auto mb-4">
                    <Plane className="h-8 w-8 text-destructive" />
                  </div>
                  <p className="text-destructive mb-4">{error}</p>
                  <Button variant="outline" onClick={loadFlights}>
                    Thử lại
                  </Button>
                </CardContent>
              </Card>
            ) : flights && flights.flights.length === 0 ? (
              <Card>
                <CardContent className="p-12 text-center">
                  <div className="p-4 rounded-full bg-muted w-fit mx-auto mb-4">
                    <Plane className="h-10 w-10 text-muted-foreground" />
                  </div>
                  <h3 className="text-lg font-semibold mb-2">Không có chuyến bay</h3>
                  <p className="text-muted-foreground">
                    Hiện tại chưa có dữ liệu chuyến bay hôm nay
                  </p>
                </CardContent>
              </Card>
            ) : (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {flights?.flights.map((flight, index) => {
                  const depTime = flight.depTime
                    ? new Date(flight.depTime).toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })
                    : '--:--';
                  const arrTime = flight.arrTime
                    ? new Date(flight.arrTime).toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })
                    : '--:--';
                  const duration = flight.durationMin
                    ? `${Math.floor(flight.durationMin / 60)}h ${flight.durationMin % 60}m`
                    : 'N/A';
                  const price = flight.priceCents
                    ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(flight.priceCents / 100)
                    : 'Liên hệ';

                  return (
                    <Card 
                      key={flight.id || index} 
                      className="group overflow-hidden hover:shadow-lg hover:shadow-primary/5 transition-all duration-300 hover:border-primary/30"
                    >
                      <CardContent className="p-0">
                        {/* Airline header */}
                        <div className="bg-gradient-to-r from-muted/50 to-transparent px-4 py-3 border-b">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center">
                                <Plane className="h-4 w-4 text-primary" />
                              </div>
                              <div>
                                <p className="font-semibold text-sm">{flight.carrier}</p>
                                <p className="text-xs text-muted-foreground">{flight.flightNo}</p>
                              </div>
                            </div>
                            <Badge variant="outline" className="text-xs">
                              <Clock className="h-3 w-3 mr-1" />
                              {duration}
                            </Badge>
                          </div>
                        </div>

                        {/* Flight route */}
                        <div className="px-4 py-4">
                          <div className="flex items-center justify-between">
                            <div className="text-center">
                              <p className="text-2xl font-bold tracking-tight">{depTime}</p>
                              <p className="text-lg font-semibold text-primary">{flight.depIata}</p>
                            </div>
                            
                            <div className="flex-1 mx-4 relative">
                              <div className="border-t-2 border-dashed border-muted-foreground/30" />
                              <div className="absolute left-1/2 -translate-x-1/2 -top-2.5 bg-background px-2">
                                <ArrowRight className="h-4 w-4 text-muted-foreground" />
                              </div>
                            </div>
                            
                            <div className="text-center">
                              <p className="text-2xl font-bold tracking-tight">{arrTime}</p>
                              <p className="text-lg font-semibold text-primary">{flight.arrIata}</p>
                            </div>
                          </div>
                        </div>

                        {/* Price footer */}
                        <div className="px-4 py-3 bg-gradient-to-r from-primary/5 to-transparent border-t flex items-center justify-between">
                          <div>
                            <p className="text-xs text-muted-foreground">Giá từ</p>
                            <p className="text-lg font-bold text-primary">{price}</p>
                          </div>
                          <Button 
                            size="sm" 
                            className="rounded-full px-4 group-hover:bg-primary group-hover:text-primary-foreground transition-colors"
                            variant="outline"
                          >
                            Chi tiết
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}
          </div>
        </ScrollArea>
      </div>
    </PageLayout>
  );
}

