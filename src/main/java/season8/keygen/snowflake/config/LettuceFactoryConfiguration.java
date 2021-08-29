package season8.keygen.snowflake.config;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import season8.keygen.snowflake.entity.RedisConfigProperties;
import season8.keygen.snowflake.entity.RegisterConfigProperties;

/**
 * 基于Lettuce实现redis配置中心配置<br>
 */
@Slf4j
@ConditionalOnClass({RedisClient.class})
@ConditionalOnProperty(name = "snowflake.register.type", havingValue = "redis")
@Configuration
public class LettuceFactoryConfiguration {

	private RegisterConfigProperties properties;

	public LettuceFactoryConfiguration(RegisterConfigProperties properties) {
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
		template.setConnectionFactory(lettuceConnectionFactory(properties.getRedis()));
		template.setKeySerializer(RedisSerializer.string());
		template.setValueSerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		template.setHashValueSerializer(RedisSerializer.string());
		template.afterPropertiesSet();
		return template;
	}

	/**
	 * lettuce连接工厂
	 */
	public LettuceConnectionFactory lettuceConnectionFactory(RedisConfigProperties properties) {
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMaxIdle(1);
		genericObjectPoolConfig.setMinIdle(0);
		genericObjectPoolConfig.setMaxTotal(1);
		genericObjectPoolConfig.setMaxWaitMillis(2000);
		genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(100);
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setDatabase(properties.getDatabase());
		redisStandaloneConfiguration.setHostName(properties.getHost());
		redisStandaloneConfiguration.setPort(properties.getPort());
		redisStandaloneConfiguration.setPassword(RedisPassword.of(properties.getPassword()));
		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.commandTimeout(properties.getTimeout())
				.poolConfig(genericObjectPoolConfig)
				.build();

		LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
		lettuceConnectionFactory.afterPropertiesSet();
		return lettuceConnectionFactory;
	}

}
