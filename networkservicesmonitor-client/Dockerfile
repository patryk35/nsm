FROM node:12.2.0-alpine AS Builder
WORKDIR /app
ENV PATH /app/node_modules/.bin:$PATH
COPY package.json /app/package.json
RUN npm install --silent
RUN npm install react-scripts@3.0.1 -g --silent
COPY . /app
RUN npm run-script build

FROM nginx:stable-alpine
# Copy .env file and shell script to container
WORKDIR /usr/share/nginx/html
COPY ./env.sh .
COPY .env .
# Add bash
RUN apk add --no-cache bash
# Make our shell script executable
RUN chmod +x env.sh

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY certificates/self-signed.conf /etc/nginx/snippets/self-signed.conf
COPY certificates/ssl-params.conf /etc/nginx/snippets/ssl-params.conf
COPY certificates/nginx-selfsigned.crt /etc/ssl/certs/nginx-selfsigned.crt
COPY certificates/nginx-selfsigned.key /etc/ssl/private/nginx-selfsigned.key
COPY certificates/dhparam.pem /etc/ssl/certs/dhparam.pem
COPY --from=Builder /app/build /usr/share/nginx/html

ENV REACT_APP_API_URL https://localhost:8443/api/v1

# Start Nginx server
CMD ["/bin/bash", "-c", "/usr/share/nginx/html/env.sh && nginx -g \"daemon off;\""]
