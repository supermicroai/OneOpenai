-- 删除表（如果存在）
DROP TABLE IF EXISTS oneapi_token_usage;
DROP TABLE IF EXISTS oneapi_token;
DROP TABLE IF EXISTS oneapi_provider;
DROP TABLE IF EXISTS oneapi_model;
DROP TABLE IF EXISTS oneapi_config;
DROP TABLE IF EXISTS oneapi_account;

-- 创建账户表
CREATE TABLE oneapi_account
(
    id           SERIAL PRIMARY KEY,
    gmt_create   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    provider_code VARCHAR(32),
    name         VARCHAR(32),
    note         VARCHAR(64),
    api_key      VARCHAR(128),
    ak           VARCHAR(32),
    sk           VARCHAR(32),
    cost         DOUBLE PRECISION,
    balance      DOUBLE PRECISION,
    status       INTEGER
);

COMMENT ON TABLE oneapi_account IS '账户表';
COMMENT ON COLUMN oneapi_account.provider_code IS '关联的提供商代码';
COMMENT ON COLUMN oneapi_account.name IS '账号类型';
COMMENT ON COLUMN oneapi_account.cost IS '已花费金额';
COMMENT ON COLUMN oneapi_account.balance IS '账户余额';
COMMENT ON COLUMN oneapi_account.status IS '是否启用 1启用 0关闭 -1欠费';

-- 插入账户数据
INSERT INTO oneapi_account (provider_code, name, note, api_key, ak, sk, cost, balance, status) VALUES
    ('openrouter_llm', 'openrouter', 'admin@gmail.com', 'sk-or-v1-this-is-a-demo-key', null, null, 0, 50, 1);

-- 创建配置表
CREATE TABLE oneapi_config
(
    id           SERIAL PRIMARY KEY,
    gmt_create   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    config_group VARCHAR(16),
    config_key   VARCHAR(32),
    config_value TEXT,
    version      INTEGER DEFAULT 0 NOT NULL,
    note         TEXT,
    status       INTEGER DEFAULT 1 NOT NULL,
    env          VARCHAR(8) DEFAULT 'global',
    CONSTRAINT uk_key UNIQUE (config_key)
);

COMMENT ON TABLE oneapi_config IS '配置表';
COMMENT ON COLUMN oneapi_config.gmt_create IS '创建时间';
COMMENT ON COLUMN oneapi_config.gmt_modified IS '修改时间';
COMMENT ON COLUMN oneapi_config.config_group IS '配置分组';
COMMENT ON COLUMN oneapi_config.config_key IS '配置键';
COMMENT ON COLUMN oneapi_config.config_value IS '配置值';
COMMENT ON COLUMN oneapi_config.version IS '版本号';
COMMENT ON COLUMN oneapi_config.note IS '备注';
COMMENT ON COLUMN oneapi_config.status IS '状态 1启用 0禁用';
COMMENT ON COLUMN oneapi_config.env IS '环境';

-- 插入配置数据
INSERT INTO oneapi_config (config_group, config_key, config_value, version, note, status, env) VALUES
    ('大模型', 'oneapi.model.default', 'claude-3-haiku', 0, '默认模型名称', 1, 'global'),
    ('应用', 'log.enable', 'true', 0, '详细日志开关', 1, 'global'),
    ('告警', 'oneapi.alert.url', 'ding://xx', 0, '告警URL(支持钉钉/Slack等)。钉钉配置格式：ding://{access_token}，Slack配置格式：slack://{token}/{channel}', 1, 'global'),
    ('大模型', 'oneapi.success.rt', '60000', 0, '模型接口超时时间', 1, 'global'),
    ('账户', 'oneapi.credit.min', '5', 0, '账户余额告警阈值（元）', 1, 'global'),
    ('服务', 'oneapi.retry', '3', 0, '服务重试最大次数', 1, 'global');

-- 创建模型表
CREATE TABLE oneapi_model
(
    id           SERIAL PRIMARY KEY,
    gmt_create   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name         VARCHAR(32),
    vendor       VARCHAR(32),
    type         VARCHAR(16),
    input_price  DECIMAL(10,6) DEFAULT 0,
    output_price DECIMAL(10,6) DEFAULT 0,
    description  VARCHAR(512),
    enable       SMALLINT DEFAULT 1
);

COMMENT ON TABLE oneapi_model IS '模型表';
COMMENT ON COLUMN oneapi_model.gmt_create IS '创建时间';
COMMENT ON COLUMN oneapi_model.gmt_modified IS '修改时间';
COMMENT ON COLUMN oneapi_model.name IS '模型名称';
COMMENT ON COLUMN oneapi_model.vendor IS '服务商';
COMMENT ON COLUMN oneapi_model.type IS '模型类型';
COMMENT ON COLUMN oneapi_model.input_price IS '输入token价格（每1M个token）';
COMMENT ON COLUMN oneapi_model.output_price IS '输出token价格（每1M个token）';
COMMENT ON COLUMN oneapi_model.description IS '模型描述';
COMMENT ON COLUMN oneapi_model.enable IS '是否启用';

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
CREATE TABLE oneapi_provider
(
    id           SERIAL PRIMARY KEY,
    gmt_create   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    code         VARCHAR(32) NOT NULL UNIQUE,
    name         VARCHAR(32),
    type         VARCHAR(16),
    url          VARCHAR(256),
    api          VARCHAR(256),
    models       TEXT,
    service      VARCHAR(64),
    enable       INTEGER
);

COMMENT ON TABLE oneapi_provider IS '服务提供商表';
COMMENT ON COLUMN oneapi_provider.gmt_create IS '创建时间';
COMMENT ON COLUMN oneapi_provider.gmt_modified IS '修改时间';
COMMENT ON COLUMN oneapi_provider.code IS '提供者代码，唯一标识';
COMMENT ON COLUMN oneapi_provider.name IS '服务商名称';
COMMENT ON COLUMN oneapi_provider.type IS '服务类型';
COMMENT ON COLUMN oneapi_provider.url IS '服务提供商主页';
COMMENT ON COLUMN oneapi_provider.api IS '基础api地址';
COMMENT ON COLUMN oneapi_provider.models IS '支持的模型';
COMMENT ON COLUMN oneapi_provider.service IS '提供者对应的各类服务bean';
COMMENT ON COLUMN oneapi_provider.enable IS '是否启用';

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
CREATE TABLE oneapi_token
(
    id           SERIAL PRIMARY KEY,
    gmt_create   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name         VARCHAR(64) NOT NULL,
    api_key      VARCHAR(128) NOT NULL UNIQUE,
    description  VARCHAR(256),
    expire_time  TIMESTAMP NULL,
    max_usage    BIGINT DEFAULT -1,
    token_usage  BIGINT DEFAULT 0,
    max_cost_limit DECIMAL(10,6) DEFAULT -1,
    current_cost_usage DECIMAL(10,6) DEFAULT 0,
    status       INTEGER DEFAULT 1,
    creator      VARCHAR(64),
    last_used_time TIMESTAMP NULL
);

COMMENT ON TABLE oneapi_token IS '令牌管理表';
COMMENT ON COLUMN oneapi_token.gmt_create IS '创建时间';
COMMENT ON COLUMN oneapi_token.gmt_modified IS '修改时间';
COMMENT ON COLUMN oneapi_token.name IS '令牌名称';
COMMENT ON COLUMN oneapi_token.api_key IS 'API密钥';
COMMENT ON COLUMN oneapi_token.description IS '令牌描述';
COMMENT ON COLUMN oneapi_token.expire_time IS '过期时间，null表示永不过期';
COMMENT ON COLUMN oneapi_token.max_usage IS '最大token数限制，-1表示不限制';
COMMENT ON COLUMN oneapi_token.token_usage IS '当前token使用量';
COMMENT ON COLUMN oneapi_token.max_cost_limit IS '最大费用限制，-1表示不限制';
COMMENT ON COLUMN oneapi_token.current_cost_usage IS '当前费用使用量';
COMMENT ON COLUMN oneapi_token.status IS '状态：1启用，0禁用';
COMMENT ON COLUMN oneapi_token.creator IS '创建者';
COMMENT ON COLUMN oneapi_token.last_used_time IS '最后使用时间';

-- 令牌使用记录表
CREATE TABLE oneapi_token_usage
(
    id           SERIAL PRIMARY KEY,
    gmt_create   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    token_id     INTEGER,
    provider     VARCHAR(64),
    model        VARCHAR(64),
    request_tokens INTEGER DEFAULT 0,
    response_tokens INTEGER DEFAULT 0,
    cost         DECIMAL(10,10) DEFAULT 0,
    status       INTEGER,
    error_msg    VARCHAR(512),
    ip_address   VARCHAR(45)
);

COMMENT ON TABLE oneapi_token_usage IS '令牌使用记录表';
COMMENT ON COLUMN oneapi_token_usage.gmt_create IS '创建时间';
COMMENT ON COLUMN oneapi_token_usage.gmt_modified IS '修改时间';
COMMENT ON COLUMN oneapi_token_usage.token_id IS '令牌ID';
COMMENT ON COLUMN oneapi_token_usage.provider IS '服务提供商';
COMMENT ON COLUMN oneapi_token_usage.model IS '使用的模型';
COMMENT ON COLUMN oneapi_token_usage.request_tokens IS '请求令牌数';
COMMENT ON COLUMN oneapi_token_usage.response_tokens IS '响应令牌数';
COMMENT ON COLUMN oneapi_token_usage.cost IS '成本';
COMMENT ON COLUMN oneapi_token_usage.status IS '调用状态：1成功，0失败';
COMMENT ON COLUMN oneapi_token_usage.error_msg IS '错误信息';
COMMENT ON COLUMN oneapi_token_usage.ip_address IS '客户端IP地址';

-- 插入默认的令牌示例
INSERT INTO oneapi_token (name, api_key, description, creator, status) VALUES
    ('默认令牌', 'sk-oneapi-default-token-2024', '系统默认令牌，用于测试', 'system', 1);

-- 创建索引
CREATE INDEX idx_token_usage_gmt_create ON oneapi_token_usage(gmt_create);

-- 创建触发器来自动更新gmt_modified字段
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.gmt_modified = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为所有表添加自动更新gmt_modified的触发器
CREATE TRIGGER update_oneapi_account_modified BEFORE UPDATE ON oneapi_account FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_oneapi_config_modified BEFORE UPDATE ON oneapi_config FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_oneapi_model_modified BEFORE UPDATE ON oneapi_model FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_oneapi_provider_modified BEFORE UPDATE ON oneapi_provider FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_oneapi_token_modified BEFORE UPDATE ON oneapi_token FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_oneapi_token_usage_modified BEFORE UPDATE ON oneapi_token_usage FOR EACH ROW EXECUTE FUNCTION update_modified_column();

