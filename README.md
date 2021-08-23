## 简介

常用id生成器:

1. UUID: 生成处理后的uuid
2. SHORT-ID: 8位短id，非唯一id。
3. SNOWFLAKE-ID：基于雪花算法的id，支持分布式workerId注册中心。

### UUID

+ 长度：32（带‘-’36）
+ 内容： 数字字母混合
+ 唯一性：唯一（碰撞概率特别小）
+ 增长连续性：无增长趋势

默认获取不带 '-' 的 小写id 也可以根据重载方法自定义生成方式，参考 UUIDKeyGen 类。

### SHORT-ID

+ 长度：8
+ 内容： 字母大小写混合
+ 唯一性：不唯一
+ 增长连续性：无增长趋势

基于UUID生成，讲uuid每四位转成一个16进制数取模后生成，详见：ShortIdKeyGen。

### SNOWFLAKE-ID

+ 长度：18
+ 内容： 纯数字
+ 唯一性：唯一
+ 增长连续性：有一定的增长趋势

引发Id碰撞因素：

1. 需要避免时钟回拨问题（通常不会遇到）。
2. 多实例下机器Id（workerId）相同，可通过注册WorkerId解决。

## 快速使用

### 使用UUID

```java

```

#### 注册 WorkerId

范围 0~1024

1. 不主动注册，类自动生成随机
2. java注册：

```java
// 绑定默认的全局workerId
SnowflakeKeyGen.bindWorkerId(111)
```

```java
// 也可以构造实例
new SnowflakeKeyGen(111)
```

3. 注册中心自注册

> 只支持Spring-Boot

+ redis 注册中心，兼容jedis和lettuce

```yaml
snowflake:
    register:
        # 数据中心标识，非必须
        data-center-id: A33
        # 应用标识
        app-id: sales-order
        # 注册中心类型
        type: REDIS
        redis:
            host: 127.0.0.1
            port: 6379
            password: xxxxx
            database: 0
            timeout: 3000ms
```

+ nacos 注册中心，尚未支持
