import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/auth-api": {
        target: process.env.VITE_API_PROXY_TARGET ?? "http://localhost:9000",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/auth-api/, ""),
      },
      "/gateway-api": {
        target: process.env.VITE_GATEWAY_PROXY_TARGET ?? "http://localhost:8080",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/gateway-api/, ""),
      },
    },
  },
});
