# OneAPI
[中文](readme-cn.md) | [English](readme.md)

## 概述
OneAPI是一个openai代理应用，旨在提供统一的openai协议下的llm代理服务, 并基于统一的llm模型名提供不同三方代理的多个账号的负载均衡及异常熔断的调用能力，在此基础上提供了相关较为友好的配置界面。

## 主要依赖版本
| 依赖          | 版本     |
|-------------|--------|
| JDK         | 21     |
| Spring Boot | 3.2.1  |
| Vue         | 3.5.13 |

## 功能
- 提供了基于openai协议下的llm代理服务及embedding代理服务
  - 支持在配置中增加标准模型名到三方代理特有模型名的映射关系
  - 支持在配置中为某个三方代理配置多个账号, 并在账号间进行负载均衡
  - 支持在账号调用持续异常下的自动熔断能力, 并在一定事件后恢复对账号的访问
  - 支持对部分三方代理的余额自动更新能力(大部分三方代理没有提供余额查询接口, 需要手动维护余额字段), 并支持钉钉群通知
  - 可通过`提供者:模型名`的方式指定调用的三方代理及模型, 例如`openrouter:gpt-4o-mini`
- 提供了基于aliyun服务的ocr代理服务. 目前仅支持阿里云的ocr服务, 由于ocr服务没有一个广泛认同的协议, 目前ocr接口协议是私有定义, 可根据需求自行修改
- 应用支持h2和mysql, 请自行在application.properties中修改数据源配置并打包
  - 在首次启动时会自动初始化数据库. 初始化过程包括创建必要的表和插入初始数据.
  - 初始化数据中的账号数据为纯测试数据无法调用, 请自行注册第三方账号并修改账号配置.

## 接口使用方式
完全兼容openai的llm代理接口, 请参考openai的llm[接口文档](https://platform.openai.com/docs/introduction).
- 本应用的接口地址为`http://localhost:7001/v1`
- 需要在oneapi_config表中配置apikey用于接口鉴权, 默认的apikey为`sk-oneapi-oneapi`. 目前该配置无配置界面, 请自行在数据库中修改, 支持配置多个apikey
- 测试代码可参见 [TestModelApi.java](oneapi-start/src/test/java/com/supersoft/oneapi/api/TestModelApi.java)

## 基础配置
- 应用部分配置在数据表oneapi_config中, 请自行修改

| 配置键                  | 样例值                                                  | 配置说明    |
|----------------------|------------------------------------------------------|---------|
| oneapi.model.default | claude-3-haiku                                       | 默认模型名称  |
| oneapi.apiKeys       | ["sk-oneapi-oneapi"]                                 | apikey  |
| log.enable           | true                                                 | 详细日志开关  |
| oneapi.alert.ding    | https://oapi.dingtalk.com/robot/send?access_token=xx | 钉钉告警机器人 |
| oneapi.success.rt    | 60000                                                | 接口超时时间  |

## 配置页面说明
### 三方代理配置列表
- 点击`是否启用`按钮可以启用或禁用三方代理
![三方代理.png](doc/img.png)

### 三方代理详情修改
- 在三方代理配置列表点击`编辑`按钮可进入详情修改页
- 页面中可以修改代理的名称、代理网址、模型映射、三方API代理地址
![代理修改.png](doc/img1.png)


### 三方代理账号列表
- 在三方代理配置列表点击`账号`按钮可进入账号列表页
- 页面中点击`编辑`按钮可以直接在行内进行账号的修改
- 页面中点击`新增账号`按钮可以新增账号
- 点击`是否启用`按钮可以启用或禁用账号
![账号列表.png](doc/img2.png)

## 二次开发
### 如何编译镜像
1. 编译前端代码
  ```bash
  cd oneapi-ui
  pnpm install
  pnpm run build
  ```
2. 编译后端代码
- 代码编译后会将最终生成的fatjar拷贝到docker目录下用于构建镜像
  ```bash
  mvn clean package -Pdev
  ```
3. 构建镜像
  ```bash
  cd APP-META/docker-config
  docker build -t account/oneapi:tag .
  ```

## 部署方式

### Docker 部署
1. 拉取 Docker 镜像：
  ```bash
  docker pull supermicroai/oneapi:20241223
  ```

2. 运行 Docker 容器：
  ```bash
  docker run -d -p 7001:7001 --name oneapi supermicroai/oneapi:20241223
  ```

### Kubernetes 部署
1. 修改部署文件[app.yaml](APP-META/app.yaml)中的`image`字段编译完成的镜像地址 
2. 部署到 Kubernetes 集群：
  ```bash
  kubectl apply -f app.yaml
  ```
