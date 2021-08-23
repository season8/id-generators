package season8.keygen.snowflake.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import season8.keygen.snowflake.center.RedisRegisterCenter;
import season8.keygen.snowflake.entity.RegisterConfigProperties;
import season8.keygen.snowflake.redis.SnowflakeRedisConfigHelper;

/**
 * redis配置中心自动配置<br>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "snowflake.register.type", havingValue = "redis")
@Import({LettuceFactoryConfiguration.class, JedisFactoryConfiguration.class})
public class SnowflakeRedisRegisterCenterConfiguration {
	private RegisterConfigProperties properties;

	public SnowflakeRedisRegisterCenterConfiguration(RegisterConfigProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnBean(name = "snowFlakeRedisTemplate")
	@ConditionalOnMissingBean(SnowflakeRedisConfigHelper.class)
	public SnowflakeRedisConfigHelper snowflakeRedisConfigHelper(@Qualifier("snowFlakeRedisTemplate") RedisTemplate<String, String> stringRedisTemplate) {
		SnowflakeRedisConfigHelper snowflakeRedisConfigHelper = new SnowflakeRedisConfigHelper();
		snowflakeRedisConfigHelper.setStringRedisTemplate(stringRedisTemplate);
		return snowflakeRedisConfigHelper;
	}

	@Bean
	@ConditionalOnBean({SnowflakeRedisConfigHelper.class})
	@ConditionalOnMissingBean(RedisRegisterCenter.class)
	public RedisRegisterCenter redisRegisterCenter(SnowflakeRedisConfigHelper snowflakeRedisConfigHelper) {
		RedisRegisterCenter redisRegisterCenter = new RedisRegisterCenter();
		redisRegisterCenter.setDataCenterId(properties.getDataCenterId());
		redisRegisterCenter.setAppId(properties.getAppId());
		redisRegisterCenter.setRedisConfigHelper(snowflakeRedisConfigHelper);
//		redisRegisterCenter.init();
		return redisRegisterCenter;
	}
}
