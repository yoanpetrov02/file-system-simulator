package com.yoanpetrov.filesystemsimulator.filestructures.data;

import com.yoanpetrov.filesystemsimulator.datastructures.*;
import com.yoanpetrov.filesystemsimulator.exceptions.FileSystemException;

/**
 * Represents a directory tree in a simulated file system. Used by FileSystem objects.
 */
public class DirectoryTree {

	final Node root;
	Node currentDir;
	StringList path;

	public DirectoryTree(String name, int inodeNumber) {
		root = new Node(name, inodeNumber, FileType.DIRECTORY, null);
		currentDir = root;
		path = new StringList();
		path.append(name);
	}

	/**
	 * Adds a child node with the given name, index node number and file type.
	 *
	 * @param name        the name of the node.
	 * @param inodeNumber the index node number of the node.
	 * @param type        the file type of the node.
	 */
	public void addChild(String name, int inodeNumber, FileType type) {
		Node newNode = new Node(name, inodeNumber, type, currentDir);
		currentDir.childNodes.append(newNode);
	}

	/**
	 * Removes the child node with the given name, if it is a child of the current node.
	 *
	 * @param name the name of the node to be removed.
	 */
	public void removeChild(String name) {
		Object[] nodes = currentDir.childNodes.toArray();
		Node nodeToRemove = null;
		for (Object node : nodes) {
			Node temp = (Node) node;
			if (temp.name.equals(name)) {
				nodeToRemove = temp;
			}
		}
		if (nodeToRemove != null) {
			currentDir.childNodes.remove(nodeToRemove);
		}
	}

	/**
	 * Removes the current node from the tree.
	 */
	public void removeCurrent() {
		String name = currentDir.name;
		goToParent();
		removeChild(name);
	}

	/**
	 * Checks whether the node with the given name is an existing child node of the current node.
	 *
	 * @param name the name of the node.
	 * @return true if the node exists and is a file, false otherwise.
	 */
	public boolean fileExists(String name) {
		Object[] nodes = currentDir.childNodes.toArray();
		for (Object node : nodes) {
			Node temp = (Node) node;
			if (temp.name.equals(name) && temp.type == FileType.FILE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the node with the given name is an existing child node of the current node.
	 *
	 * @param name the name of the node.
	 * @return true if the node exists and is a directory, false otherwise.
	 */
	public boolean dirExists(String name) {
		Object[] nodes = currentDir.childNodes.toArray();
		for (Object node : nodes) {
			Node temp = (Node) node;
			if (temp.name.equals(name) && temp.type == FileType.DIRECTORY) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the current node.
	 *
	 * @return the current node.
	 */
	public Node getCurrentDir() {
		return currentDir;
	}

	/**
	 * Returns the current path sequence
	 * (the sequence of directories in which the user is at the given moment) as a String.
	 * The path sequence is updated every time the current directory changes.
	 *
	 * @return the current path sequence.
	 */
	public String getPath() {
		String[] paths = path.toArray();
		StringAppender builder = new StringAppender();
		for (int i = 0; i < paths.length - 1; i++) {
			builder.append(paths[i] + "/");
		}
		builder.append(paths[paths.length - 1]);
		return builder.toString();
	}

	/**
	 * Returns the child node with the given name, if it exists.
	 *
	 * @param name the name of the child node.
	 * @return the child node with the given name, null if it doesn't exist.
	 */
	public Node getChild(String name) {
		Object[] nodes = currentDir.childNodes.toArray();
		for (Object node : nodes) {
			Node temp = (Node) node;
			if (temp.name.equals(name)) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * Goes to the directory that the passed path sequence points to, if it exists.
	 *
	 * @param pathSequence the path to the directory.
	 * @throws FileSystemException if one of the elements in the sequence does not point to an existing directory.
	 */
	public void goTo(String[] pathSequence)
			throws FileSystemException {
		for (String pathElement : pathSequence) {
			goToChildDir(pathElement);
		}
	}

	/**
	 * Goes to the parent node of the current node, if it exists.
	 */
	public void goToParent() {
		if (currentDir.parent != null) {
			currentDir = currentDir.parent;
			path.removeLast();
		}
	}

	/**
	 * Goes to the root node of the tree.
	 */
	public void goToRoot() {
		currentDir = root;
		path = new StringList();
		path.append("root");
	}

	/**
	 * Directory tree node. Each node contains information about a directory/file from the file system.
	 */
	public static class Node {

		public String name;
		public int inodeNumber;
		public FileType type;
		public Node parent;
		public LinkedList<Node> childNodes;

		Node(String name, int inodeNumber, FileType type, Node parent) {
			this.name = name;
			this.inodeNumber = inodeNumber;
			this.type = type;
			this.parent = parent;
			childNodes = new LinkedList<>();
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Goes to the child directory with the given name, if it exists.
	 *
	 * @param name the name of the directory.
	 * @throws FileSystemException if the name points to a file, or if the directory does not exist at all.
	 */
	private void goToChildDir(String name)
			throws FileSystemException {
		Object[] nodes = currentDir.childNodes.toArray();
		for (Object node : nodes) {
			Node temp = (Node) node;
			if (temp.name.equals(name)) {
				if (temp.type == FileType.FILE) {
					throw new FileSystemException("The specified path points to a file!");
				} else {
					currentDir = temp;
					path.append(currentDir.name);
					return;
				}
			}
		}
		throw new FileSystemException("The directory was not found!");
	}
}
