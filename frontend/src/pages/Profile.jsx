import React, { useEffect, useState } from 'react';
import { User, Mail, Calendar, ShieldCheck, AlertTriangle } from 'lucide-react';
import { endpoints, CURRENT_USER_ID } from '../api/axios';

const Profile = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const res = await endpoints.getUser(CURRENT_USER_ID);
                setUser(res.data);
            } catch (err) {
                console.error(err);
                setError("Could not load user profile.");
            } finally {
                setLoading(false);
            }
        };
        fetchUser();
    }, []);

    if (loading) return <div className="p-8 text-gray-500 animate-pulse">Loading profile...</div>;

    if (error || !user) return (
        <div className="p-8 flex flex-col items-center justify-center text-center mt-10">
            <div className="bg-red-500/10 p-4 rounded-full mb-4">
                <AlertTriangle className="text-red-500" size={48} />
            </div>
            <h2 className="text-2xl font-bold text-white mb-2">User Not Found</h2>
            <p className="text-gray-400 max-w-md">
                It seems User #{CURRENT_USER_ID} does not exist in the database.
            </p>
            <p className="text-gray-500 text-sm mt-4 bg-[#1f1f1f] p-4 rounded-lg font-mono">
                INSERT INTO app_users (user_id, email, username, password_hash) <br/>
                VALUES (1, 'demo@example.com', 'Demo User', 'hash');
            </p>
        </div>
    );

    return (
        <div className="p-4 md:p-8 max-w-4xl mx-auto">
            <h1 className="text-3xl font-bold text-white mb-8 flex items-center gap-3">
                <User className="text-red-500" size={32} />
                User Profile
            </h1>

            <div className="bg-[#1f1f1f] border border-[#272727] rounded-2xl overflow-hidden shadow-2xl relative">
                {/* Banner */}
                <div className="h-32 bg-gradient-to-r from-red-900 to-black"></div>
                
                <div className="px-6 md:px-8 pb-8">
                    {/* Avatar */}
                    <div className="relative -mt-12 mb-4">
                        <div className="w-24 h-24 bg-[#272727] rounded-full border-4 border-[#1f1f1f] flex items-center justify-center text-white text-3xl font-bold shadow-lg">
                            {user.username ? user.username.charAt(0).toUpperCase() : 'U'}
                        </div>
                    </div>

                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
                        <div>
                            <h2 className="text-3xl font-bold text-white">{user.username}</h2>
                            <p className="text-gray-400">Standard Member</p>
                        </div>
                        <div className="flex gap-2">
                             <span className="px-3 py-1 rounded-full bg-green-500/10 text-green-400 text-sm border border-green-500/20">Active</span>
                             <span className="px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-sm border border-blue-500/20">ID: {user.userId}</span>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-8">
                        {/* Email Card */}
                        <div className="p-4 bg-[#121212] rounded-xl border border-[#272727] flex items-center gap-4 hover:border-red-900/50 transition-colors">
                            <div className="p-3 bg-blue-900/20 rounded-lg text-blue-400">
                                <Mail size={24} />
                            </div>
                            <div>
                                <p className="text-xs text-gray-500 uppercase font-bold">Email</p>
                                <p className="text-white break-all">{user.email}</p>
                            </div>
                        </div>

                        {/* Date Card */}
                        <div className="p-4 bg-[#121212] rounded-xl border border-[#272727] flex items-center gap-4 hover:border-red-900/50 transition-colors">
                            <div className="p-3 bg-green-900/20 rounded-lg text-green-400">
                                <Calendar size={24} />
                            </div>
                            <div>
                                <p className="text-xs text-gray-500 uppercase font-bold">Joined</p>
                                <p className="text-white">
                                    {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                                </p>
                            </div>
                        </div>

                        {/* Security Card */}
                        <div className="p-4 bg-[#121212] rounded-xl border border-[#272727] flex items-center gap-4 md:col-span-2 hover:border-red-900/50 transition-colors">
                            <div className="p-3 bg-purple-900/20 rounded-lg text-purple-400">
                                <ShieldCheck size={24} />
                            </div>
                            <div>
                                <p className="text-xs text-gray-500 uppercase font-bold">Security</p>
                                <p className="text-gray-300 text-sm">Password hash is secure. 2FA is currently disabled.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Profile;