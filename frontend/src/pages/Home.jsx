import React, { useContext } from 'react';
import { Search, Sparkles, PlaySquare } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { endpoints, CURRENT_USER_ID } from '../api/axios';
import VideoCard from '../components/VideoCard';
import { RecommendationContext } from '../context/RecommendationContext';

const Home = () => {
  // Use Context so data survives navigation
  const { 
      homeQuery, setHomeQuery, 
      homeData, setHomeData, 
      loading, setLoading 
  } = useContext(RecommendationContext);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!homeQuery.trim()) return;

    setLoading(true);
    // Don't clear data immediately if you want to keep showing old results while loading
    // setHomeData(null); 

    try {
      const res = await endpoints.recommend(CURRENT_USER_ID, homeQuery);
      setHomeData(res.data);
    } catch (err) {
      console.error(err);
      alert("Backend error. Is Spring Boot running?");
    } finally {
      setLoading(false);
    }
  };

  const handleInteract = async (videoId, action) => {
      try {
        await endpoints.interact({ userId: CURRENT_USER_ID, videoId, action });
      } catch (e) { console.error(e); }
  };

  return (
    <div className="p-4 md:p-8 max-w-5xl mx-auto min-h-screen">
      
      {/* Search Header */}
      <div className="mb-12 text-center mt-6 md:mt-10">
        <h1 className="text-3xl md:text-4xl font-bold mb-6 flex items-center justify-center gap-3 text-white">
            <Sparkles className="text-red-500 w-8 h-8" /> 
            <span>AI Recommender</span>
        </h1>
        
        <form onSubmit={handleSearch} className="flex gap-2 justify-center w-full">
          <div className="relative w-full max-w-2xl">
            <input 
                type="text" 
                value={homeQuery}
                onChange={(e) => setHomeQuery(e.target.value)}
                placeholder="Ex: 'I want to build a REST API with Spring Boot'..."
                className="w-full bg-[#1f1f1f] border border-[#3f3f3f] text-white rounded-full pl-6 pr-14 py-4 focus:outline-none focus:border-red-600 focus:ring-1 focus:ring-red-600 transition-all shadow-lg text-lg placeholder-gray-500"
            />
            <button 
                type="submit" 
                disabled={loading}
                className="absolute right-2 top-2 bottom-2 bg-[#272727] hover:bg-[#3f3f3f] text-gray-300 px-5 rounded-full transition-colors flex items-center border border-[#3f3f3f]"
            >
                {loading ? <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div> : <Search size={20} />}
            </button>
          </div>
        </form>
      </div>

      {/* Results */}
      {homeData && (
        <div className="space-y-12 animate-fade-in pb-20">
            
            {/* AI Analysis */}
            <div className="bg-[#1f1f1f] rounded-2xl border border-[#3f3f3f] shadow-2xl overflow-hidden">
                <div className="bg-[#272727]/50 p-4 border-b border-[#3f3f3f] flex items-center gap-3">
                    <div className="bg-red-600/10 p-2 rounded-lg">
                        <Sparkles size={20} className="text-red-500" />
                    </div>
                    <h2 className="font-bold text-lg text-white">AI Analysis</h2>
                </div>
                <div className="p-6 md:p-8 text-gray-300 leading-relaxed">
                    <ReactMarkdown components={{ p: ({...props}) => <p className="mb-4 text-lg" {...props} /> }}>
                        {homeData.aiResponse}
                    </ReactMarkdown>
                </div>
            </div>

            {/* Videos */}
            {homeData.videos && homeData.videos.length > 0 && (
                <div>
                    <h3 className="text-2xl font-bold mb-6 flex items-center gap-2 text-white">
                        <PlaySquare className="text-red-600" size={28} /> 
                        Recommended Videos
                    </h3>
                    <div className="flex flex-col gap-4">
                        {homeData.videos.map((video) => (
                            <div key={video.videoId}>
                                <VideoCard 
                                    video={video} 
                                    interaction={null} 
                                    onInteract={handleInteract} 
                                />
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
      )}

      {!homeData && !loading && (
          <div className="text-center text-gray-600 mt-20 flex flex-col items-center">
              <Sparkles className="w-12 h-12 mb-4 opacity-20" />
              <p className="text-lg">Ready to learn? Ask me anything.</p>
          </div>
      )}
    </div>
  );
};

export default Home;