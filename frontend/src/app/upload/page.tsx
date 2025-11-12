"use client";

import { useState } from "react";
import Link from "next/link";

export default function UploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [airlineCode, setAirlineCode] = useState("");
  const [docType, setDocType] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!file || !airlineCode || !docType) {
      alert("Please fill in all fields and select a file");
      return;
    }

    setIsUploading(true);
    setUploadResult(null);

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("airlineCode", airlineCode);
      formData.append("docType", docType);

      const response = await fetch("http://localhost:8080/upload", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const result = await response.text();
        setUploadResult(`Success: ${result}`);
        setFile(null);
        setAirlineCode("");
        setDocType("");
        // Reset form
        const form = document.querySelector("form") as HTMLFormElement;
        form?.reset();
      } else {
        setUploadResult(`Error: ${response.statusText}`);
      }
    } catch (error) {
      setUploadResult(`Error: ${error}`);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-2xl mx-auto">
        
        {/* Header */}
        <div className="mb-8">
          <Link href="/" className="text-blue-500 hover:text-blue-700 mb-4 inline-block">
            ← Back to Home
          </Link>
          <h1 className="text-4xl font-bold text-gray-800">Upload Documents</h1>
          <p className="text-gray-600 mt-2">
            Upload airline policy documents (PDF or TXT) to the system for AI processing
          </p>
        </div>

        {/* Upload Form */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <form onSubmit={handleUpload} className="space-y-6">
            
            <div>
              <label htmlFor="airlineCode" className="block text-sm font-medium text-gray-700 mb-2">
                Airline Code *
              </label>
              <input
                type="text"
                id="airlineCode"
                value={airlineCode}
                onChange={(e) => setAirlineCode(e.target.value)}
                placeholder="e.g., VN, AA, DL"
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              />
            </div>

            <div>
              <label htmlFor="docType" className="block text-sm font-medium text-gray-700 mb-2">
                Document Type *
              </label>
              <select
                id="docType"
                value={docType}
                onChange={(e) => setDocType(e.target.value)}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              >
                <option value="">Select document type...</option>
                <option value="POLICY">Policy Document</option>
                <option value="PROCEDURE">Procedure Manual</option>
                <option value="FAQ">FAQ Document</option>
                <option value="TERMS">Terms & Conditions</option>
                <option value="GUIDE">User Guide</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div>
              <label htmlFor="file" className="block text-sm font-medium text-gray-700 mb-2">
                Document File *
              </label>
              <input
                type="file"
                id="file"
                onChange={handleFileChange}
                accept=".pdf,.txt,.doc,.docx"
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              />
              <p className="text-sm text-gray-500 mt-1">
                Supported formats: PDF, TXT, DOC, DOCX
              </p>
            </div>

            {file && (
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="text-sm text-gray-700">
                  <strong>Selected file:</strong> {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                </p>
              </div>
            )}

            <button
              type="submit"
              disabled={isUploading}
              className="w-full bg-blue-500 text-white py-3 px-6 rounded-lg hover:bg-blue-600 disabled:bg-blue-300 disabled:cursor-not-allowed transition-colors"
            >
              {isUploading ? "Uploading..." : "Upload Document"}
            </button>
          </form>
        </div>

        {/* Upload Result */}
        {uploadResult && (
          <div className={`mt-6 p-4 rounded-lg ${uploadResult.startsWith("Success") ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
            {uploadResult}
          </div>
        )}

        {/* Info Section */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-3">How it works</h2>
          <ul className="space-y-2 text-gray-700">
            <li>• Documents are processed with Apache Tika for text extraction</li>
            <li>• Content is split into chunks and converted to vectors</li>
            <li>• Vectors are stored in PostgreSQL with pgvector for similarity search</li>
            <li>• AI can then answer questions based on uploaded content</li>
          </ul>
        </div>

      </div>
    </div>
  );
}