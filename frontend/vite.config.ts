import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Inside Docker the backend service is reachable at bt-backend:8085;
// locally it runs on localhost:8085. VITE_API_BASE_URL controls this.
const apiTarget = process.env.VITE_API_BASE_URL || 'http://localhost:8085'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3010,
    host: true,
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
      },
    },
  },
})
