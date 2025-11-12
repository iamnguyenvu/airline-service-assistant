'use client';

import { useState, useEffect } from 'react';
import { Plane, Clock, RefreshCw } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { apiClient } from '@/lib/api/client';
import { FlightSearchResult } from '@/lib/api/types';
import { Loader2 } from 'lucide-react';

interface TodayFlightsProps {
  collapsed?: boolean;
}

export function TodayFlights({ collapsed = false }: TodayFlightsProps) {
  const [flights, setFlights] = useState<FlightSearchResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadFlights = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await apiClient.getTodayFlights(10); // Load top 10 flights
      setFlights(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải chuyến bay');
      console.error('Error loading today flights:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    // Load flights on mount, but only once per session to avoid excessive API calls
    loadFlights();
  }, []);

  if (collapsed) {
    return null; // Don't show when sidebar is collapsed
  }

  if (isLoading && !flights) {
    return (
      <div className="p-4 flex items-center justify-center">
        <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error && !flights) {
    return (
      <div className="p-4 text-center text-sm text-muted-foreground">
        <p>{error}</p>
        <Button variant="ghost" size="sm" onClick={loadFlights} className="mt-2">
          Thử lại
        </Button>
      </div>
    );
  }

  const flightList = flights?.flights || [];

  return (
    <div className="border-t p-4 space-y-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold flex items-center gap-2">
          <Plane className="h-4 w-4" />
          Chuyến Bay Hôm Nay
        </h3>
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={loadFlights}
          disabled={isLoading}
        >
          <RefreshCw className={`h-3 w-3 ${isLoading ? 'animate-spin' : ''}`} />
        </Button>
      </div>

      {flightList.length === 0 ? (
        <p className="text-xs text-muted-foreground text-center py-4">
          Không có chuyến bay hôm nay
        </p>
      ) : (
        <ScrollArea className="h-[300px]">
          <div className="space-y-2">
            {flightList.slice(0, 10).map((flight, index) => {
              const depTime = flight.depTime
                ? new Date(flight.depTime).toLocaleTimeString('vi-VN', {
                    hour: '2-digit',
                    minute: '2-digit',
                  })
                : 'N/A';
              const price = flight.priceCents
                ? `${(flight.priceCents / 100).toLocaleString('vi-VN')} ₫`
                : 'N/A';

              return (
                <Card key={flight.id || index} className="p-2">
                  <CardContent className="p-0">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-medium truncate">
                          {flight.carrier} {flight.flightNo}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {flight.depIata} → {flight.arrIata}
                        </p>
                      </div>
                      <div className="text-right ml-2">
                        <p className="text-xs font-semibold">{depTime}</p>
                        <p className="text-xs text-muted-foreground">{price}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </ScrollArea>
      )}

      {flights && flights.totalElements > 10 && (
        <p className="text-xs text-muted-foreground text-center">
          Hiển thị 10/{flights.totalElements} chuyến bay
        </p>
      )}
    </div>
  );
}

