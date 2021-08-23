package season8.keygen.snowflake.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import season8.keygen.snowflake.entity.RegisterConfigProperties;

/**
 * 雪花workerId注册中心自动配置<br>
 */
@Configuration
@ConditionalOnProperty(name = "snowflake.register.type")
@EnableConfigurationProperties(RegisterConfigProperties.class)
@Import({SnowflakeRedisRegisterCenterConfiguration.class, SnowflakeNacosRegisterCenterConfiguration.class})
public class SnowflakeRegisterCenterAutoConfiguration {
}
