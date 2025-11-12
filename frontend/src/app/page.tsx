import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-2xl mx-auto text-center">
        <h1 className="text-4xl font-bold mb-8">Airline Service Assistant</h1>
        <div className="space-y-4">
          <Link href="/policy" className="block p-4 bg-blue-500 text-white rounded-lg">
            Policy Q&A
          </Link>
          <Link href="/upload" className="block p-4 bg-green-500 text-white rounded-lg">
            Upload Documents  
          </Link>
        </div>
      </div>
    </div>
  );
}
