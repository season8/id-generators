package season8.keygen.snowflake.redis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Redis缓存类<br>
 */
@Slf4j
@Data
public class SnowflakeRedisConfigHelper {
	private static final Long RELEASE_SUCCESS = 1L;

	private RedisTemplate<String, String> stringRedisTemplate;

	public String hGget(String key, Object field) {
		return stringRedisTemplate.<String, String>boundHashOps(key).get(field);
	}

	public Boolean hSet(String key, String field, String value) {
		return stringRedisTemplate.<String, String>boundHashOps(key).putIfAbsent(field, value);
	}

	public Boolean hDel(String key, String field) {
		Long delete = stringRedisTemplate.boundHashOps(key).delete(field);
		return delete != null && delete == 1L;
	}

	public List<String> values(String key) {
		return stringRedisTemplate.<String, String>boundHashOps(key).values();
	}

	/**
	 * 设置锁，设置毫秒级别到期时间
	 */
	public boolean lock(final String key, String value, final long expMilliSeconds) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		try {
			RedisCallback<Boolean> callback = (connection) -> {
				byte[] serializeKey = stringRedisTemplate.getStringSerializer().serialize(key);
				byte[] serializeValue = stringRedisTemplate.getStringSerializer().serialize(value);
				return connection.set(serializeKey, serializeValue, Expiration.milliseconds(expMilliSeconds), RedisStringCommands.SetOption.SET_IF_ABSENT);
			};
			Boolean lock = stringRedisTemplate.execute(callback);
			return lock != null && lock;
		} catch (Exception e) {
			log.error("set redis occured an exception", e);
		}
		return false;
	}

	/**
	 * 释放分布式锁
	 *
	 * @param key   锁
	 * @param value 请求标识
	 * @return 是否释放成功
	 */
	public boolean releaseLock(String key, String value) {
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		Long result = (Long) stringRedisTemplate.execute(RedisScript.of(script, Long.class), Collections.singletonList(key), value);

		if (RELEASE_SUCCESS.equals(result)) {
			return true;
		}
		return false;

	}

	/**
	 * 删除锁
	 * 删除前判断值是否相同
	 * 该方法相对直接删除较安全
	 * 但由于get和del操作不是原子操作，还是可能会误删
	 */
	public boolean unlock(String key, String value) {
		return releaseLock(key, value);
	}


}
