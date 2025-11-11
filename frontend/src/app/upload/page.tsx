'use client';

import { useState } from 'react';
import { PageLayout } from '@/components/layout/PageLayout';
import { apiClient } from '@/lib/api/client';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Upload, Loader2, CheckCircle2, AlertCircle, FileText } from 'lucide-react';
import { toast } from 'sonner';

export default function UploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [airlineCode, setAirlineCode] = useState('');
  const [docType, setDocType] = useState('policy');
  const [isUploading, setIsUploading] = useState(false);
  const [uploadSuccess, setUploadSuccess] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
      setUploadSuccess(false);
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!file || !airlineCode || !docType) {
      toast.error('Please fill in all fields and select a file');
      return;
    }

    setIsUploading(true);
    setUploadSuccess(false);

    try {
      const result = await apiClient.uploadDocument(file, airlineCode, docType);
      toast.success(result.message || 'Document uploaded successfully');
      setUploadSuccess(true);
      
      // Reset form
      setFile(null);
      setAirlineCode('');
      setDocType('policy');
      const form = e.target as HTMLFormElement;
      form.reset();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Failed to upload document');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <PageLayout>
      <div className="p-6 max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Upload Policy Documents</h1>
        <p className="text-muted-foreground mt-2">
          Upload airline policy documents (PDF, TXT, DOC, DOCX) for AI processing
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Document Upload</CardTitle>
          <CardDescription>
            Documents will be processed and indexed for AI-powered Q&A
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleUpload} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="airlineCode">Airline Code *</Label>
              <Input
                id="airlineCode"
                value={airlineCode}
                onChange={(e) => setAirlineCode(e.target.value.toUpperCase())}
                placeholder="e.g., VN, VJ, QH"
                maxLength={3}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="docType">Document Type *</Label>
              <Select value={docType} onValueChange={setDocType}>
                <SelectTrigger id="docType">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="policy">Policy Document</SelectItem>
                  <SelectItem value="procedure">Procedure Manual</SelectItem>
                  <SelectItem value="faq">FAQ Document</SelectItem>
                  <SelectItem value="terms">Terms & Conditions</SelectItem>
                  <SelectItem value="guide">User Guide</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="file">Document File *</Label>
              <div className="flex items-center justify-center w-full">
                <label
                  htmlFor="file"
                  className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed rounded-lg cursor-pointer bg-muted hover:bg-muted/50 transition-colors"
                >
                  <div className="flex flex-col items-center justify-center pt-5 pb-6">
                    <Upload className="w-8 h-8 mb-2 text-muted-foreground" />
                    <p className="mb-2 text-sm text-muted-foreground">
                      <span className="font-semibold">Click to upload</span> or drag and drop
                    </p>
                    <p className="text-xs text-muted-foreground">
                      PDF, TXT, DOC, DOCX (MAX. 10MB)
                    </p>
                  </div>
                  <input
                    id="file"
                    type="file"
                    className="hidden"
                    accept=".pdf,.txt,.doc,.docx"
                    onChange={handleFileChange}
                    required
                  />
                </label>
              </div>
              {file && (
                <div className="flex items-center gap-2 p-3 bg-muted rounded-lg">
                  <FileText className="w-4 h-4 text-muted-foreground" />
                  <span className="text-sm flex-1">{file.name}</span>
                  <span className="text-xs text-muted-foreground">
                    {(file.size / 1024 / 1024).toFixed(2)} MB
                  </span>
                </div>
              )}
            </div>

            <Button type="submit" disabled={isUploading} className="w-full">
              {isUploading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Uploading...
                </>
              ) : (
                <>
                  <Upload className="w-4 h-4 mr-2" />
                  Upload Document
                </>
              )}
            </Button>
          </form>

          {uploadSuccess && (
            <Alert className="mt-4">
              <CheckCircle2 className="h-4 w-4" />
              <AlertDescription>
                Document uploaded and processed successfully! It will be available for AI Q&A shortly.
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>How it works</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li className="flex items-start gap-2">
              <span className="text-primary">•</span>
              <span>Documents are processed with Apache Tika for text extraction</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-primary">•</span>
              <span>Content is split into chunks and converted to vectors</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-primary">•</span>
              <span>Vectors are stored in PostgreSQL with pgvector for similarity search</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-primary">•</span>
              <span>AI can then answer questions based on uploaded content</span>
            </li>
          </ul>
        </CardContent>
      </Card>
      </div>
    </PageLayout>
  );
}
