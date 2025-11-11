'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Loader2, Play, CheckCircle2, AlertCircle, Plane } from 'lucide-react';
import { apiClient } from '@/lib/api/client';
import { toast } from 'sonner';
import { format } from 'date-fns';

export function FlightIngestionTab() {
  const [date, setDate] = useState(() => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return format(tomorrow, 'yyyy-MM-dd');
  });
  const [provider, setProvider] = useState('amadeus');
  const [dep, setDep] = useState('');
  const [arr, setArr] = useState('');
  const [isTesting, setIsTesting] = useState(false);
  const [result, setResult] = useState<any>(null);

  const handleTest = async () => {
    if (!date) {
      toast.error('Vui lòng chọn ngày');
      return;
    }

    setIsTesting(true);
    setResult(null);

    try {
      const data = await apiClient.testFlightIngestion(date, provider, dep || undefined, arr || undefined);
      setResult(data);
      toast.success(`Tìm thấy ${data.count} chuyến bay`);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Lỗi khi test ingestion');
      setResult(null);
    } finally {
      setIsTesting(false);
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Plane className="h-5 w-5" />
            Test Flight Ingestion
          </CardTitle>
          <CardDescription>
            Test ingestion provider để lấy dữ liệu chuyến bay (không lưu vào database)
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="date">Ngày</Label>
              <Input
                id="date"
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="provider">Provider</Label>
              <Select value={provider} onValueChange={setProvider}>
                <SelectTrigger id="provider">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="amadeus">Amadeus</SelectItem>
                  <SelectItem value="aviationstack">Aviationstack</SelectItem>
                  <SelectItem value="mock">Mock</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="dep">Sân bay đi (IATA) - Tùy chọn</Label>
              <Input
                id="dep"
                value={dep}
                onChange={(e) => setDep(e.target.value.toUpperCase())}
                placeholder="VD: SGN"
                maxLength={3}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="arr">Sân bay đến (IATA) - Tùy chọn</Label>
              <Input
                id="arr"
                value={arr}
                onChange={(e) => setArr(e.target.value.toUpperCase())}
                placeholder="VD: HAN"
                maxLength={3}
              />
            </div>
          </div>

          <Button onClick={handleTest} disabled={isTesting} className="w-full">
            {isTesting ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Đang test...
              </>
            ) : (
              <>
                <Play className="w-4 h-4 mr-2" />
                Test Ingestion
              </>
            )}
          </Button>
        </CardContent>
      </Card>

      {result && (
        <Card>
          <CardHeader>
            <CardTitle>Kết quả</CardTitle>
            <CardDescription>
              Provider: {result.provider} | Ngày: {result.date} | Tổng: {result.count} chuyến
            </CardDescription>
          </CardHeader>
          <CardContent>
            {result.count === 0 ? (
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  Không tìm thấy chuyến bay nào. Vui lòng kiểm tra lại ngày và route.
                </AlertDescription>
              </Alert>
            ) : (
              <div className="space-y-4">
                <Alert>
                  <CheckCircle2 className="h-4 w-4" />
                  <AlertDescription>
                    Tìm thấy {result.count} chuyến bay. Hiển thị {Math.min(3, result.sample.length)} mẫu:
                  </AlertDescription>
                </Alert>

                <div className="rounded-md border">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Route</TableHead>
                        <TableHead>Flight No</TableHead>
                        <TableHead>Carrier</TableHead>
                        <TableHead>Departure</TableHead>
                        <TableHead>Arrival</TableHead>
                        <TableHead>Duration</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {result.sample.map((flight: any, index: number) => (
                        <TableRow key={index}>
                          <TableCell className="font-medium">{flight.route}</TableCell>
                          <TableCell>{flight.flightNo}</TableCell>
                          <TableCell>{flight.carrier}</TableCell>
                          <TableCell>
                            {flight.depTime
                              ? format(new Date(flight.depTime), 'HH:mm')
                              : 'N/A'}
                          </TableCell>
                          <TableCell>
                            {flight.arrTime
                              ? format(new Date(flight.arrTime), 'HH:mm')
                              : 'N/A'}
                          </TableCell>
                          <TableCell>{flight.durationMin} phút</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}

