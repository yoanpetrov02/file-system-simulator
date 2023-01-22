package com.yoanpetrov.filesystemsimulator.utils;

import com.yoanpetrov.filesystemsimulator.datastructures.StringList;

public class StringManipulator {

	public static String[] split(String string, char delimiter) {
		StringList tokens = new StringList();

		for (int i = 0; i < string.length(); i++) {
			int delimiterIndex = indexOf(string, delimiter, i);
			if (delimiterIndex != -1) {
				tokens.append(substring(string, i, delimiterIndex));
				i = delimiterIndex;
			} else {
				tokens.append(substring(string, i, string.length()));
				break;
			}
		}
		return tokens.toArray();
	}

	public static String[] split(String string, char delimiter, char ignoreBetween) {
		StringList tokens = new StringList();

		for (int i = 0; i < string.length(); i++) {
			int delimiterIndex;
			if (string.charAt(i) == ignoreBetween) {
				delimiterIndex = indexOf(string, ignoreBetween, i + 1) + 1;
			} else {
				delimiterIndex = indexOf(string, delimiter, i);
			}
			if (delimiterIndex != -1) {
				tokens.append(substring(string, i, delimiterIndex));
				i = delimiterIndex;
			} else {
				tokens.append(substring(string, i, string.length()));
				break;
			}
		}
		return tokens.toArray();
	}

	/**
	 * Returns a substring of the given string from start (inclusive) to end (exclusive).
	 * @param string the string to extract the substring from.
	 * @param start the starting index, inclusive.
	 * @param end the ending index, exclusive.
	 * @return the newly created substring.
	 */
	public static String substring(String string, int start, int end) {
		if (start > end)
			return "";

		if (end > string.length()) {
			end = string.length();
		}

		byte[] result = new byte[end - start];
		int resultIndex = 0;

		for (int i = start; i < end; i++) {
			result[resultIndex++] = (byte) string.charAt(i);
		}
		return new String(result);
	}

	public static int indexOf(String string, char needed, int from) {
		for (int i = from; i < string.length(); i++) {
			if (string.charAt(i) == needed) {
				return i;
			}
		}
		return -1;
	}

	public static String toLowerCase(String input) {
		char[] chars = toCharArray(input);
		for (int i = 0; i < chars.length; i++) {
			if ((int)chars[i] >= 65 && (int)chars[i] <= 90) {
				chars[i] += 32;
			}
		}
		return String.valueOf(chars);
	}

	public static char[] toCharArray(String input) {
		char[] result = new char[input.length()];
		for (int i = 0; i < input.length(); i++) {
			result[i] = input.charAt(i);
		}
		return result;
	}

	public static String removeQuotes(String input) {
		if (input.length() == 0) return "";
		int start = 0;
		int end = input.length();
		if (input.charAt(0) == '"') {
			start = 1;
		}
		if (input.charAt(input.length() - 1) == '"') {
			end = input.length() - 1;
		}
		return substring(input, start, end);
	}

	public static void main(String[] args) {
		System.out.println(removeQuotes("when the"));
	}
}
