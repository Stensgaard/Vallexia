# Multi-stage Dockerfile for production nginx with frontend
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY src/main/java/com/vallexia/web/package*.json ./

# Install all dependencies (including dev dependencies for build)
RUN npm install

# Copy source code
COPY src/main/java/com/vallexia/web .

# Build the application
RUN npm run build

# Production nginx stage
FROM nginx:alpine

# Copy built frontend assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY deployment/nginx/nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
