package season8.keygen.snowflake.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import season8.keygen.snowflake.entity.RegisterConfigProperties;

/**
 * nacos配置中心自动配置<br>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "snowflake.register.type", havingValue = "nacos")
public class SnowflakeNacosRegisterCenterConfiguration {
	private RegisterConfigProperties properties;

	public SnowflakeNacosRegisterCenterConfiguration(RegisterConfigProperties properties) {
		this.properties = properties;
	}
}
