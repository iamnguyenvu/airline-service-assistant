'use client';

import { Menu, Plane, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Sheet,
  SheetContent,
  SheetTrigger,
} from '@/components/ui/sheet';
import { Sidebar } from './Sidebar';

export function MobileHeader() {
  return (
    <header className="sticky top-0 z-40 flex h-14 items-center justify-between border-b bg-background px-4 md:hidden">
      <Sheet>
        <SheetTrigger asChild>
          <Button variant="ghost" size="icon">
            <Menu className="h-5 w-5" />
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="w-60 p-0">
          <div className="h-full">
            <Sidebar collapsed={false} onToggleCollapse={() => {}} />
          </div>
        </SheetContent>
      </Sheet>

      <div className="flex items-center gap-2">
        <Plane className="h-5 w-5 text-primary" />
        <span className="font-semibold">Airline AI</span>
      </div>

      <Button variant="ghost" size="icon">
        <Plus className="h-5 w-5" />
      </Button>
    </header>
  );
}
