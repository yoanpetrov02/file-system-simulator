package com.yoanpetrov.filesystemsimulator.datastructures;

import com.yoanpetrov.filesystemsimulator.utils.ArrayManipulator;

/**
 * String appender class, simplified version of StringBuilder.
 * Strings can be appended to it in a loop, and the resulting value can be retrieved.
 */
public class StringAppender {

	private byte[] value;
	private int capacity;
	private int size;

	public StringAppender(String initial) {
		capacity = Math.max(initial.length(), 16);
		value = new byte[capacity];
		addValues(initial.getBytes());
		size = initial.length();
	}

	public StringAppender() {
		this("");
	}

	/**
	 * Appends the given value to the end of the string.
	 *
	 * @param str the string to be appended.
	 * @return the StringAppender the method was called, on, used to chain call append().
	 */
	public StringAppender append(String str) {
		if (size + str.length() >= capacity) {
			grow();
		}
		addValues(str.getBytes());
		return this;
	}

	/**
	 * Returns the value of the appender as a String.
	 *
	 * @return the string that the value of the appender represents.
	 */
	@Override
	public String toString() {
		byte[] str = new byte[size];
		for (int i = 0; i < size; i++) {
			str[i] = value[i];
		}
		return new String(str);
	}

	/**
	 * Adds the given values to the value array.
	 *
	 * @param values the values to be added.
	 */
	private void addValues(byte[] values) {
		for (byte b : values) {
			value[size++] = b;
		}
	}

	/**
	 * Creates an array twice as large as the value array,
	 * and copies its elements into the new array, effectively
	 * doubling the capacity of the appender.
	 */
	private void grow() {
		capacity *= 2;
		byte[] newValue = new byte[capacity];
		ArrayManipulator.copyArray(value, newValue, size);
		value = newValue;
	}
}
