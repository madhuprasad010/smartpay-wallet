FROM nginx:alpine

LABEL maintainer="SmartPay Team"

# Remove default config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost/nginx-health || exit 1

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
