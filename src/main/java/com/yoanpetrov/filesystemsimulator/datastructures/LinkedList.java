package com.yoanpetrov.filesystemsimulator.datastructures;

/**
 * Linked list data structure.
 *
 * @param <T> the type of the values inside the list.
 */
public class LinkedList<T> {

	Node head;
	int size;

	public LinkedList() {
		head = null;
		size = 0;
	}

	/**
	 * Appends the value to the list.
	 *
	 * @param value the value to be appended.
	 */
	public void append(T value) {
		size++;
		Node newNode = new Node(value);
		if (head == null) {
			head = new Node(value);
			return;
		}
		newNode.next = null;
		Node last = head;
		while (last.next != null) {
			last = last.next;
		}
		last.next = newNode;
	}

	/**
	 * Removes the value from the list.
	 *
	 * @param value the value to be removed.
	 */
	public void remove(T value) {
		Node temp = head;
		Node previous = null;
		if (temp != null && temp.data.equals(value)) {
			head = temp.next;
			size--;
			return;
		}
		while (temp != null && !temp.data.equals(value)) {
			previous = temp;
			temp = temp.next;
		}

		if (temp == null) {
			return;
		}
		size--;
		previous.next = temp.next;
	}

	/**
	 * Returns the values from the linked list in an array.
	 *
	 * @return the array with the linked list's values.
	 */
	public Object[] toArray() {
		Object[] values = new Object[size];
		Node current = head;
		for (int i = 0; i < size && current != null; i++) {
			values[i] = current.data;
			current = current.next;
		}
		return values;
	}

	/**
	 * Prints the elements of the list.
	 */
	public void print() {
		Node current = head;
		while (current != null) {
			System.out.print(current.data + " ");
			current = current.next;
		}
		System.out.println();
	}

	/**
	 * Linked list node, contains a value and a reference to the next node.
	 */
	static class Node {
		Object data;
		Node next;

		Node(Object data) {
			this.data = data;
		}
	}
}
