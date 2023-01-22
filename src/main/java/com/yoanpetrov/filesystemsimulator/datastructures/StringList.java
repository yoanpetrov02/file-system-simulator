package com.yoanpetrov.filesystemsimulator.datastructures;

import com.yoanpetrov.filesystemsimulator.utils.ArrayManipulator;

/**
 * String representation of the ArrayList data structure.
 */
public class StringList {

	private int capacity;
	private int size;
	private String[] values;

	public StringList() {
		this(16);
	}

	public StringList(int initialCapacity) {
		capacity = initialCapacity;
		size = 0;
		values = new String[initialCapacity];
	}

	/**
	 * Appends the given string to the list.
	 *
	 * @param element the string to be appended.
	 */
	public void append(String element) {
		if (size == capacity) {
			grow();
		}
		values[size++] = element;
	}

	/**
	 * Removes the element at the given index from the list.
	 *
	 * @param index the index of the element that will be removed.
	 */
	public void remove(int index) {
		if (index < 0 || index >= size) {
			return;
		}
		ArrayManipulator.shiftArrayLeft(values, index, size);
		size--;
	}

	/**
	 * Removes the last element from the list.
	 */
	public void removeLast() {
		remove(size - 1);
	}

	/**
	 * Returns the values from the string list in an array.
	 *
	 * @return the array with the string list's values.
	 */
	public String[] toArray() {
		String[] result = new String[size];
		ArrayManipulator.copyArray(values, result, size);
		return result;
	}

	/**
	 * Creates an array twice as large as the values array,
	 * and copies its elements into the new array, effectively
	 * doubling the capacity of the appender.
	 */
	private void grow() {
		capacity *= 2;
		String[] newValues = new String[capacity];
		ArrayManipulator.copyArray(values, newValues, size);
		values = newValues;
	}

	/**
	 * Returns the value of the string list as a String.
	 *
	 * @return the resulting string after all the values are appended together.
	 */
	@Override
	public String toString() {
		StringAppender result = new StringAppender("");
		for (int i = 0; i < size - 1; i++) {
			result.append(values[i] + " ");
		}
		result.append(values[size - 1]);
		return result.toString();
	}
}
