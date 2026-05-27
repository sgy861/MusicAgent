# --- 阶段 1: 构建阶段 ---
FROM maven:3.9.6-eclipse-temurin-21 AS build-stage
WORKDIR /app

# 复制所有源码和 pom
COPY . .

# 编译打包特定模块，并利用 cache mount 缓存 Maven 依赖
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl easymusic-admin -am -DskipTests

# --- 阶段 2: 运行阶段 ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 从构建阶段复制打包好的 jar
COPY --from=build-stage /app/easymusic-admin/target/easymusic-admin-1.0.0.jar app.jar

# 暴露 8091 (Spring Boot Admin Web 端口)
EXPOSE 8091

# 启动配置
ENTRYPOINT ["java", "-server", "-Xms1g", "-Xmx1g", "-XX:+UseG1GC", "-jar", "app.jar"]

