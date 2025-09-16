import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { 
    host: true, 
    port: 5173 
  },
  build: {
    // Generate source maps for production builds
    sourcemap: true,
    // Keep source maps separate for security
    minify: 'terser'
  },
  css: {
    // Enable CSS source maps
    devSourcemap: true
  },
  // Enable source maps in development
  define: {
    __DEV__: JSON.stringify(process.env.NODE_ENV === 'development')
  }
});


