package season8.keygen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.util.Calendar;

/**
 * 基于雪花算法的键值生成器<br>
 */
@Slf4j
public final class SnowflakeKeyGen {
	public static final long EPOCH;
	private static final long SEQUENCE_BITS = 12L;
	private static final long WORKER_ID_BITS = 10L;
	private static final long SEQUENCE_MASK = 4095L;
	private static final long WORKER_ID_LEFT_SHIFT_BITS = 12L;
	private static final long TIMESTAMP_LEFT_SHIFT_BITS = 22L;
	private static final long WORKER_ID_MAX_VALUE = 1024L;
	private static final long WORKER_ID = 0L;
	private static final int DEFAULT_VIBRATION_VALUE = 1;
	private static final int MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 10;

	private int sequenceOffset = -1;
	private long sequence;
	private long lastMilliseconds;
	/**
	 * 振幅
	 */
	private int maxVibrationOffset = 1;
	/**
	 * 时差容忍
	 */
	private int maxTolerateTimeDifference = 10;

	/**
	 * 默认机器Id，可手动指定，也可以通过注册中心自注册
	 */
	private static long defaultWorkerId = RandomUtils.nextInt(0, 1024);
	/**
	 * 机器ID，如果配置了，默认机器Id将不生效
	 */
	private Long workerId;
	/**
	 * 默认实例，不指定机器Id，使用默认的机器Id
	 */
	private static volatile SnowflakeKeyGen defaultInstance;

	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 10, 1);
		calendar.set(11, 0);
		calendar.set(12, 0);
		calendar.set(13, 0);
		calendar.set(14, 0);
		EPOCH = calendar.getTimeInMillis();
	}

	/**
	 * 默认生成器
	 */
	public static SnowflakeKeyGen defaultInstance() {
		if (defaultInstance != null) {
			return defaultInstance;
		}
		synchronized (SnowflakeKeyGen.class) {
			if (defaultInstance != null) {
				return defaultInstance;
			}
			defaultInstance = new SnowflakeKeyGen();
			return defaultInstance;
		}
	}

	/**
	 * 构造一个使用默认机器Id的生成器
	 */
	private SnowflakeKeyGen() {
	}

	/**
	 * 构造一个指定workerId的生成器
	 *
	 * @param workerId 自定义的机器id
	 */
	public SnowflakeKeyGen(Long workerId) {
		this.workerId = workerId;
	}

	public static Comparable<?> generate() {
		return defaultInstance().generateKey();
	}


	/**
	 * 生成id
	 */
	public synchronized Comparable<?> generateKey() {
		long currentMilliseconds = System.currentTimeMillis();
		if (this.waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
			currentMilliseconds = System.currentTimeMillis();
		}

		if (this.lastMilliseconds == currentMilliseconds) {
			if (0L == (this.sequence = this.sequence + 1L & 4095L)) {
				currentMilliseconds = this.waitUntilNextTime(currentMilliseconds);
			}
		} else {
			this.vibrateSequenceOffset();
			this.sequence = this.sequenceOffset;
		}

		this.lastMilliseconds = currentMilliseconds;
		return currentMilliseconds - EPOCH << 22 | getWorkerId() << 12 | this.sequence;
	}

	/**
	 * 设定 默认workerId
	 */
	public static void bindWorkerId(long workerId) {
		Validate.exclusiveBetween(0, 1024, workerId, "Illegal workerId:" + workerId + ", it is supposed to between 0 and 1024");
		log.info("init snowflake default workerId:{}", workerId);
		SnowflakeKeyGen.defaultWorkerId = workerId;
	}

	/**
	 * 设定 默认workerId
	 */
	public void setWorkerId(long workerId) {
		Validate.exclusiveBetween(0, 1024, workerId, "Illegal workerId:" + workerId + ", it is supposed to between 0 and 1024");
		log.info("init snowflake workerId:{}", workerId);
		this.workerId = workerId;
	}

	/**
	 * 优先使用设置的workerId
	 */
	public long getWorkerId() {
		return this.workerId != null ? workerId : defaultWorkerId;
	}

	/**
	 * 检查时差
	 */
	private boolean waitTolerateTimeDifferenceIfNeed(long currentMilliseconds) {
		try {
			if (this.lastMilliseconds <= currentMilliseconds) {
				return false;
			} else {
				long timeDifferenceMilliseconds = this.lastMilliseconds - currentMilliseconds;
				Validate.isTrue(timeDifferenceMilliseconds < (long) this.getMaxTolerateTimeDifferenceMilliseconds(), "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", this.lastMilliseconds, currentMilliseconds);
				Thread.sleep(timeDifferenceMilliseconds);
				return true;
			}
		} catch (InterruptedException e) {
			log.error("", e);
			return false;
		}
	}

	private long waitUntilNextTime(long lastTime) {
		long result;
		for (result = System.currentTimeMillis(); result <= lastTime; result = System.currentTimeMillis()) {
		}

		return result;
	}

	private void vibrateSequenceOffset() {
		this.sequenceOffset = this.sequenceOffset >= this.getMaxVibrationOffset() ? 0 : this.sequenceOffset + 1;
	}

	private int getMaxVibrationOffset() {
		return this.maxVibrationOffset;
	}

	public void setMaxVibrationOffset(int maxVibrationOffset) {
		Validate.isTrue(maxVibrationOffset >= 0 && (long) maxVibrationOffset <= 4095L, "Illegal maxVibrationOffset" + maxVibrationOffset + ", it is supposed to between 0 and 4095");
		this.maxVibrationOffset = maxVibrationOffset;
	}

	private int getMaxTolerateTimeDifferenceMilliseconds() {
		return this.maxTolerateTimeDifference;
	}

	public void setMaxTolerateTimeDifference(int maxTolerateTimeDifference) {
		this.maxTolerateTimeDifference = maxTolerateTimeDifference;
	}
}
