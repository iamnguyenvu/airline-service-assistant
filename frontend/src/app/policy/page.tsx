"use client";

import Image from "next/image";
import { useState } from "react";

export default function Home() {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim()) return;

    setIsLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/policy/ask", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ question }),
      });

      if (response.ok) {
        const data = await response.json();
        setAnswer(data.answer);
      } else {
        setAnswer("Error: Unable to get answer. Please try again.");
      }
    } catch (error) {
      setAnswer("Error: Unable to connect to server. Please check if the backend is running.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-8">
          <Image
            className="mx-auto mb-4 dark:invert"
            src="/next.svg"
            alt="Airline Assistant Logo"
            width={120}
            height={30}
            priority
          />
          <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
            ✈️ Airline Service Assistant
          </h1>
          <p className="text-lg text-gray-600 dark:text-gray-300">
            AI-Powered Policy Q&A System
          </p>
        </div>

        {/* Main Content */}
        <div className="max-w-4xl mx-auto">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 mb-6">
            <h2 className="text-2xl font-semibold mb-4 text-gray-800 dark:text-white">
              Ask about airline policies
            </h2>
            
            <form onSubmit={handleAskQuestion} className="space-y-4">
              <div>
                <label htmlFor="question" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Your Question
                </label>
                <textarea
                  id="question"
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  placeholder="e.g., What is the baggage allowance for economy class passengers?"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  rows={3}
                  disabled={isLoading}
                />
              </div>
              
              <button
                type="submit"
                disabled={isLoading || !question.trim()}
                className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? "Thinking..." : "Ask Question"}
              </button>
            </form>
          </div>

          {/* Answer Section */}
          {(answer || isLoading) && (
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6">
              <h3 className="text-xl font-semibold mb-4 text-gray-800 dark:text-white">
                Answer
              </h3>
              {isLoading ? (
                <div className="flex items-center space-x-2">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                  <span className="text-gray-600 dark:text-gray-400">Processing your question...</span>
                </div>
              ) : (
                <div className="prose dark:prose-invert max-w-none">
                  <p className="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
                    {answer}
                  </p>
                </div>
              )}
            </div>
          )}
          
          {/* Features Section */}
          <div className="mt-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
              <h3 className="font-semibold text-gray-800 dark:text-white mb-2">🧠 AI-Powered</h3>
              <p className="text-gray-600 dark:text-gray-400 text-sm">
                Uses Google Gemini AI for intelligent responses
              </p>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
              <h3 className="font-semibold text-gray-800 dark:text-white mb-2">🔍 Vector Search</h3>
              <p className="text-gray-600 dark:text-gray-400 text-sm">
                Finds relevant policy information using semantic search
              </p>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
              <h3 className="font-semibold text-gray-800 dark:text-white mb-2">📚 Comprehensive</h3>
              <p className="text-gray-600 dark:text-gray-400 text-sm">
                Covers baggage, booking, refund, and change policies
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}