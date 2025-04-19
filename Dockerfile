FROM eclipse-temurin:17-jdk-jammy

# 替换为阿里云镜像源
RUN sed -i 's|http://archive.ubuntu.com|https://mirrors.aliyun.com|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com|https://mirrors.aliyun.com|g' /etc/apt/sources.list

# 安装 Python3、pip 和 FFmpeg，并创建 `python` 软链接
RUN apt-get update && \
    apt-get install -y \
        python3 \
        python3-pip \
        ffmpeg && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/communicate-0.0.1-SNAPSHOT.jar app.jar
COPY scripts/ /app/scripts/

# 安装 Python 依赖（使用清华镜像加速）
RUN pip install -r /app/scripts/requirements.txt -i https://mirrors.aliyun.com/pypi/simple/

# 确保脚本可执行
RUN chmod +x /app/scripts/tts.py

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]