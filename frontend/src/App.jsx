import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { RecommendationProvider } from './context/RecommendationContext';
import { Menu } from 'lucide-react';
import Sidebar from './components/Sidebar';
import Home from './pages/Home';
import Library from './pages/Library';
import Profile from './pages/Profile';
import Admin from './pages/Admin';

const App = () => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  return (
    <RecommendationProvider>
        <Router>
          <div className="flex h-screen bg-[#0f0f0f] text-gray-100 font-sans overflow-hidden">
            
            {/* Sidebar */}
            <Sidebar 
                isOpen={isSidebarOpen} 
                onClose={() => setIsSidebarOpen(false)} 
            />

            {/* Main Content Wrapper */}
            <div className="flex-1 flex flex-col min-w-0 h-full relative">
                
                {/* Mobile Header */}
                <div className="md:hidden p-4 border-b border-[#272727] flex items-center justify-between bg-[#121212] shrink-0">
                    <span className="font-bold text-white">AI Recommender</span>
                    <button 
                        onClick={() => setIsSidebarOpen(true)}
                        className="p-2 text-gray-300 hover:bg-[#272727] rounded-lg"
                    >
                        <Menu size={24} />
                    </button>
                </div>

                {/* Scrollable Page Area */}
                <main className="flex-1 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-700 scrollbar-track-transparent">
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/history" element={<Library type="history" />} />
                        <Route path="/watch-later" element={<Library type="watch-later" />} />
                        <Route path="/admin" element={<Admin />} />
                        <Route path="/profile" element={<Profile />} />
                    </Routes>
                </main>
            </div>
          </div>
        </Router>
    </RecommendationProvider>
  );
};

export default App;