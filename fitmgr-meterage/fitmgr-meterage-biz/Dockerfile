FROM fitmgr/adoptopenjdk-openjdk8:v1
MAINTAINER fitmgr@fiberhome.com
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN mkdir -p /app
WORKDIR /app
EXPOSE 8888
ADD ./*.jar /app/app.jar
ENTRYPOINT ["java", "-Xmx768m", "-Xms768m", "-Xmn256m", "-XX:MetaspaceSize=256m", "-XX:MaxMetaspaceSize=256m", "-XX:+UseParNewGC", "-XX:+UseConcMarkSweepGC", "-XX:+UseCMSCompactAtFullCollection", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/app/logs", "-jar", "/app/app.jar"]
