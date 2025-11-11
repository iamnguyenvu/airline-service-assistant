'use client';

import { useState } from 'react';
import { PageLayout } from '@/components/layout/PageLayout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Loader2, MessageSquare } from 'lucide-react';
import { apiClient } from '@/lib/api/client';
import { toast } from 'sonner';

export default function PolicyPage() {
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim()) {
      toast.error('Vui lòng nhập câu hỏi');
      return;
    }

    setIsLoading(true);
    setAnswer('');

    try {
      const data = await apiClient.askPolicy(question);
      setAnswer(data.answer);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Không thể kết nối đến server';
      setAnswer(`Lỗi: ${errorMessage}`);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <PageLayout>
      <div className="p-6 max-w-4xl mx-auto space-y-6">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <MessageSquare className="h-8 w-8" />
            Policy Q&A
          </h1>
          <p className="text-muted-foreground mt-2">
            Hỏi đáp về chính sách hãng hàng không với AI
          </p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Đặt Câu Hỏi</CardTitle>
            <CardDescription>
              Nhập câu hỏi về chính sách hành lý, đổi vé, hoàn vé, v.v.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleAskQuestion} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="question">Câu hỏi của bạn</Label>
                <Textarea
                  id="question"
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  placeholder="Ví dụ: VNA cho phép mang bao nhiêu kg hành lý xách tay?"
                  rows={4}
                  disabled={isLoading}
                  className="resize-none"
                />
              </div>

              <Button type="submit" disabled={isLoading || !question.trim()} className="w-full">
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Đang xử lý...
                  </>
                ) : (
                  <>
                    <MessageSquare className="w-4 h-4 mr-2" />
                    Gửi Câu Hỏi
                  </>
                )}
              </Button>
            </form>
          </CardContent>
        </Card>

        {(answer || isLoading) && (
          <Card>
            <CardHeader>
              <CardTitle>Câu Trả Lời</CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex items-center gap-2 py-4">
                  <Loader2 className="w-5 h-5 animate-spin text-primary" />
                  <span className="text-muted-foreground">Đang xử lý câu hỏi của bạn...</span>
                </div>
              ) : (
                <div className="prose prose-sm max-w-none dark:prose-invert">
                  <p className="whitespace-pre-wrap text-foreground">{answer}</p>
                </div>
              )}
            </CardContent>
          </Card>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">🧠 AI-Powered</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Sử dụng Google Gemini AI để trả lời thông minh
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">🔍 Vector Search</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Tìm thông tin chính sách liên quan bằng semantic search
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">📚 Toàn Diện</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Bao gồm hành lý, đặt vé, hoàn vé, đổi vé
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </PageLayout>
  );
}