import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, History, Clock, User, X, Sparkles, LogOut, Database } from 'lucide-react';

const Sidebar = ({ isOpen, onClose }) => {
    
    // Updated classes: Flexbox layout to push footer to bottom naturally
    const sidebarClasses = `
        fixed top-0 left-0 h-full bg-[#121212] border-r border-[#272727] w-64 z-50 
        transform transition-transform duration-300 ease-in-out flex flex-col
        ${isOpen ? 'translate-x-0' : '-translate-x-full'} 
        md:translate-x-0 md:static md:flex
    `;

    const navItems = [
        { path: '/', icon: <Home size={20} />, label: 'Home' },
        { path: '/history', icon: <History size={20} />, label: 'History' },
        { path: '/watch-later', icon: <Clock size={20} />, label: 'Watch Later' }, // Ensure this is here
        { path: '/admin', icon: <Database size={20} />, label: 'Ingest' },
        { path: '/profile', icon: <User size={20} />, label: 'Profile' },
    ];

    return (
        <>
            {/* Mobile Overlay */}
            {isOpen && (
                <div 
                    className="fixed inset-0 bg-black/50 z-40 md:hidden backdrop-blur-sm"
                    onClick={onClose}
                ></div>
            )}

            <aside className={sidebarClasses}>
                {/* Header */}
                <div className="p-6 flex items-center justify-between shrink-0">
                    <div className="flex items-center gap-2 font-bold text-xl text-white">
                         <Sparkles className="text-red-600 fill-red-600" />
                         <span>Recommender</span>
                    </div>
                    <button onClick={onClose} className="md:hidden text-gray-400 hover:text-white">
                        <X size={24} />
                    </button>
                </div>

                {/* Navigation Items (Scrollable if needed) */}
                <nav className="flex-1 px-4 space-y-2 overflow-y-auto">
                    {navItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            onClick={() => onClose && window.innerWidth < 768 && onClose()}
                            className={({ isActive }) => `
                                flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium
                                ${isActive 
                                    ? 'bg-red-600 text-white shadow-lg shadow-red-900/20' 
                                    : 'text-gray-400 hover:bg-[#1f1f1f] hover:text-white'
                                }
                            `}
                        >
                            {item.icon}
                            <span>{item.label}</span>
                        </NavLink>
                    ))}
                </nav>

                {/* Footer User Section (Fixed at bottom via flex-col) */}
                <div className="p-4 border-t border-[#272727] shrink-0">
                    <div className="bg-[#1f1f1f] rounded-xl p-3 flex items-center gap-3 border border-[#272727]">
                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-red-500 to-purple-600 flex items-center justify-center text-white font-bold">
                            U1
                        </div>
                        <div className="flex-1 overflow-hidden">
                            <p className="text-sm font-bold text-white truncate">Demo User</p>
                            <p className="text-xs text-gray-500 truncate">user@demo.com</p>
                        </div>
                        <button className="text-gray-500 hover:text-white transition-colors">
                            <LogOut size={18} />
                        </button>
                    </div>
                </div>
            </aside>
        </>
    );
};

export default Sidebar;