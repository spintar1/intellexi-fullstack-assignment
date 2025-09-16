import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Extended development configuration with enhanced debugging
export default defineConfig({
  plugins: [react()],
  server: { 
    host: true, 
    port: 5173,
    // Enable HMR debugging
    hmr: {
      overlay: true
    }
  },
  build: {
    // Generate detailed source maps for development builds
    sourcemap: 'inline',
    minify: false,
    rollupOptions: {
      output: {
        // Keep readable names for debugging
        manualChunks: undefined,
      }
    }
  },
  css: {
    // Enable CSS source maps
    devSourcemap: true
  },
  // Development-specific defines
  define: {
    __DEV__: JSON.stringify(true),
    'process.env.NODE_ENV': JSON.stringify('development')
  },
  // Optimize for development
  optimizeDeps: {
    // Include commonly used dependencies
    include: ['react', 'react-dom', 'jwt-decode']
  }
});
