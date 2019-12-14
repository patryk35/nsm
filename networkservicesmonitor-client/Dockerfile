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
COPY --from=Builder /app/build /usr/share/nginx/html

ENV REACT_APP_API_URL http://localhost:5000/api/v2

# Start Nginx server
CMD ["/bin/bash", "-c", "/usr/share/nginx/html/env.sh && nginx -g \"daemon off;\""]
