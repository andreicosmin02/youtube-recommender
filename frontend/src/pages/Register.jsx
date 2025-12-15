import React, { useState } from 'react';
import { endpoints } from '../api/axios';
import { UserPlus, Wand2 } from 'lucide-react';

const Register = () => {
  const [form, setForm] = useState({ username: '', email: '', interests: '' });
  const [success, setSuccess] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
        const payload = {
            ...form,
            interests: form.interests.split(',').map(i => i.trim()) // Convert comma string to list
        };
        const res = await endpoints.register(payload);
        setSuccess(`User '${res.data.username}' created! ID: ${res.data.userId}. Initial preference vector generated.`);
    } catch (err) {
        alert(`Registration failed. ${err.message}`);
    }
  };

  return (
    <div className="p-10 flex justify-center items-center min-h-[80vh]">
        <div className="w-full max-w-lg bg-[#1f1f1f] border border-[#3f3f3f] rounded-2xl p-8 shadow-2xl">
            <div className="text-center mb-8">
                <div className="bg-[#272727] w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                    <UserPlus className="text-red-500" size={32} />
                </div>
                <h1 className="text-2xl font-bold text-white">Create Profile</h1>
                <p className="text-gray-400">Tell us what you like, and AI will adapt.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <input 
                    placeholder="Username" 
                    className="w-full bg-[#0f0f0f] border border-gray-600 rounded-lg p-3 text-white"
                    onChange={e => setForm({...form, username: e.target.value})}
                    required
                />
                <input 
                    placeholder="Email" 
                    type="email"
                    className="w-full bg-[#0f0f0f] border border-gray-600 rounded-lg p-3 text-white"
                    onChange={e => setForm({...form, email: e.target.value})}
                    required
                />
                <div>
                    <label className="text-xs text-gray-400 ml-1">Interests (comma separated)</label>
                    <textarea 
                        placeholder="e.g. Java, Hiking, Cooking, Space" 
                        className="w-full bg-[#0f0f0f] border border-gray-600 rounded-lg p-3 text-white h-24 resize-none"
                        onChange={e => setForm({...form, interests: e.target.value})}
                        required
                    />
                </div>
                <button type="submit" className="w-full bg-white text-black font-bold py-3 rounded-lg hover:bg-gray-200 transition-colors flex justify-center items-center gap-2">
                    <Wand2 size={18} /> Generate Profile Embedding
                </button>
            </form>

            {success && (
                <div className="mt-6 bg-green-900/20 text-green-400 p-4 rounded-lg text-sm border border-green-900">
                    {success}
                </div>
            )}
        </div>
    </div>
  );
};

export default Register;