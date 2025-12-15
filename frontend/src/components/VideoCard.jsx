import React, { useState } from 'react';
import { ThumbsUp, ThumbsDown, Clock, CheckCircle, Circle, PlayCircle, ExternalLink, Trash2, Info } from 'lucide-react';

const VideoCard = ({ video, interaction, onInteract, onDelete }) => {

  // --- LOCAL STATE FOR OPTIMISTIC UI ---
  const [userOverride, setUserOverride] = useState(null);

  // --- CALCULATE CURRENT STATE (Merge Server Data + User Override) ---
  
  // 1. Base values from server (defaulting to empty/false)
  const serverLikeStatus = interaction?.likeStatus || 'NONE';
  const serverWatchStatus = interaction?.watchStatus || 'NOT_WATCHED';
  const serverWatchLater = interaction?.watchLater === true;

  // 2. Final values (Override takes precedence)
  const likeStatus = userOverride?.likeStatus !== undefined ? userOverride.likeStatus : serverLikeStatus;
  const watchStatus = userOverride?.watchStatus !== undefined ? userOverride.watchStatus : serverWatchStatus;
  const watchLater = userOverride?.watchLater !== undefined ? userOverride.watchLater : serverWatchLater;

  const formatDuration = (seconds) => {
      if(!seconds) return "00:00";
      const min = Math.floor(seconds / 60);
      const sec = seconds % 60;
      return `${min}:${String(sec).padStart(2, '0')}`;
  };

  const formatPublishedDate = (dateString) => {
      if (!dateString) return "";
      return new Date(dateString).getFullYear();
  }

  // --- ACTION HANDLER ---
  const handleAction = (e, actionType) => {
      e.stopPropagation(); // Prevent opening video

      let updates = {};

      // Logic matches Backend 'RecommendationService' switch cases
      switch (actionType) {
          case 'TOGGLE_LIKE':
              updates.likeStatus = (likeStatus === 'LIKE') ? 'NONE' : 'LIKE';
              break;
          case 'TOGGLE_DISLIKE':
              updates.likeStatus = (likeStatus === 'DISLIKE') ? 'NONE' : 'DISLIKE';
              break;
          case 'TOGGLE_WATCH_LATER':
              updates.watchLater = !watchLater;
              break;
          case 'MARK_PARTIAL':
              updates.watchStatus = 'PARTIAL';
              break;
          case 'MARK_FULL':
              updates.watchStatus = 'FULL';
              break;
          default:
              return;
      }

      // Update UI instantly
      setUserOverride(prev => ({ ...prev, ...updates }));

      // Send to Backend
      onInteract(video.videoId, actionType);
  };

  return (
    <div className="group flex flex-col md:flex-row gap-4 bg-[#1f1f1f] border border-[#272727] rounded-xl p-3 hover:border-[#3f3f3f] transition-all shadow-lg hover:shadow-xl w-full relative">
      
      {/* --- LEFT COLUMN: Thumbnail & Buttons --- */}
      <div className="w-full md:w-72 flex-shrink-0 flex flex-col gap-3">
        
        {/* Thumbnail */}
        <div className="relative aspect-video rounded-lg overflow-hidden bg-[#272727]">
            <img 
                src={video.thumbnailUrl || "https://via.placeholder.com/320x180"} 
                alt={video.title} 
                className="w-full h-full object-cover group-hover:opacity-90 transition-opacity"
            />
            <div className="absolute bottom-1 right-1 bg-black/80 text-white text-xs px-1.5 py-0.5 rounded font-medium">
                {formatDuration(video.durationSeconds)}
            </div>

            {/* Status Icons Overlay */}
            <div className="absolute top-2 left-2 flex gap-1 z-10">
                {watchStatus === 'FULL' && (
                    <div className="bg-green-600 text-white p-1 rounded-full shadow-sm" title="Watched">
                        <CheckCircle size={14} />
                    </div>
                )}
                {watchStatus === 'PARTIAL' && (
                    <div className="bg-blue-600 text-white p-1 rounded-full shadow-sm" title="In Progress">
                        <PlayCircle size={14} />
                    </div>
                )}
                {watchLater && (
                    <div className="bg-yellow-600 text-white p-1 rounded-full shadow-sm" title="Watch Later">
                        <Clock size={14} />
                    </div>
                )}
            </div>

            <a href={`https://www.youtube.com/watch?v=${video.videoId}`} target="_blank" rel="noopener noreferrer" className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/30 backdrop-blur-[1px]">
                <ExternalLink className="text-white bg-red-600/80 p-2 rounded-full w-12 h-12" />
            </a>
        </div>

        {/* Button Bar */}
        <div className="flex items-center justify-between bg-[#181818] rounded-lg p-2 border border-[#272727]" onClick={(e) => e.stopPropagation()}>
            <div className="flex gap-1">
                <button onClick={(e) => handleAction(e, 'TOGGLE_LIKE')} title="Like" className={`p-1.5 rounded-full transition-colors ${likeStatus === 'LIKE' ? 'text-green-500 bg-green-500/10' : 'text-gray-400 hover:bg-[#3f3f3f] hover:text-white'}`}>
                    <ThumbsUp size={18} fill={likeStatus === 'LIKE' ? "currentColor" : "none"} />
                </button>
                <button onClick={(e) => handleAction(e, 'TOGGLE_DISLIKE')} title="Dislike" className={`p-1.5 rounded-full transition-colors ${likeStatus === 'DISLIKE' ? 'text-red-500 bg-red-500/10' : 'text-gray-400 hover:bg-[#3f3f3f] hover:text-white'}`}>
                    <ThumbsDown size={18} fill={likeStatus === 'DISLIKE' ? "currentColor" : "none"} />
                </button>
                <div className="w-px h-6 bg-[#272727] mx-1"></div>
                <button onClick={(e) => handleAction(e, 'TOGGLE_WATCH_LATER')} title="Watch Later" className={`p-1.5 rounded-full transition-colors ${watchLater ? 'text-yellow-500 bg-yellow-500/10' : 'text-gray-400 hover:bg-[#3f3f3f] hover:text-white'}`}>
                    <Clock size={18} fill={watchLater ? "currentColor" : "none"} />
                </button>
            </div>

            <div className="flex gap-1 items-center">
                <button onClick={(e) => handleAction(e, 'MARK_PARTIAL')} title="Mark Partial" className={`p-1.5 rounded-full transition-colors ${watchStatus === 'PARTIAL' ? 'text-blue-400' : 'text-gray-600 hover:text-blue-400'}`}>
                    <Circle size={18} fill={watchStatus === 'PARTIAL' ? "currentColor" : "none"}/>
                </button>
                <button onClick={(e) => handleAction(e, 'MARK_FULL')} title="Mark Watched" className={`p-1.5 rounded-full transition-colors ${watchStatus === 'FULL' ? 'text-green-500' : 'text-gray-600 hover:text-green-500'}`}>
                    <CheckCircle size={18} fill={watchStatus === 'FULL' ? "currentColor" : "none"}/>
                </button>
                {onDelete && (
                    <button onClick={(e) => { e.stopPropagation(); onDelete(video.videoId); }} className="ml-2 p-1.5 hover:bg-red-500/20 rounded-full text-gray-600 hover:text-red-500 transition-colors" title="Remove from History">
                        <Trash2 size={18} />
                    </button>
                )}
            </div>
        </div>
      </div>
      
      {/* --- RIGHT COLUMN: Details --- */}
      <div className="flex-1 flex flex-col">
        <div className="mb-auto">
            <h3 className="text-lg font-bold text-white leading-tight mb-1 line-clamp-2" title={video.title}>
                {video.title}
            </h3>
            <p className="text-sm text-gray-400 mb-3 flex items-center gap-2">
                <span className="hover:text-white transition-colors">{video.channelName}</span>
                {video.publishedAt && (
                    <>
                        <span className="w-1 h-1 rounded-full bg-gray-600"></span>
                        <span>{formatPublishedDate(video.publishedAt)}</span>
                    </>
                )}
            </p>
            <p className="text-sm text-gray-500 line-clamp-3 font-light">
                {video.description || "No description available."}
            </p>
        </div>

        {/* Status Indicators */}
        <div className="mt-4 bg-[#181818] rounded-lg p-2.5 border border-[#272727] flex flex-wrap gap-x-4 gap-y-2 text-xs">
             <div className="flex items-center gap-1.5 text-gray-500 font-semibold uppercase tracking-wider">
                <Info size={14} />
                <span>Status</span>
             </div>
             <div className="flex items-center gap-1.5">
                 <span className="text-gray-500">Like:</span>
                 <span className={`font-medium ${likeStatus === 'LIKE' ? 'text-green-400' : likeStatus === 'DISLIKE' ? 'text-red-400' : 'text-gray-500'}`}>
                     {likeStatus}
                 </span>
             </div>
             <div className="flex items-center gap-1.5">
                 <span className="text-gray-500">Progress:</span>
                 <span className={`font-medium ${watchStatus === 'FULL' ? 'text-green-400' : watchStatus === 'PARTIAL' ? 'text-blue-400' : 'text-gray-500'}`}>
                     {watchStatus.replace('_', ' ')}
                 </span>
             </div>
             <div className="flex items-center gap-1.5">
                 <span className="text-gray-500">List:</span>
                 <span className={`font-medium ${watchLater ? 'text-yellow-400' : 'text-gray-400'}`}>
                     {watchLater ? 'WATCH LATER' : '—'}
                 </span>
             </div>
        </div>
      </div>
    </div>
  );
};

export default VideoCard;