package season8.keygen.snowflake.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import season8.keygen.snowflake.entity.RedisConfigProperties;
import season8.keygen.snowflake.entity.RegisterConfigProperties;

/**
 * 基于Jedis实现redis配置中心配置<br>
 */
@Slf4j
@ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class})
@ConditionalOnProperty(name = "snowflake.register.type", havingValue = "redis")
@Configuration
public class JedisFactoryConfiguration {

	private RegisterConfigProperties properties;

	public JedisFactoryConfiguration(RegisterConfigProperties properties) {
		this.properties = properties;
	}

	@Bean("snowFlakeRedisTemplate")
	@ConditionalOnMissingBean(name = "snowFlakeRedisTemplate")
	public RedisTemplate<String, String> jedisTemplate() {
		RedisTemplate<String, String> template = new RedisTemplate<String, String>() {
			@Override
			protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
				return new DefaultStringRedisConnection(connection);
			}
		};

		template.setConnectionFactory(jedisConnectionFactory(properties.getRedis()));
		template.setKeySerializer(RedisSerializer.string());
		template.setValueSerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		template.setHashValueSerializer(RedisSerializer.string());
		template.afterPropertiesSet();
		return template;
	}

	/**
	 * jedis连接工厂
	 */
	public JedisConnectionFactory jedisConnectionFactory(RedisConfigProperties properties) {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(properties.getHost());
		redisStandaloneConfiguration.setPort(properties.getPort());
		redisStandaloneConfiguration.setDatabase(properties.getDatabase());
		redisStandaloneConfiguration.setPassword(properties.getPassword());

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(1);
		poolConfig.setMinIdle(0);
		poolConfig.setMaxTotal(1);
		poolConfig.setMaxWaitMillis(2000);

		JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpcb = (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
		jpcb.poolConfig(poolConfig);
		JedisClientConfiguration jedisClientConfiguration = jpcb.build();
		return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
	}

}
