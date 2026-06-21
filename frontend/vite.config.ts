import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5183,
    proxy: {
      '/api': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8093',
        ws: true,
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
})
