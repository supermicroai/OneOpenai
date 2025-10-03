-- 删除表（如果存在）
drop table if exists oneapi_account;
drop table if exists oneapi_config;
drop table if exists oneapi_model;
drop table if exists oneapi_provider;
drop table if exists oneapi_token;
drop table if exists oneapi_token_usage;

-- 创建账户表
create table oneapi_account
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP null,
    gmt_modified timestamp default CURRENT_TIMESTAMP null,
    provider_code varchar(32)                        null comment '关联的提供商代码',
    name         varchar(32)                         null comment '账号类型',
    note         varchar(64)                         null,
    api_key      varchar(128)                        null,
    ak           varchar(32)                         null,
    sk           varchar(32)                         null,
    cost         double                              null comment '已花费金额',
    balance      double                              null comment '账户余额',
    status       int                                 null comment '是否启用 1启用 0关闭 -1欠费'
);

-- 插入账户数据
INSERT INTO oneapi_account (provider_code, name, note, api_key, ak, sk, cost, balance, status) VALUES
    ('openrouter_llm', 'openrouter', 'admin@gmail.com', 'sk-or-v1-this-is-a-demo-key', null, null, 0, 50, 1);

-- 创建配置表
create table oneapi_config
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    config_group varchar(16),
    config_key   varchar(32),
    config_value clob,
    version      int default 0 not null,
    note         clob,
    status       int default 1 not null,
    env          varchar(8) default 'global',
    constraint uk_key unique (config_key)
);

-- 插入配置数据
INSERT INTO oneapi_config (config_group, config_key, config_value, version, note, status, env) VALUES
    ('大模型', 'oneapi.model.default', 'claude-3-haiku', 0, '默认模型名称', 1, 'global'),
    ('大模型', 'oneapi.apiKeys', '["sk-oneapi-oneapi"]', 0, '接受的apikey', 1, 'global'),
    ('应用', 'log.enable', 'true', 0, '详细日志开关', 1, 'global'),
    ('告警', 'oneapi.alert.ding', 'https://oapi.dingtalk.com/robot/send?access_token=xx', 0, '钉钉告警机器人', 1, 'global'),
    ('大模型', 'oneapi.success.rt', '60000', 0, '模型接口超时时间', 1, 'global');

-- 创建模型表
create table oneapi_model
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    name         varchar(32),
    vendor       varchar(32),
    type         varchar(16),
    input_price  decimal(10,6) default 0 comment '输入token价格（每1M个token）',
    output_price decimal(10,6) default 0 comment '输出token价格（每1M个token）',
    description  varchar(512) comment '模型描述',
    enable       tinyint default 1
);

-- 插入模型数据
INSERT INTO oneapi_model (name, vendor, type, input_price, output_price, enable) VALUES
    ('bge-large-zh-v1.5', 'Baidu', 'embedding', 0.1, 0.1, 1),
    ('bge-m3', 'Baidu', 'embedding', 0.1, 0.1, 1),
    ('claude-3-haiku', 'Claude', 'llm', 0.25, 1.25, 1),
    ('claude-3.5-haiku', 'Claude', 'llm', 1.0, 5.0, 1),
    ('claude-3.5-sonnet', 'Claude', 'llm', 3.0, 15.0, 1),
    ('deepseek-chat', 'DeepSeek', 'llm', 0.14, 0.28, 1),
    ('deepseek-chat-v2', 'DeepSeek', 'llm', 0.14, 0.28, 1),
    ('gpt-3.5-turbo', 'OpenAI', 'llm', 1.5, 2.0, 1),
    ('gpt-4o', 'OpenAI', 'llm', 2.5, 10.0, 1),
    ('gpt-4o-mini', 'OpenAI', 'llm', 0.15, 0.6, 1),
    ('llama-3.1-405b', 'Meta', 'llm', 5.0, 15.0, 1),
    ('llama-3.1-70b', 'Meta', 'llm', 0.9, 0.9, 1),
    ('llama-3.1-8b', 'Meta', 'llm', 0.2, 0.2, 1),
    ('o1', 'OpenAI', 'llm', 15.0, 60.0, 1),
    ('o1-mini', 'OpenAI', 'llm', 3.0, 12.0, 1),
    ('o1-preview', 'OpenAI', 'llm', 15.0, 60.0, 1),
    ('ocr-ali-v1', 'Qwen', 'ocr', 1.0, 1.0, 1),
    ('qwen-math-plus', 'Qwen', 'llm', 1.0, 2.0, 1),
    ('qwen-max', 'Qwen', 'llm', 2.0, 6.0, 1),
    ('qwen-vl-max', 'Qwen', 'llm', 2.0, 6.0, 1),
    ('qwen-vl-plus', 'Qwen', 'llm', 1.0, 2.0, 1),
    ('qwen1.5-110b', 'Qwen', 'llm', 1.8, 1.8, 1),
    ('qwen1.5-72b', 'Qwen', 'llm', 0.9, 0.9, 1),
    ('qwen2-57b', 'Qwen', 'llm', 1.4, 1.4, 1),
    ('qwen2-72b', 'Qwen', 'llm', 0.9, 0.9, 1),
    ('qwen2.5-72b', 'Qwen', 'llm', 0.9, 0.9, 1),
    ('qwen2.5-math-72b', 'Qwen', 'llm', 1.8, 1.8, 1),
    ('text-embedding-3-large', 'OpenAI', 'embedding', 0.13, 0.13, 1),
    ('text-embedding-3-small', 'OpenAI', 'embedding', 0.02, 0.02, 1),
    ('text-embedding-ada-002', 'OpenAI', 'embedding', 0.1, 0.1, 1),
    ('text-embedding-v3', 'Qwen', 'embedding', 0.1, 0.1, 1);

-- 创建服务提供商表
create table oneapi_provider
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    code         varchar(32) not null unique comment '提供者代码',
    name         varchar(32),
    type         varchar(16),
    url          varchar(256),
    api          varchar(256),
    models       clob,
    service      varchar(64),
    enable       int
);

-- 插入服务提供商数据
INSERT INTO oneapi_provider (code, name, type, url, api, models, service, enable) VALUES
    ('openrouter_llm', 'openrouter', 'llm', 'https://openrouter.ai', 'https://openrouter.ai/api/v1', '{"o1":"openai/o1-preview-2024-09-12","claude-3-haiku":"anthropic/claude-3-haiku","claude-3.5-sonnet":"anthropic/claude-3.5-sonnet","llama-3.1-8b":"meta-llama/llama-3.1-8b-instruct","gpt-4o-mini":"openai/gpt-4o-mini","o1-preview":"openai/o1-preview-2024-09-12","llama-3.1-405b":"meta-llama/llama-3.1-405b-instruct","gpt-3.5-turbo":"openai/gpt-3.5-turbo","llama-3.1-70b":"meta-llama/llama-3.1-70b-instruct","claude-3.5-haiku":"anthropic/claude-3-5-haiku","llama-3-70b":"meta-llama/llama-3-70b","o1-mini":"openai/o1-mini","gpt-4o":"openai/gpt-4o"}', 'openRouterService', 1),
    ('aliyun_llm', 'aliyun', 'llm', 'https://bailian.console.aliyun.com', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '{"qwen2.5-72b": "qwen2.5-72b-instruct","qwen2.5-math-72b": "qwen2.5-math-72b-instruct","qwen2-72b": "qwen2-72b-instruct","qwen2-57b": "qwen2-57b-a14b-instruct","qwen1.5-110b": "qwen1.5-110b-chat","qwen1.5-72b": "qwen1.5-72b-chat","qwen-vl-plus": "qwen-vl-plus","qwen-vl-max": "qwen-vl-max","qwen-math-plus": "qwen-math-plus"}', 'aliyunService', 1),
    ('siliconflow_llm', 'siliconflow', 'llm', 'https://cloud.siliconflow.cn', 'https://api.siliconflow.cn/v1', '{"deepseek-chat-v2": "deepseek-ai/DeepSeek-V2-Chat"}', 'siliconService', 1),
    ('aliyun_embedding', 'aliyun', 'embedding', 'https://bailian.console.aliyun.com', 'https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding', '{"text-embedding-v3": "text-embedding-v3"}', 'aliyunService', 1),
    ('siliconflow_embedding', 'siliconflow', 'embedding', 'https://cloud.siliconflow.cn', 'https://api.siliconflow.cn/v1/embeddings', '{"bge-large-zh-v1.5": "BAAI/bge-large-zh-v1.5","bge-m3": "BAAI/bge-m3"}', 'siliconService', 1),
    ('aliyun_ocr', 'aliyun', 'ocr', 'https://bailian.console.aliyun.com', '', '{"ocr-ali-v1": "ocr-ali-v1"}', 'aliyunService', 1);

-- 令牌管理表
create table oneapi_token
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP,
    name         varchar(64) not null comment '令牌名称',
    api_key      varchar(128) not null unique comment 'API密钥',
    description  varchar(256) comment '令牌描述',
    expire_time  timestamp null comment '过期时间，null表示永不过期',
    max_usage    bigint default -1 comment '最大token数限制，-1表示不限制',
    token_usage  bigint default 0 comment '当前token使用量',
    max_cost_limit decimal(10,6) default -1 comment '最大费用限制，-1表示不限制',
    current_cost_usage decimal(10,6) default 0 comment '当前费用使用量',
    status       int default 1 comment '状态：1启用，0禁用',
    creator      varchar(64) comment '创建者',
    last_used_time timestamp null comment '最后使用时间'
);

-- 令牌使用记录表
create table oneapi_token_usage
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP,
    token_id     int comment '令牌ID',
    provider     varchar(64) comment '服务提供商',
    model        varchar(64) comment '使用的模型',
    request_tokens int default 0 comment '请求令牌数',
    response_tokens int default 0 comment '响应令牌数',
    cost         decimal(10,10) default 0 comment '成本',
    status       int comment '调用状态：1成功，0失败',
    error_msg    varchar(512) comment '错误信息',
    ip_address   varchar(45) comment '客户端IP地址'
);

-- 插入默认的令牌示例
INSERT INTO oneapi_token (name, api_key, description, creator, status) VALUES
    ('默认令牌', 'sk-oneapi-default-token-2024', '系统默认令牌，用于测试', 'system', 1);

-- 创建索引
create index idx_token_usage_gmt_create on oneapi_token_usage(gmt_create);
