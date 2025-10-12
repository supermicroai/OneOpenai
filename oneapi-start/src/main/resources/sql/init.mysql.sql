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
    gmt_create   timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    gmt_modified timestamp default CURRENT_TIMESTAMP null comment '修改时间',
    provider_code varchar(32)                        null comment '关联的提供商代码',
    name         varchar(32)                         null comment '账号类型',
    note         varchar(64)                         null comment '备注',
    api_key      varchar(128)                        null comment 'api key',
    ak           varchar(32)                         null comment 'access key',
    sk           varchar(32)                         null comment 'secret key',
    cost         double                              null comment '已花费金额',
    balance      double                              null comment '账户余额',
    status       int                                 null comment '是否启用 1启用 0关闭 -1欠费'
) comment '账户表';

-- 插入账户数据
INSERT INTO oneapi_account (provider_code, name, note, api_key, ak, sk, cost, balance, status) VALUES
    ('openrouter_llm', 'openrouter', 'admin@gmail.com', 'sk-or-v1-this-is-a-demo-key', null, null, 0, 50, 1);

-- 创建配置表
create table oneapi_config
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP comment '创建时间',
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '修改时间',
    config_group varchar(16) comment '配置分组',
    config_key   varchar(32) comment '配置键',
    config_value text comment '配置值',
    version      int default 0 not null comment '版本号',
    note         text comment '备注',
    status       int default 1 not null comment '状态 1启用 0禁用',
    env          varchar(8) default 'global' comment '环境',
    constraint uk_key unique (config_key)
) comment '配置表';

-- 插入配置数据
INSERT INTO oneapi_config (config_group, config_key, config_value, version, note, status, env) VALUES
    ('大模型', 'oneapi.model.default', 'claude-3-haiku', 0, '默认模型名称', 1, 'global'),
    ('应用', 'log.enable', 'true', 0, '详细日志开关', 1, 'global'),
    ('告警', 'oneapi.alert.url', 'ding://xx', 0, '告警URL(支持钉钉/Slack等)。钉钉配置格式：ding://{access_token}，Slack配置格式：slack://{token}/{channel}', 1, 'global'),
    ('大模型', 'oneapi.success.rt', '60000', 0, '模型接口超时时间', 1, 'global'),
    ('账户', 'oneapi.credit.min', '5', 0, '账户余额告警阈值（元）', 1, 'global'),
    ('服务', 'oneapi.retry', '3', 0, '服务重试最大次数', 1, 'global');

-- 创建模型表
create table oneapi_model
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP comment '创建时间',
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '修改时间',
    name         varchar(32) comment '模型名称',
    vendor       varchar(32) comment '服务商',
    type         varchar(16) comment '模型类型',
    input_price  decimal(10,6) default 0 comment '输入token价格（每1M个token）',
    output_price decimal(10,6) default 0 comment '输出token价格（每1M个token）',
    description  varchar(512) comment '模型描述',
    enable       tinyint default 1 comment '是否启用'
) comment '模型表';

-- 插入模型数据
INSERT INTO oneapi_model (name, vendor, type, input_price, output_price, enable) VALUES
    ('bge-large-zh-v1.5', 'Baidu', 'embedding', 0.1, 0.1, 1),
    ('bge-m3', 'Baidu', 'embedding', 0.1, 0.1, 1),
    ('claude-3-haiku', 'Claude', 'llm', 0.25, 1.25, 1),
    ('claude-3.5-haiku', 'Claude', 'llm', 1.0, 5.0, 1),
    ('claude-3.5-sonnet', 'Claude', 'llm', 3.0, 15.0, 1),
    ('claude-3.7-sonnet', 'Claude', 'llm', 3.0, 15.0, 1),
    ('claude-4.0-sonnet', 'Claude', 'llm', 5.0, 25.0, 1),
    ('claude-4.5-sonnet', 'Claude', 'llm', 5.0, 25.0, 1),
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
    gmt_create   timestamp default CURRENT_TIMESTAMP comment '创建时间',
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '修改时间',
    code         varchar(32) not null unique comment '提供者代码，唯一标识',
    name         varchar(32) comment '服务商名称',
    type         varchar(16) comment '服务类型',
    url          varchar(256) comment '服务提供商主页',
    api          varchar(256) comment '基础api地址',
    models       text comment '支持的模型',
    service      varchar(64) comment '提供者对应的各类服务bean',
    enable       int comment '是否启用'
) comment '服务提供商表';

-- 插入服务提供商数据（使用新的JSON格式）
INSERT INTO oneapi_provider (code, name, type, url, api, models, service, enable) VALUES
    ('openrouter_llm', 'openrouter', 'llm', 'https://openrouter.ai', 'https://openrouter.ai/api/v1', '{"o1":{"alias":"openai/o1-preview-2024-09-12","inputPrice":15.0,"outputPrice":60.0},"claude-3-haiku":{"alias":"anthropic/claude-3-haiku","inputPrice":0.25,"outputPrice":1.25},"claude-3.5-sonnet":{"alias":"anthropic/claude-3.5-sonnet","inputPrice":3.0,"outputPrice":15.0},"claude-3.7-sonnet":{"alias":"anthropic/claude-3.7-sonnet","inputPrice":3.0,"outputPrice":15.0},"claude-4.0-sonnet":{"alias":"anthropic/claude-4.0-sonnet","inputPrice":5.0,"outputPrice":25.0},"claude-4.5-sonnet":{"alias":"anthropic/claude-4.5-sonnet","inputPrice":5.0,"outputPrice":25.0},"llama-3.1-8b":{"alias":"meta-llama/llama-3.1-8b-instruct","inputPrice":0.2,"outputPrice":0.2},"gpt-4o-mini":{"alias":"openai/gpt-4o-mini","inputPrice":0.15,"outputPrice":0.6},"o1-preview":{"alias":"openai/o1-preview-2024-09-12","inputPrice":15.0,"outputPrice":60.0},"llama-3.1-405b":{"alias":"meta-llama/llama-3.1-405b-instruct","inputPrice":5.0,"outputPrice":15.0},"gpt-3.5-turbo":{"alias":"openai/gpt-3.5-turbo","inputPrice":1.5,"outputPrice":2.0},"llama-3.1-70b":{"alias":"meta-llama/llama-3.1-70b-instruct","inputPrice":0.9,"outputPrice":0.9},"claude-3.5-haiku":{"alias":"anthropic/claude-3-5-haiku","inputPrice":1.0,"outputPrice":5.0},"llama-3-70b":{"alias":"meta-llama/llama-3-70b","inputPrice":0.9,"outputPrice":0.9},"o1-mini":{"alias":"openai/o1-mini","inputPrice":3.0,"outputPrice":12.0},"gpt-4o":{"alias":"openai/gpt-4o","inputPrice":2.5,"outputPrice":10.0}}', 'openRouterService', 1),
    ('aliyun_llm', 'aliyun', 'llm', 'https://bailian.console.aliyun.com', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '{"qwen2.5-72b":{"alias":"qwen2.5-72b-instruct","inputPrice":0.9,"outputPrice":0.9},"qwen2.5-math-72b":{"alias":"qwen2.5-math-72b-instruct","inputPrice":1.8,"outputPrice":1.8},"qwen2-72b":{"alias":"qwen2-72b-instruct","inputPrice":0.9,"outputPrice":0.9},"qwen2-57b":{"alias":"qwen2-57b-a14b-instruct","inputPrice":1.4,"outputPrice":1.4},"qwen1.5-110b":{"alias":"qwen1.5-110b-chat","inputPrice":1.8,"outputPrice":1.8},"qwen1.5-72b":{"alias":"qwen1.5-72b-chat","inputPrice":0.9,"outputPrice":0.9},"qwen-vl-plus":{"alias":"qwen-vl-plus","inputPrice":1.0,"outputPrice":2.0},"qwen-vl-max":{"alias":"qwen-vl-max","inputPrice":2.0,"outputPrice":6.0},"qwen-math-plus":{"alias":"qwen-math-plus","inputPrice":1.0,"outputPrice":2.0}}', 'aliyunService', 1),
    ('siliconflow_llm', 'siliconflow', 'llm', 'https://cloud.siliconflow.cn', 'https://api.siliconflow.cn/v1', '{"deepseek-chat-v2":{"alias":"deepseek-ai/DeepSeek-V2-Chat","inputPrice":0.14,"outputPrice":0.28}}', 'siliconService', 1),
    ('aliyun_embedding', 'aliyun', 'embedding', 'https://bailian.console.aliyun.com', 'https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding', '{"text-embedding-v3":{"alias":"text-embedding-v3","inputPrice":0.1,"outputPrice":0.1}}', 'aliyunService', 1),
    ('siliconflow_embedding', 'siliconflow', 'embedding', 'https://cloud.siliconflow.cn', 'https://api.siliconflow.cn/v1/embeddings', '{"bge-large-zh-v1.5":{"alias":"BAAI/bge-large-zh-v1.5","inputPrice":0.1,"outputPrice":0.1},"bge-m3":{"alias":"BAAI/bge-m3","inputPrice":0.1,"outputPrice":0.1}}', 'siliconService', 1),
    ('aliyun_ocr', 'aliyun', 'ocr', 'https://bailian.console.aliyun.com', '', '{"ocr-ali-v1":{"alias":"ocr-ali-v1","inputPrice":1.0,"outputPrice":1.0}}', 'aliyunService', 1),
    ('openai_official', 'openai', 'llm', 'https://openai.com', 'https://api.openai.com/v1', '{"gpt-3.5-turbo":{"alias":"gpt-3.5-turbo","inputPrice":1.5,"outputPrice":2.0},"gpt-4":{"alias":"gpt-4","inputPrice":3.0,"outputPrice":6.0},"gpt-4-turbo":{"alias":"gpt-4-turbo","inputPrice":3.0,"outputPrice":6.0},"gpt-4o":{"alias":"gpt-4o","inputPrice":2.5,"outputPrice":10.0},"gpt-4o-mini":{"alias":"gpt-4o-mini","inputPrice":0.15,"outputPrice":0.6},"o1":{"alias":"o1","inputPrice":15.0,"outputPrice":60.0},"o1-mini":{"alias":"o1-mini","inputPrice":3.0,"outputPrice":12.0},"o1-preview":{"alias":"o1-preview","inputPrice":15.0,"outputPrice":60.0}}', 'openaiService', 1),
    ('openai_embedding', 'openai', 'embedding', 'https://openai.com', 'https://api.openai.com/v1', '{"text-embedding-3-large":{"alias":"text-embedding-3-large","inputPrice":0.13,"outputPrice":0.13},"text-embedding-3-small":{"alias":"text-embedding-3-small","inputPrice":0.02,"outputPrice":0.02},"text-embedding-ada-002":{"alias":"text-embedding-ada-002","inputPrice":0.1,"outputPrice":0.1}}', 'openaiService', 1),
    ('claude_official', 'claude', 'llm', 'https://anthropic.com', 'https://api.anthropic.com/v1', '{"claude-3-haiku":{"alias":"claude-3-haiku-20240307","inputPrice":0.25,"outputPrice":1.25},"claude-3-sonnet":{"alias":"claude-3-sonnet-20240229","inputPrice":3.0,"outputPrice":15.0},"claude-3-opus":{"alias":"claude-3-opus-20240229","inputPrice":15.0,"outputPrice":75.0},"claude-3.5-haiku":{"alias":"claude-3-5-haiku-20241022","inputPrice":1.0,"outputPrice":5.0},"claude-3.5-sonnet":{"alias":"claude-3-5-sonnet-20241022","inputPrice":3.0,"outputPrice":15.0},"claude-3.7-sonnet":{"alias":"claude-3-7-sonnet-20241101","inputPrice":3.0,"outputPrice":15.0},"claude-4.0-sonnet":{"alias":"claude-4-0-sonnet-20241201","inputPrice":5.0,"outputPrice":25.0},"claude-4.5-sonnet":{"alias":"claude-4-5-sonnet-20241201","inputPrice":5.0,"outputPrice":25.0}}', 'claudeService', 1),
    ('azure_openai', 'azure', 'llm', 'https://azure.microsoft.com/zh-cn/products/ai-services/openai-service', 'https://{deployment}.openai.azure.com', '{"azure-gpt-4o":{"alias":"gpt-4o","inputPrice":2.5,"outputPrice":10.0},"azure-gpt-4o-mini":{"alias":"gpt-4o-mini","inputPrice":0.15,"outputPrice":0.6},"azure-gpt-35-turbo":{"alias":"gpt-35-turbo","inputPrice":1.5,"outputPrice":2.0}}', 'azureService', 1),
    ('bedrock_llm', 'bedrock', 'llm', 'https://aws.amazon.com/bedrock', 'https://bedrock-runtime.{region}.amazonaws.com', '{"bedrock-claude-3-haiku":{"alias":"anthropic.claude-3-haiku-20240307-v1:0","inputPrice":0.25,"outputPrice":1.25},"bedrock-claude-3-sonnet":{"alias":"anthropic.claude-3-sonnet-20240229-v1:0","inputPrice":3.0,"outputPrice":15.0},"bedrock-claude-3.5-sonnet":{"alias":"anthropic.claude-3-5-sonnet-20241022-v2:0","inputPrice":3.0,"outputPrice":15.0}}', 'bedrockService', 1);

-- 令牌管理表
create table oneapi_token
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP comment '创建时间',
    gmt_modified timestamp default CURRENT_TIMESTAMP comment '修改时间',
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
) comment '令牌管理表';

-- 令牌使用记录表
create table oneapi_token_usage
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP comment '创建时间',
    gmt_modified timestamp default CURRENT_TIMESTAMP comment '修改时间',
    token_id     int comment '令牌ID',
    provider     varchar(64) comment '服务提供商',
    model        varchar(64) comment '使用的模型',
    request_tokens int default 0 comment '请求令牌数',
    response_tokens int default 0 comment '响应令牌数',
    cost         decimal(10,10) default 0 comment '成本',
    status       int comment '调用状态：1成功，0失败',
    error_msg    varchar(512) comment '错误信息',
    ip_address   varchar(45) comment '客户端IP地址'
) comment '令牌使用记录表';

-- 插入默认的令牌示例
INSERT INTO oneapi_token (name, api_key, description, creator, status) VALUES
    ('默认令牌', 'sk-oneapi-default-token-2024', '系统默认令牌，用于测试', 'system', 1);

-- 创建索引
create index idx_token_usage_gmt_create on oneapi_token_usage(gmt_create);