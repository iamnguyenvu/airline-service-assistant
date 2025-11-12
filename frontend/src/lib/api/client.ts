import { Document } from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        const error = await response.json().catch(() => ({
          message: response.statusText,
          status: response.status,
        }));
        throw new Error(error.message || `HTTP ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error('Network error occurred');
    }
  }

  // Chat API
  async chatAsk(message: string, sessionId?: string, locale?: string) {
    return this.request<{
      answer: string;
      usedTools: boolean;
      model?: string;
      sessionId?: string;
      flightResults?: {
        flights: Array<{
          id?: number;
          carrier: string;
          flightNo: string;
          depIata: string;
          arrIata: string;
          depTime: string | null;
          arrTime: string | null;
          durationMin: number | null;
          priceCents: number | null;
        }>;
        totalElements: number;
        totalPages: number;
        currentPage: number;
        pageSize: number;
        hasNext: boolean;
        hasPrevious: boolean;
      };
    }>('/api/chat/ask', {
      method: 'POST',
      body: JSON.stringify({ message, sessionId, locale }),
    });
  }

  // Policy API
  async askPolicy(question: string) {
    return this.request<{ answer: string }>('/api/policy/ask', {
      method: 'POST',
      body: JSON.stringify({ question }),
    });
  }

  async uploadDocument(file: File, airlineCode: string, docType: string) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('airlineCode', airlineCode);
    formData.append('docType', docType);

    const url = `${this.baseUrl}/api/policy/upload`;
    const response = await fetch(url, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({
        message: response.statusText,
      }));
      throw new Error(error.message || `HTTP ${response.status}`);
    }

    return await response.json();
  }

  async listDocuments() {
    return this.request<Document[]>('/api/policy/documents');
  }

  async deleteDocument(id: number) {
    return this.request<{ message: string }>(`/api/policy/documents/${id}`, {
      method: 'DELETE',
    });
  }

  async crawlVna(docType: string = 'policy') {
    return this.request<{ ingested: number }>(
      `/api/policy/crawl/vna?docType=${docType}`,
      {
        method: 'POST',
      }
    );
  }

  // Flight Ingestion API
  async testFlightIngestion(
    date: string,
    provider: string = 'amadeus',
    dep?: string,
    arr?: string
  ) {
    const params = new URLSearchParams({
      date,
      provider,
    });
    if (dep) params.append('dep', dep);
    if (arr) params.append('arr', arr);

    return this.request<{
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
    }>(`/api/ingestion/test?${params.toString()}`);
  }

  // Policy Ingestion API
  async ingestRawText(text: string, metadata: Record<string, unknown>) {
    return this.request<{ message: string }>('/api/policy/ingest', {
      method: 'POST',
      body: JSON.stringify({ text, metadata }),
    });
  }

  // Flight API - Get today's flights
  async getTodayFlights(limit: number = 20) {
    return this.request<import('./types').FlightSearchResult>(
      `/api/flights/today?limit=${limit}`
    );
  }
}

export const apiClient = new ApiClient();

