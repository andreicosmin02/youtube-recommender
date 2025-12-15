import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export const CURRENT_USER_ID = 1; // Hardcoded default user

export const endpoints = {
  
  ingest: (topic, max) =>
    api.post(`/ingestion/trigger?topic=${topic}&max=${max}`),
  recommend: (userId, query) =>
    api.get(`/recommendations/ask?userId=${userId}&query=${query}`),
  interact: (data) => api.post("/interactions", data),
  deleteInteraction: (userId, videoId) =>
    api.delete(`/interactions?userId=${userId}&videoId=${videoId}`),
  history: (userId) =>
    api.get(`/interactions/history?userId=${userId}&limit=50`),
  watchLater: (userId) => api.get(`/interactions/watch-later?userId=${userId}`),

  // NEW: Get User Profile
  getUser: (userId) => api.get(`/users/${userId}`),
};

export default api;
