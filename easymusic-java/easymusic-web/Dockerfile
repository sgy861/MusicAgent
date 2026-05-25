# 使用轻量级的 JRE 21 作为运行环境
FROM eclipse-temurin:21-jre-jammy

# 设置工作目录
WORKDIR /app

# 复制本地打包好的 jar 文件到镜像中
COPY target/easymusic-web-1.0.0.jar app.jar

# 暴露 8090 (Spring Boot Web 端口) 和 8099 (Netty WebSocket 端口)
EXPOSE 8090 8099

# JVM 优化参数配置启动
ENTRYPOINT ["java", "-server", "-Xms2g", "-Xmx2g", "-XX:MaxDirectMemorySize=2g", "-XX:+UseG1GC", "-jar", "app.jar"]
