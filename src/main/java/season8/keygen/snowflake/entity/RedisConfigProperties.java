package season8.keygen.snowflake.entity;

import lombok.Data;

import java.time.Duration;

/**
 * redis 配置<br>
 */
@Data
public class RedisConfigProperties {
	private String url;
	private String host = "localhost";
	private int port = 6379;
	private String password;
	private int database = 0;
	private boolean ssl;
	private Duration timeout;
	private String clientName;
}
