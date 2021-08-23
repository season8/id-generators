package season8.keygen.snowflake.center;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import season8.keygen.snowflake.SnowflakeKeyGen;
import season8.keygen.snowflake.redis.SnowflakeRedisConfigHelper;
import season8.keygen.snowflake.util.IpUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;

/**
 * redis 注册中心<br>
 */
@Slf4j
@Data
@ConditionalOnBean(SnowflakeRedisConfigHelper.class)
@Component
public class RedisRegisterCenter {

	/**
	 * 注册中心Hash键
	 */
	public static final String KEY = "SNOWFLAKE_WORKER_ID";
	/**
	 * 锁前缀
	 */
	public static final String LOCK_PREFIX = "SNOWFLAKE_WORKER_ID_LOCK:";
	/**
	 * 默认重试次数
	 */
	private static final int TIMES = 3;
	/**
	 * 数据中心id
	 */
	private String dataCenterId = "0";
	/**
	 * 应用id
	 */
	private String appId;
	/**
	 * 机器code
	 */
	private String machineCode;
	/**
	 * 机器id，根据id计算得到
	 */
	private Integer machineId;
	/**
	 * 本地ip地址
	 */
	private List<String> localIps;
	/**
	 * 注册失败重试次数
	 */
	private int retryTimes;

	@Autowired
	private SnowflakeRedisConfigHelper redisConfigHelper;

	/**
	 * 初始化
	 */
	@PostConstruct
	public void init() {
		localIps = IpUtil.getIpAddresses();

		if (CollectionUtils.isEmpty(localIps)) {
			log.error("[snowflake-redis] fetch local ip failed!!!");
			return;
		}
		machineCode = dataCenterId + ":[" + String.join(",", localIps) + "]:" + appId;

		initMachineId();

		SnowflakeKeyGen.bindWorkerId(machineId);
	}


	/**
	 * 容器销毁前清除注册记录
	 */
	@PreDestroy
	public void destroyMachineId() {
		if (StringUtils.isBlank(machineCode)) {
			return;
		}
		Boolean success = null;
		try {
			success = redisConfigHelper.hDel(KEY, machineCode);
		} catch (Exception e) {
			log.error("", e);
		}
		log.info("[snowflake-redis] unregisterMachineId -> machineCode:{} machineId:{},success:{}", machineCode, machineId, success);
	}

	/**
	 * 初始化机器ID
	 */
	private void initMachineId() {

		if (StringUtils.isBlank(machineCode)) {
			return;
		}

		machineId = queryRegisteredMachineId();

		if (machineId != null) {
			return;
		}

		machineId = createAndRegister(retryTimes <= 0 ? TIMES : retryTimes);

		if (machineId == null) {
			machineId = randomMachineId();
			log.warn("[snowflake-redis] register failed, use a random value:{} (it might clash with other services)", machineId);
		}
	}

	/**
	 * 查询已注册的ID
	 */
	private Integer queryRegisteredMachineId() {
		String s = redisConfigHelper.hGget(KEY, machineCode);
		if (StringUtils.isBlank(s)) {
			return null;
		}
		return Integer.parseInt(s);
	}

	/**
	 * 创建并注册
	 */
	public Integer createAndRegister(int times) {
		if (times <= 0) {
			return null;
		}
		List<String> values = redisConfigHelper.values(KEY);

		int id = randomMachineId();
		while (values != null && values.contains(String.valueOf(id))) {
			id = randomMachineId();
		}

		String lockKey = LOCK_PREFIX + id;
		String lockValue = UUID.randomUUID().toString();
		boolean lock = redisConfigHelper.lock(lockKey, lockValue, 3000);

		if (!lock) {
			return createAndRegister(--times);
		}

		try {
			values = redisConfigHelper.values(KEY);
			if (values.contains(String.valueOf(id))) {
				return createAndRegister(--times);
			}

			Boolean success = redisConfigHelper.hSet(KEY, machineCode, Integer.toString(id));

			log.info("[snowflake-redis] registerMachineId -> machineCode:{} machineId:{},success:{}", machineCode, id, success);

			if (!success) {
				return createAndRegister(--times);
			}

			return id;
		} finally {
			redisConfigHelper.unlock(lockKey, lockValue);
		}
	}

	/**
	 * 随机id
	 */
	private int randomMachineId() {
		return RandomUtils.nextInt(0, 1024);
	}
}
