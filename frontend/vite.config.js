import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    chunkSizeWarningLimit: 3000
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Rewrite /api to /api/v1 to match backend context-path
        rewrite: (path) => path.replace(/^\/api/, '/api/v1')
      }
    }
  }
})
