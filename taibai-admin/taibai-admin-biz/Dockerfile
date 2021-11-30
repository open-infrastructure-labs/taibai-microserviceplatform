FROM taibai/adoptopenjdk-openjdk8:v1
MAINTAINER taibai@fiberhome.com
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN mkdir -p /app
WORKDIR /app
EXPOSE 4000
ADD ./*.jar /app/app.jar
ENTRYPOINT ["java", "-Xmx1024m", "-Xms1024m", "-Xmn256m", "-XX:MetaspaceSize=256m", "-XX:MaxMetaspaceSize=256m", "-XX:+UseParNewGC", "-XX:+UseConcMarkSweepGC", "-XX:+UseCMSCompactAtFullCollection", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/app/logs", "-jar", "/app/app.jar"]
