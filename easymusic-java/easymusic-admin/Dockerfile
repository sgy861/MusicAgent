# 使用轻量级的 JRE 21 作为运行环境
FROM eclipse-temurin:21-jre-jammy

# 设置工作目录
WORKDIR /app

# 复制本地打包好的 jar 文件到镜像中
COPY target/easymusic-admin-1.0.0.jar app.jar

# 暴露 8091 (Spring Boot Admin Web 端口)
EXPOSE 8091

# 启动配置
ENTRYPOINT ["java", "-server", "-Xms1g", "-Xmx1g", "-XX:+UseG1GC", "-jar", "app.jar"]
