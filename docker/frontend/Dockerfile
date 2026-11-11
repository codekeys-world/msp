# ---------- Étape 1 : build Angular ----------
FROM node:20-alpine AS build
WORKDIR /build
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# ---------- Étape 2 : exécution avec nginx ----------
FROM nginx:1.27-alpine

ARG APP_VERSION=1.0.0

COPY --from=build /build/dist/msp_frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080
ENV APP_VERSION=${APP_VERSION}

CMD ["nginx", "-g", "daemon off;"]



