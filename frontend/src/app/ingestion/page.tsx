'use client';

import { PageLayout } from '@/components/layout/PageLayout';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { FlightIngestionTab } from '@/components/ingestion/FlightIngestionTab';
import { PolicyIngestionTab } from '@/components/ingestion/PolicyIngestionTab';

export default function IngestionPage() {
  return (
    <PageLayout>
      <div className="p-6 max-w-6xl mx-auto space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Quản Lý Dữ Liệu</h1>
          <p className="text-muted-foreground mt-2">
            Quản lý ingestion dữ liệu chuyến bay và chính sách
          </p>
        </div>

        <Tabs defaultValue="flight" className="space-y-4">
          <TabsList>
            <TabsTrigger value="flight">Flight Ingestion</TabsTrigger>
            <TabsTrigger value="policy">Policy Ingestion</TabsTrigger>
          </TabsList>

          <TabsContent value="flight" className="space-y-4">
            <FlightIngestionTab />
          </TabsContent>

          <TabsContent value="policy" className="space-y-4">
            <PolicyIngestionTab />
          </TabsContent>
        </Tabs>
      </div>
    </PageLayout>
  );
}
