# --- 阶段 1: 构建阶段 ---
FROM maven:3.9.6-eclipse-temurin-21 AS build-stage
WORKDIR /app

# 复制所有源码和 pom
COPY . .

# 编译打包特定模块，并利用 cache mount 缓存 Maven 依赖
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl easymusic-web -am -DskipTests

# --- 阶段 2: 运行阶段 ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 从构建阶段复制打包好的 jar
COPY --from=build-stage /app/easymusic-web/target/easymusic-web-1.0.0.jar app.jar

# 暴露 8090 (Spring Boot Web 端口) 和 8099 (Netty WebSocket 端口)
EXPOSE 8090 8099

# JVM 优化参数配置启动
ENTRYPOINT ["java", "-server", "-Xms2g", "-Xmx2g", "-XX:MaxDirectMemorySize=2g", "-XX:+UseG1GC", "-jar", "app.jar"]

