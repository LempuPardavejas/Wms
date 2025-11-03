// Configuration module to handle environment variables
// This provides better testability by centralizing env var access

export const config = {
  apiUrl: typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_URL
    ? import.meta.env.VITE_API_URL
    : '/api'
};
