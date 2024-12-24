create table oneapi_account
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    name         varchar(32),
    note         varchar(64),
    api_key      varchar(128),
    ak           varchar(32),
    sk           varchar(32),
    cost         double,
    balance      double,
    status       int
);

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

create table oneapi_model
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    name         varchar(32),
    type         varchar(16),
    enable       tinyint default 1
);

create table oneapi_provider
(
    id           int auto_increment primary key,
    gmt_create   timestamp default CURRENT_TIMESTAMP,
    gmt_modified timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    name         varchar(32),
    type         varchar(16),
    url          varchar(256),
    api          varchar(256),
    models       clob,
    service      varchar(64),
    enable       int
);

INSERT INTO oneapi_model (name, type, enable) VALUES
    ('bge-large-zh-v1.5', 'embedding', 1),
    ('bge-m3', 'embedding', 1),
    ('claude-3-haiku', 'llm', 1),
    ('claude-3.5-haiku', 'llm', 1),
    ('claude-3.5-sonnet', 'llm', 1),
    ('deepseek-chat', 'llm', 1),
    ('deepseek-chat-v2', 'llm', 1),
    ('gpt-3.5-turbo', 'llm', 1),
    ('gpt-4o', 'llm', 1),
    ('gpt-4o-mini', 'llm', 1),
    ('llama-3.1-405b', 'llm', 1),
    ('llama-3.1-70b', 'llm', 1),
    ('llama-3.1-8b', 'llm', 1),
    ('o1', 'llm', 1),
    ('o1-mini', 'llm', 1),
    ('o1-preview', 'llm', 1),
    ('ocr-ali-v1', 'ocr', 1),
    ('qwen-math-plus', 'llm', 1),
    ('qwen-max', 'llm', 1),
    ('qwen-vl-max', 'llm', 1),
    ('qwen-vl-plus', 'llm', 1),
    ('qwen1.5-110b', 'llm', 1),
    ('qwen1.5-72b', 'llm', 1),
    ('qwen2-57b', 'llm', 1),
    ('qwen2-72b', 'llm', 1),
    ('qwen2.5-72b', 'llm', 1),
    ('qwen2.5-math-72b', 'llm', 1),
    ('text-embedding-3-large', 'embedding', 1),
    ('text-embedding-3-small', 'embedding', 1),
    ('text-embedding-ada-002', 'embedding', 1),
    ('text-embedding-v3', 'embedding', 1);

INSERT INTO oneapi_provider (name, type, url, api, models, service, enable) VALUES
    ('openrouter', 'llm', 'https://openrouter.ai', 'https://openrouter.ai/api/v1', '{"o1":"openai/o1-preview-2024-09-12","claude-3-haiku":"anthropic/claude-3-haiku","claude-3.5-sonnet":"anthropic/claude-3.5-sonnet","llama-3.1-8b":"meta-llama/llama-3.1-8b-instruct","gpt-4o-mini":"openai/gpt-4o-mini","o1-preview":"openai/o1-preview-2024-09-12","llama-3.1-405b":"meta-llama/llama-3.1-405b-instruct","gpt-3.5-turbo":"openai/gpt-3.5-turbo","llama-3.1-70b":"meta-llama/llama-3.1-70b-instruct","claude-3.5-haiku":"anthropic/claude-3-5-haiku","llama-3-70b":"meta-llama/llama-3-70b","o1-mini":"openai/o1-mini","gpt-4o":"openai/gpt-4o"}', 'openRouterService', 1),
    ('aliyun', 'llm', 'https://bailian.console.aliyun.com', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '{"qwen2.5-72b": "qwen2.5-72b-instruct","qwen2.5-math-72b": "qwen2.5-math-72b-instruct","qwen2-72b": "qwen2-72b-instruct","qwen2-57b": "qwen2-57b-a14b-instruct","qwen1.5-110b": "qwen1.5-110b-chat","qwen1.5-72b": "qwen1.5-72b-chat","qwen-vl-plus": "qwen-vl-plus","qwen-vl-max": "qwen-vl-max","qwen-math-plus": "qwen-math-plus"}', 'aliyunService', 1),
    ('siliconflow', 'llm', 'https://cloud.siliconflow.cn', 'https://api.siliconflow.cn/v1', '{"deepseek-chat-v2": "deepseek-ai/DeepSeek-V2-Chat"}', 'siliconService', 1),
    ('aliyun', 'embedding', 'https://bailian.console.aliyun.com', 'https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding', '{"text-embedding-v3": "text-embedding-v3"}', 'aliyunService', 1),
    ('siliconflow', 'embedding', 'https://cloud.siliconflow.cn', 'https://api.siliconflow.cn/v1/embeddings', '{"bge-large-zh-v1.5": "BAAI/bge-large-zh-v1.5","bge-m3": "BAAI/bge-m3"}', 'siliconService', 1),
    ('aliyun', 'ocr', 'https://bailian.console.aliyun.com', '', '{"ocr-ali-v1": "ocr-ali-v1"}', 'aliyunService', 1);

INSERT INTO oneapi_config (config_group, config_key, config_value, version, note, status, env) VALUES
    ('大模型', 'oneapi.model.default', 'claude-3-haiku', 0, '默认模型名称', 1, 'global'),
    ('大模型', 'oneapi.apiKeys', '["sk-oneapi-oneapi"]', 0, '接受的apikey', 1, 'global'),
    ('应用', 'log.enable', 'true', 0, '详细日志开关', 1, 'global'),
    ('告警', 'oneapi.alert.ding', 'https://oapi.dingtalk.com/robot/send?access_token=xx', 0, '钉钉告警机器人', 1, 'global'),
    ('大模型', 'oneapi.success.rt', '60000', 0, '模型接口超时时间', 1, 'global');

INSERT INTO oneapi_account (name, note, api_key, ak, sk, cost, balance, status) VALUES
    ('openrouter', 'admin@gmail.com', 'sk-or-v1-this-is-a-demo-key', null, null, 0, 50, 1);
