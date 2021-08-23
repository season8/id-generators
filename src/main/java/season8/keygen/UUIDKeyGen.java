package season8.keygen;


import java.util.UUID;

/**
 * 基于UUID的键值生成器<br>
 */

public final class UUIDKeyGen {

	/**
	 * 生成不带'-'的小写id
	 */
	public static String generate() {
		return generate(true, true);
	}

	/**
	 * 生成ID
	 *
	 * @param noSpecialCharacter 去掉特殊字符（即：‘-’）
	 * @param toLowerCase        转小写
	 * @return id
	 */
	public static String generate(boolean noSpecialCharacter, boolean toLowerCase) {

		String s = UUID.randomUUID().toString();
		s = noSpecialCharacter ? s.replaceAll("-", "") : s;

		return toLowerCase ? s.toLowerCase() : s.toUpperCase();
	}
}
