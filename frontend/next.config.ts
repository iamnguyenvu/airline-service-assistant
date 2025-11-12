import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactCompiler: true,
  // Removed 'standalone' output for monorepo compatibility
  // Use 'standalone' only in Docker/production builds if needed
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: '*.supabase.co',
      },
    ],
  },
};

export default nextConfig;
