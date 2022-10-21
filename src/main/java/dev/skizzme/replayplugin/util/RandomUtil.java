package dev.skizzme.replayplugin.util;

import java.util.Random;

public class RandomUtil {
	
	public static int randomInt(int start, int end) {
		int range = end-start;
		return (int) Math.round((Math.random()*range)+start);
	}

	public static double randomDouble(double start, double end) {
		double range = end-start;
		return (double) (Math.random()*range)+start;
	}

	public static boolean randomBoolean() {
		return randomInt(0, 1) == 1 ? true : false;
	}

	public static boolean randomChance(double percent) {
		double rnd = randomDouble(0, 100)/100;
		return rnd <= percent/100;
	}

	public static String randomString(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		StringBuilder sb = new StringBuilder();
		Random rnd = new Random();
		while (sb.length() < length) {
			int index = (int) (rnd.nextFloat() * chars.length());
			sb.append(chars.charAt(index));
		}
		String rndStr = sb.toString();
		return rndStr;
	}

}
