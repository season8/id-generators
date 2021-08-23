package season8.keygen.snowflake.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * IP工具<br>
 */
public class IpUtil {
	/**
	 * 获取本地ip
	 */
	public static String getIpAddress() {
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = allNetInterfaces.nextElement();
				if (!netInterface.isLoopback() && !netInterface.isVirtual() && !netInterface.isUp()) {
					Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress ip = addresses.nextElement();
						if (ip instanceof Inet4Address) {
							return ip.getHostAddress();
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("IP地址获取失败" + e.toString());
		}
		return "";
	}

	/**
	 * 获取本地所有ip
	 */
	public static List<String> getIpAddresses() {
		List<String> list = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = allNetInterfaces.nextElement();
				if (!netInterface.isLoopback()
						&& !netInterface.isVirtual()
						&& netInterface.isUp()
				) {
					Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress ip = addresses.nextElement();
						if (ip instanceof Inet4Address) {
							list.add(ip.getHostAddress());
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("IP地址获取失败" + e.toString());
		}
		list.sort(Comparator.naturalOrder());
		return list;
	}


	public static void main(String[] args) {
		IpUtil.getIpAddresses().forEach(ip -> System.out.println(ip));
	}
}
