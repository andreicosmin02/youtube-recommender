import React, { useState } from 'react';
import { endpoints } from '../api/axios';
import { Database, Loader2, CheckCircle } from 'lucide-react';

const Admin = () => {
  const [topic, setTopic] = useState('');
  const [max, setMax] = useState(5);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleIngest = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    
    try {
      const res = await endpoints.ingest(topic, max);
      setMessage(res.data); // "Successfully ingested..."
    } catch (err) {
      console.error(err);
      setMessage("Error: Could not ingest videos. Check backend logs.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-10 max-w-2xl mx-auto">
      <div className="bg-[#1f1f1f] border border-[#3f3f3f] rounded-2xl p-8 shadow-xl">
        <div className="flex items-center gap-3 mb-6 border-b border-[#3f3f3f] pb-4">
            <Database className="text-red-500" size={32} />
            <div>
                <h1 className="text-2xl font-bold text-white">Content Ingestion</h1>
                <p className="text-gray-400 text-sm">Scrape YouTube, Summarize with AI, and Vectorize.</p>
            </div>
        </div>

        <form onSubmit={handleIngest} className="space-y-6">
            <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Topic / Search Query</label>
                <input 
                    type="text" 
                    value={topic}
                    onChange={(e) => setTopic(e.target.value)}
                    placeholder="e.g. 'Advanced Spring Boot Security'"
                    className="w-full bg-[#0f0f0f] border border-gray-600 rounded-lg p-3 text-white focus:border-red-500 focus:outline-none"
                    required
                />
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Max Videos to Fetch</label>
                <select 
                    value={max}
                    onChange={(e) => setMax(e.target.value)}
                    className="w-full bg-[#0f0f0f] border border-gray-600 rounded-lg p-3 text-white focus:border-red-500 focus:outline-none"
                >
                    <option value="3">3 Videos</option>
                    <option value="5">5 Videos</option>
                    <option value="10">10 Videos (Slow)</option>
                </select>
            </div>

            <button 
                type="submit" 
                disabled={loading}
                className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-lg transition-colors flex justify-center items-center gap-2"
            >
                {loading ? <><Loader2 className="animate-spin" /> Processing...</> : 'Start Ingestion Pipeline'}
            </button>
        </form>

        {message && (
            <div className={`mt-6 p-4 rounded-lg flex items-center gap-3 ${message.includes("Error") ? "bg-red-900/30 text-red-200" : "bg-green-900/30 text-green-200"}`}>
                {message.includes("Error") ? null : <CheckCircle size={20} />}
                {message}
            </div>
        )}
      </div>
    </div>
  );
};

export default Admin;