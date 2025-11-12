'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Loader2, Globe, Trash2, CheckCircle2, AlertCircle, FileText } from 'lucide-react';
import { apiClient } from '@/lib/api/client';
import { toast } from 'sonner';
import { format } from 'date-fns';
import { Document } from '@/lib/api/types';

export function PolicyIngestionTab() {
  const [docType, setDocType] = useState('policy');
  const [isCrawling, setIsCrawling] = useState(false);
  const [crawlResult, setCrawlResult] = useState<{ ingested: number } | null>(null);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [documentToDelete, setDocumentToDelete] = useState<number | null>(null);

  const loadDocuments = async () => {
    setIsLoading(true);
    try {
      const docs = await apiClient.listDocuments();
      setDocuments(docs);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Lỗi khi tải danh sách tài liệu');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleCrawl = async () => {
    setIsCrawling(true);
    setCrawlResult(null);

    try {
      const result = await apiClient.crawlVna(docType);
      setCrawlResult(result);
      toast.success(`Đã crawl và ingest ${result.ingested} trang`);
      loadDocuments();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Lỗi khi crawl VNA');
      setCrawlResult(null);
    } finally {
      setIsCrawling(false);
    }
  };

  const handleDeleteClick = (id: number) => {
    setDocumentToDelete(id);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!documentToDelete) return;

    try {
      await apiClient.deleteDocument(documentToDelete);
      toast.success('Đã xóa tài liệu thành công');
      setDeleteDialogOpen(false);
      setDocumentToDelete(null);
      loadDocuments();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Lỗi khi xóa tài liệu');
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Globe className="h-5 w-5" />
            Crawl VNA Policy Pages
          </CardTitle>
          <CardDescription>
            Crawl và ingest chính sách từ trang web Vietnam Airlines
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="crawlDocType">Document Type</Label>
            <Select value={docType} onValueChange={setDocType}>
              <SelectTrigger id="crawlDocType">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="policy">Policy Document</SelectItem>
                <SelectItem value="procedure">Procedure Manual</SelectItem>
                <SelectItem value="faq">FAQ Document</SelectItem>
                <SelectItem value="terms">Terms & Conditions</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              Cần cấu hình VNA_POLICY_ALLOWLIST trong backend để crawl. URLs được phân cách bởi dấu phẩy.
            </AlertDescription>
          </Alert>

          <Button onClick={handleCrawl} disabled={isCrawling} className="w-full">
            {isCrawling ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Đang crawl...
              </>
            ) : (
              <>
                <Globe className="w-4 h-4 mr-2" />
                Bắt đầu Crawl
              </>
            )}
          </Button>

          {crawlResult && (
            <Alert>
              <CheckCircle2 className="h-4 w-4" />
              <AlertDescription>
                Đã crawl và ingest thành công {crawlResult.ingested} trang
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Danh Sách Tài Liệu Đã Upload
          </CardTitle>
          <CardDescription>
            Quản lý các tài liệu đã được upload và ingest
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
            </div>
          ) : documents.length === 0 ? (
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                Chưa có tài liệu nào được upload. Hãy upload tài liệu tại trang Upload.
              </AlertDescription>
            </Alert>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Airline</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Source</TableHead>
                    <TableHead>Uploaded At</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {documents.map((doc) => (
                    <TableRow key={doc.id}>
                      <TableCell className="font-medium">{doc.id}</TableCell>
                      <TableCell>{doc.airlineCode}</TableCell>
                      <TableCell>{doc.docType}</TableCell>
                      <TableCell className="max-w-xs truncate">{doc.sourceUrl || 'N/A'}</TableCell>
                      <TableCell>
                        {doc.createdAt
                          ? format(new Date(doc.createdAt), 'dd/MM/yyyy HH:mm')
                          : 'N/A'}
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDeleteClick(doc.id)}
                          className="text-destructive hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          <div className="mt-4">
            <Button variant="outline" onClick={loadDocuments} disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Đang tải...
                </>
              ) : (
                'Làm mới'
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xóa</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa tài liệu này? Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteConfirm} className="bg-destructive text-destructive-foreground">
              Xóa
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

