# --- 阶段 1: 构建阶段 ---
FROM node:20-alpine AS build-stage

WORKDIR /app

# 复制依赖定义
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制全部源码
COPY . .

# 执行生产环境打包编译
RUN npm run build

# --- 阶段 2: 运行阶段 ---
FROM nginx:stable-alpine

# 复制构建好的静态资源
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 覆盖默认 nginx 配置，加入反向代理规则
COPY default.conf /etc/nginx/conf.d/default.conf

# 暴露容器 80 端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
