FROM supermicroai/java-base-jdk21

ARG APP_NAME
ENV APP_NAME=oneapi
ARG ENV
ENV ENV=${ENV}

ARG CONFIG_PATH=environment/common/bin/

COPY ${CONFIG_PATH} /home/admin/${APP_NAME}/bin

RUN groupadd admin &&\
    useradd -r -g admin admin && \
    mkdir /home/admin/${APP_NAME}/logs && \
    mkdir /home/admin/logs

ENV LANG=zh_CN.utf8 LC_ALL=zh_CN.utf8

RUN mkdir -p /home/admin/${APP_NAME}/target && \
    # 读取properties配置并填充到环境变量
    echo -e "#!/bin/bash \n/home/admin/${APP_NAME}/bin/env.sh \n" >> /home/admin/start.sh && \
    # 启动应用
    echo -e "/home/admin/${APP_NAME}/bin/appctl.sh start" >> /home/admin/start.sh &&\
    chmod +x /home/admin/*.sh

# 设置文件操作权限
RUN chmod -R a+x /home/admin/${APP_NAME}/bin/

VOLUME /home/admin/${APP_NAME}/logs \
       /home/admin/logs

# 启动容器时进入的工作目录
WORKDIR /home/admin/${APP_NAME}/bin
COPY *.jar /home/admin/${APP_NAME}/target/${APP_NAME}.jar

RUN chown -R admin.admin /home/admin

EXPOSE 7001

ENTRYPOINT ["/home/admin/start.sh"]
