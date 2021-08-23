package season8.keygen.snowflake.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.annotation.PostConstruct;

/**
 * 注册中心配置类<br>
 */
@Data
@ConfigurationProperties(prefix = "snowflake.register")
public class RegisterConfigProperties {
	/**
	 * 注册中心类型
	 */
	private RegisterCenterType type;
	@NestedConfigurationProperty
	private RedisConfigProperties redis;
	@NestedConfigurationProperty
	private NacosConfigProperties nacos;
	/**
	 * 数据中心Id
	 */
	private String dataCenterId = "default";
	/**
	 * 应用ID
	 */
	private String appId;

	@PostConstruct
	public void afterPropertiesSet() {
		if (type == null) {
			throw new RuntimeException("the register center type should be explicit");
		}

		if (StringUtils.isBlank(appId)) {
			throw new RuntimeException("the appId should be explicit");
		}

		if (type == RegisterCenterType.REDIS && redis == null) {
			throw new RuntimeException("the register center type is REDIS, but no redis properties is been set");
		}

		if (type == RegisterCenterType.NACOS && nacos == null) {
			throw new RuntimeException("the register center type is NACOS, but no nacos properties is been set");
		}
	}
}
