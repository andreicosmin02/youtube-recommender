import React, { useEffect, useState } from 'react';
import { endpoints, CURRENT_USER_ID } from '../api/axios';
import VideoCard from '../components/VideoCard';
import { History, Clock, AlertCircle } from 'lucide-react';

const Library = ({ type }) => {
  const [items, setItems] = useState([]); 
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, [type]);

  const fetchData = async () => {
    setLoading(true);
    setItems([]);
    try {
      let res;
      if (type === 'history') {
          res = await endpoints.history(CURRENT_USER_ID);
      } else {
          res = await endpoints.watchLater(CURRENT_USER_ID);
      }
      
      // Deduplicate by Video ID
      const uniqueItemsMap = new Map();
      res.data.forEach(item => {
          if (item.video && item.video.videoId) {
              uniqueItemsMap.set(item.video.videoId, item);
          }
      });
      setItems(Array.from(uniqueItemsMap.values()));

    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleInteract = async (videoId, action) => {
    try {
        // We now send 'action' which maps to InteractionAction enum in Backend
        await endpoints.interact({ userId: CURRENT_USER_ID, videoId, action });
    } catch (e) {
        console.error(e);
    }
  };

  const handleDelete = async (videoId) => {
      if(!confirm("Are you sure you want to remove this from history?")) return;
      try {
          await endpoints.deleteInteraction(CURRENT_USER_ID, videoId);
          setItems(prev => prev.filter(item => item.video.videoId !== videoId));
      } catch (err) {
          console.error("Delete failed", err);
      }
  };

  return (
    <div className="p-8 max-w-5xl mx-auto min-h-screen">
      <div className="flex items-center gap-3 mb-8">
        {type === 'history' ? <History className="text-red-500" size={32}/> : <Clock className="text-red-500" size={32}/>}
        <h1 className="text-3xl font-bold text-white capitalize">{type.replace('-', ' ')}</h1>
      </div>

      {loading ? (
          <div className="text-gray-500 animate-pulse">Loading library...</div>
      ) : (
          <>
            <div className="flex flex-col gap-4">
                {items.map((interaction) => (
                   <div key={interaction.video.videoId}>
                        <VideoCard 
                            video={interaction.video} 
                            interaction={interaction}
                            onInteract={handleInteract} 
                            onDelete={type === 'history' ? handleDelete : null} 
                        />
                   </div>
                ))}
            </div>
            
            {items.length === 0 && (
                <div className="text-center mt-20 text-gray-500 flex flex-col items-center">
                    <AlertCircle size={48} className="mb-4 opacity-50"/>
                    <p className="text-xl">Your list is empty.</p>
                </div>
            )}
          </>
      )}
    </div>
  );
};

export default Library;