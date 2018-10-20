FROM nginx:1.14-alpine

RUN rm /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf
COPY content /usr/share/nginx/html
