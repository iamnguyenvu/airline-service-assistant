export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  usedTools?: boolean;
  model?: string;
}

export interface Document {
  id: number;
  airlineCode: string;
  docType: string;
  sourceUrl?: string;
  versionTag?: string;
  createdAt?: string;
}

export interface FlightIngestionTestResult {
  date: string;
  provider: string;
  count: number;
  sample: Array<{
    route: string;
    flightNo: string;
    carrier: string;
    depTime: string;
    arrTime: string;
    durationMin: number;
  }>;
}

