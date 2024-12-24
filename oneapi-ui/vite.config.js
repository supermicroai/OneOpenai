import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const modeIndex = process.argv.indexOf('--mode');
const mode = modeIndex !== -1 ? process.argv[modeIndex + 1] : 'dev';
console.log(mode)

export default defineConfig({
  plugins: [vue()],
  resolve: {
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: mode === 'dev' ? {
      "^/api/": {
        target: "http://localhost:7001/",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    } : {},
  }
})
