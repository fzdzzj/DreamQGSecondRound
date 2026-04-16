import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        [env.VITE_BASE_API]: {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true,
          rewrite: (p) => p.replace(new RegExp(`^${env.VITE_BASE_API}`), '')
        },
        '/ws': {
          target: env.VITE_PROXY_TARGET.replace(/^http/, 'ws'),
          ws: true,
          changeOrigin: true
        }
      }
    }
  }
})
