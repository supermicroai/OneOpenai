#!/bin/bash

source /etc/environment

PROG_NAME=$0
ACTION=$1

APP_HOME=$(cd $(dirname "${BASH_SOURCE[0]}")/..; pwd)

DEBUG8_JVM="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
DEBUG17_JVM="-Djava.awt.headless=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"
OPENS_JVM="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.internal.misc=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.sql=ALL-UNNAMED"

# 获取系统的总内存
total_memory=$(echo $APP_MEM | sed 's/Mi//g')
if [ -z "$total_memory" ]; then
    total_memory=512
    echo "INFO: total memory not found, defaulting to ${total_memory} MB"
else
    echo "INFO: total memory is ${total_memory} MB"
fi

# 给系统预留内存
reserved_memory=$((total_memory / 10))
if [ $reserved_memory -lt 50 ]; then
    reserved_memory=50
elif [ $reserved_memory -gt 200 ]; then
    reserved_memory=200
fi
echo "INFO: reserved os memory is ${reserved_memory} MB"
# 计算Xms和Xmx参数
remaining_memory=$((total_memory - reserved_memory))
xms=$((remaining_memory / 2))
xmx=$remaining_memory

# 设置JVM的通用参数
AGENT_JVM="-Xms${xms}m -Xmx${xmx}m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/admin/data/dump -Djava.awt.headless=true -Dsun.jnu.encoding=UTF-8 -Dfile.encoding=UTF-8"
echo "INFO: AGENT_JVM: ${AGENT_JVM}"

# 增加对目录的权限修改
chown -R admin.admin /home/admin

jdk_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1-2)
if test "$jdk_version" -lt "9"; then
  DEBUG_JVM="${DEBUG8_JVM}"
else
  DEBUG_JVM="${DEBUG17_JVM}"
fi
echo "INFO: current jdk version is ${jdk_version}, debug options is ${DEBUG_JVM}"

start() {
    prepare
    echo "INFO: ${APP_NAME} try to start..."
    echo "INFO: execute command ${JAVA_HOME}/bin/java ${OPENS_JVM} ${AGENT_JVM} -jar ${DEBUG_JVM} ${APP_HOME}/target/${APP_NAME}.jar"
    su admin -c "${JAVA_HOME}/bin/java ${OPENS_JVM} ${AGENT_JVM} -jar ${DEBUG_JVM} ${APP_HOME}/target/${APP_NAME}.jar"
    echo "INFO: ${APP_NAME} start success"
}

stop() {
    echo "INFO: ${APP_NAME} try to stop..."
    echo "INFO: ${APP_NAME} stop success"
}

main() {
    now=$(date "+%Y-%m-%d %H:%M:%S")
    echo "$now--------------------------"
    echo "INFO: deploy log: ${APP_HOME}/logs/${APP_NAME}_deploy.log"
    case "$ACTION" in
        start)
            start
        ;;
        stop)
            stop
        ;;
        restart)
            stop
            start
        ;;
    esac
}

main | tee -a ${APP_HOME}/logs/${APP_NAME}_deploy.log;
