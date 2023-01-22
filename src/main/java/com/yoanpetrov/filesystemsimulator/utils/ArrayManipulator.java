package com.yoanpetrov.filesystemsimulator.utils;

public class ArrayManipulator {

	/**
	 * Returns a sub array of the given array from the starting to the ending index.
	 * @param bytes the array to get the sub array from.
	 * @param start the starting index (inclusive).
	 * @param end the ending index (exclusive).
	 * @return the sub array.
	 */
	public static byte[] subArray(byte[] bytes, int start, int end) {
		if (start > end) {
			return new byte[] {};
		}

		if (end > bytes.length) {
			end = bytes.length;
		}

		byte[] result = new byte[end - start];
		int resultIndex = 0;

		for (int i = start; i < end; i++) {
			result[resultIndex++] = bytes[i];
		}

		return result;
	}

	/**
	 * Copies the specified amount of bytes from one array to another.
	 * @param from the array to copy the bytes from.
	 * @param to the array to copy the bytes to.
	 * @param length the amount of bytes to be copied.
	 */
	public static void copyArray(byte[] from, byte[] to, int length) {
		if (length > to.length) {
			length = to.length;
		}

		for (int i = 0; i < length; i++) {
			to[i] = from[i];
		}
	}

	/**
	 * Copies the specified amount of objects from one array to another.
	 * @param from the array to copy the objects from.
	 * @param to the array to copy the objects to.
	 * @param length the amount of objects to be copied.
	 */
	public static void copyArray(Object[] from, Object[] to, int length) {
		if (length > to.length) {
			length = to.length;
		}

		for (int i = 0; i < length; i++) {
			to[i] = from[i];
		}
	}

	/**
	 * Shifts all elements that are to the right of the given position and before the given max size, to the left once.
	 * @param array the array to shift the elements of.
	 * @param pos the starting position.
	 * @param maxSize the size of the array or the last index of the array before the shift stops.
	 */
	public static void shiftArrayLeft(int[] array, int pos, int maxSize) {
		if (pos < maxSize && pos > -1) {
			for (int i = pos; i < maxSize - 1; i++) {
				array[i] = array[i + 1];
			}

			array[maxSize - 1] = -1;
		}
	}

	/**
	 * Shifts all elements that are to the right of the given position and before the given max size, to the left once.
	 * @param array the array to shift the elements of.
	 * @param pos the starting position.
	 * @param maxSize the size of the array or the last index of the array before the shift stops.
	 */
	public static void shiftArrayLeft(String[] array, int pos, int maxSize) {
		if (pos < maxSize && pos > -1) {
			for (int i = pos; i < maxSize - 1; i++) {
				array[i] = array[i + 1];
			}

			array[maxSize - 1] = null;
		}
	}

	/**
	 * Fills the array with the given value.
	 * @param array the array to be filled.
	 * @param value the value to fill the array with.
	 */
	public static void fillArray(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			array[i] = value;
		}
	}

	/**
	 * Fills the array with the given value.
	 * @param array the array to be filled.
	 * @param value the value to fill the array with.
	 */
	public static void fillArray(byte[] array, byte value) {
		fillArray(array, value, array.length);
	}

	public static void fillArray(byte[] array, byte value, int len) {
		if (len > array.length) {
			len = array.length;
		}
		for (int i = 0; i < len; i++) {
			array[i] = value;
		}
	}

	/**
	 * Counts the amount of elements in the array. An element is counted if it has a value, different from 0.
	 *
	 * @param array the array to count from.
	 */
	public static int getElementCount(byte[] array) {
		int count = 0;
		for (byte b : array) {
			if (b == 0) {
				break;
			}
			count++;
		}
		return count;
	}

	public static void main(String[] args) {
		String str = "test string"; // 11
		System.out.println(getElementCount(str.getBytes()));
	}
}
