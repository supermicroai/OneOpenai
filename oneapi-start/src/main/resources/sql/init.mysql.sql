-- 删除表（如果存在）
DROP TABLE IF EXISTS oneapi_account;
DROP TABLE IF EXISTS oneapi_config;
DROP TABLE IF EXISTS oneapi_model;
DROP TABLE IF EXISTS oneapi_provider;
DROP TABLE IF EXISTS oneapi_token;
DROP TABLE IF EXISTS oneapi_token_usage;

-- 创建账户表
create table oneapi_account
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP null,
    gmt_modified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    provider_code varchar(32)                        null comment '关联的提供商代码',
    name         varchar(32)                         null comment '账号类型',
    note         varchar(64)                         null,
    api_key      varchar(128)                        null,
    ak           varchar(32)                         null,
    sk           varchar(32)                         null,
    cost         double                              null comment '已花费金额',
    balance      double                              null comment '账户余额',
    status       int                                 null comment '是否启用 1启用 0关闭 -1欠费',
    FOREIGN KEY (provider_code) REFERENCES oneapi_provider(code)
);

-- 插入账户数据
INSERT INTO oneapi_account (provider_code, name, note, api_key, ak, sk, cost, balance, status) VALUES
    ('openrouter_llm', 'openrouter', 'admin@gmail.com', 'sk-or-v1-this-is-a-demo-key', null, null, 0, 50, 1);

-- 创建配置表
create table oneapi_config
(
    id           int auto_increment primary key,
    gmt_create   timestamp  default CURRENT_TIMESTAMP null,
    gmt_modified timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    config_group varchar(16)                          null,
    config_key   varchar(32)                          null,
    config_value mediumtext                           null,
    version      int        default 0                 not null,
    note         text                                 null,
    status       int        default 1                 not null,
    env          varchar(8) default 'global'          null comment '环境 dev prd global',
    constraint uk_key
        unique (config_key)
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
    gmt_create   timestamp default CURRENT_TIMESTAMP null,
    gmt_modified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    name         varchar(32)                         null comment '模型名称',
    vendor       varchar(32)                         null comment '厂商名称',
    type         varchar(16)                         null,
    enable       tinyint   default 1                 null
);

-- 插入模型数据
INSERT INTO oneapi_model (name, vendor, type, enable) VALUES
    ('bge-large-zh-v1.5', 'Baidu', 'embedding', 1),
    ('bge-m3', 'Baidu', 'embedding', 1),
    ('claude-3-haiku', 'Claude', 'llm', 1),
    ('claude-3.5-haiku', 'Claude', 'llm', 1),
    ('claude-3.5-sonnet', 'Claude', 'llm', 1),
    ('deepseek-chat', 'DeepSeek', 'llm', 1),
    ('deepseek-chat-v2', 'DeepSeek', 'llm', 1),
    ('gpt-3.5-turbo', 'OpenAI', 'llm', 1),
    ('gpt-4o', 'OpenAI', 'llm', 1),
    ('gpt-4o-mini', 'OpenAI', 'llm', 1),
    ('llama-3-70b', 'Meta', 'llm', 1),
    ('llama-3.1-405b', 'Meta', 'llm', 1),
    ('llama-3.1-70b', 'Meta', 'llm', 1),
    ('llama-3.1-8b', 'Meta', 'llm', 1),
    ('o1', 'OpenAI', 'llm', 1),
    ('o1-mini', 'OpenAI', 'llm', 1),
    ('o1-preview', 'OpenAI', 'llm', 1),
    ('ocr-ali-v1', 'Qwen', 'ocr', 1),
    ('qwen-math-plus', 'Qwen', 'llm', 1),
    ('qwen-max', 'Qwen', 'llm', 1),
    ('qwen-vl-max', 'Qwen', 'llm', 1),
    ('qwen-vl-plus', 'Qwen', 'llm', 1),
    ('qwen1.5-110b', 'Qwen', 'llm', 1),
    ('qwen1.5-72b', 'Qwen', 'llm', 1),
    ('qwen2-57b', 'Qwen', 'llm', 1),
    ('qwen2-72b', 'Qwen', 'llm', 1),
    ('qwen2.5-72b', 'Qwen', 'llm', 1),
    ('qwen2.5-math-72b', 'Qwen', 'llm', 1),
    ('text-embedding-3-large', 'OpenAI', 'embedding', 1),
    ('text-embedding-3-small', 'OpenAI', 'embedding', 1),
    ('text-embedding-ada-002', 'OpenAI', 'embedding', 1),
    ('text-embedding-v3', 'Qwen', 'embedding', 1);

-- 创建服务提供商表
create table oneapi_provider
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP null,
    gmt_modified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    code         varchar(32)                         not null unique comment '提供者代码',
    name         varchar(32)                         null comment '提供者名称',
    type         varchar(16)                         null comment '服务类型 llm embedding ocr',
    url          varchar(256)                        null comment '提供者网址',
    api          varchar(256)                        null comment '基础api地址',
    models       mediumtext                          null comment '标准模型名称映射到提供者模型名称',
    service      varchar(64)                         null comment '提供者对应的各类服务bean',
    enable       int                                 null
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
    gmt_create   timestamp default CURRENT_TIMESTAMP null,
    gmt_modified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    name         varchar(64)                         not null comment '令牌名称',
    api_key      varchar(128)                        not null unique comment 'API密钥',
    description  varchar(256)                        null comment '令牌描述',
    expire_time  timestamp                           null comment '过期时间，null表示永不过期',
    max_usage    bigint      default -1              null comment '最大token数限制，-1表示不限制',
    token_usage  bigint      default 0               null comment '当前token使用量',
    status       int         default 1               null comment '状态：1启用，0禁用',
    creator      varchar(64)                         null comment '创建者',
    last_used_time timestamp                         null comment '最后使用时间'
);

-- 令牌使用记录表
create table oneapi_token_usage
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP null,
    gmt_modified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    provider     varchar(64)                         null comment '服务提供商',
    model        varchar(64)                         null comment '使用的模型',
    request_tokens int       default 0               null comment '请求令牌数',
    response_tokens int      default 0               null comment '响应令牌数',
    cost         decimal(10,4) default 0             null comment '成本',
    status       int                                 null comment '调用状态：1成功，0失败',
    error_msg    varchar(512)                        null comment '错误信息',
    ip_address   varchar(45)                         null comment '客户端IP地址'
);

-- 插入默认的令牌示例
INSERT INTO oneapi_token (name, api_key, description, creator, status) VALUES
    ('默认令牌', 'sk-oneapi-default-token-2024', '系统默认令牌，用于测试', 'system', 1);

-- 创建索引
CREATE INDEX idx_token_usage_gmt_create ON oneapi_token_usage(gmt_create);
