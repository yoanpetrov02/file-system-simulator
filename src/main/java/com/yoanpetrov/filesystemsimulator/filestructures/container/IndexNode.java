package com.yoanpetrov.filesystemsimulator.filestructures.container;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import com.yoanpetrov.filesystemsimulator.exceptions.FileSystemException;
import com.yoanpetrov.filesystemsimulator.filestructures.data.FileType;
import com.yoanpetrov.filesystemsimulator.utils.ArrayManipulator;

/**
 * Represents an index node file system structure. An index node contains information about a file,
 * like its size, name and references to the data blocks that the file occupies.
 */
public class IndexNode {

	public static final int INODE_SIZE = 256;

	static final int MAX_DIRECT_BLOCKS = 56;

	static final int MAX_NAME_SIZE = 16;

	FileType type;
	int size;
	int allocatedBlockCount;
	int[] directBlocks;
	int nameSize;
	byte[] name;

	public IndexNode() {
		initialize();
	}

	public IndexNode(String name, FileType type)
			throws FileSystemException {
		this();
		setName(name);
		this.type = type;
	}

	/**
	 * Writes the index node to the given file, at the file's current position.
	 *
	 * @param file the file to write the index node to.
	 */
	public void write(RandomAccessFile file) {
		try {
			file.writeShort(type == FileType.DIRECTORY ? 0 : 1);
			file.writeInt(size);
			file.writeInt(allocatedBlockCount);
			for (int i = 0; i < MAX_DIRECT_BLOCKS; i++) {
				file.writeInt(directBlocks[i]);
			}
			file.writeInt(nameSize);
			file.write(name);
		} catch (IOException e) {
			System.err.println("Error while writing index node to file.");
		}
	}

	/**
	 * Reads the index node from the given file, starting at the file's current position.
	 *
	 * @param file the file to read the index node from.
	 */
	public void read(RandomAccessFile file) {
		try {
			type = (file.readShort() == 0)
					? FileType.DIRECTORY
					: FileType.FILE;
			size = file.readInt();
			allocatedBlockCount = file.readInt();
			for (int i = 0; i < MAX_DIRECT_BLOCKS; i++) {
				directBlocks[i] = file.readInt();
			}
			nameSize = file.readInt();
			file.read(name);
		} catch (IOException e) {
			initialize();
		}
	}

	/**
	 * Adds a reference to a data block in the direct block list of the index node, if there is free space.
	 *
	 * @param block the number of the data block to be added.
	 * @throws FileSystemException if the maximum file/directory size is reached.
	 */
	public void addDirectBlock(int block)
			throws FileSystemException {
		if (isMaxSize()) {
			if (type == FileType.FILE) {
				throw new FileSystemException("Max file size reached");
			} else {
				throw new FileSystemException("Max directory size reached");
			}
		}
		directBlocks[allocatedBlockCount++] = block;
	}

	/**
	 * Removes a direct block from the direct block list of the index node.
	 *
	 * @param block the number of the data block to be removed.
	 */
	public void removeDirectBlock(int block) {
		for (int i = 0; i < allocatedBlockCount; i++) {
			if (directBlocks[i] == block) {
				ArrayManipulator.shiftArrayLeft(directBlocks, i, allocatedBlockCount--);
				return;
			}
		}
	}

	public int[] getDirectBlocks() {
		return directBlocks;
	}

	/**
	 * Returns an array containing the allocated direct blocks of the node.
	 *
	 * @return the allocated direct blocks of the node.
	 */
	public int[] getAllocatedDirectBlocks() {
		int[] result = new int[allocatedBlockCount - 1];
		int count = 0;
		for (int i = 1; i < allocatedBlockCount; i++) {
			result[count++] = directBlocks[i];
		}

		return result;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public int getAllocatedBlockCount() {
		return allocatedBlockCount;
	}

	public String getName() {
		return new String(Arrays.copyOfRange(name, 0, nameSize));
	}

	/**
	 * Sets the node's name to the given string.
	 *
	 * @param name the new name of the node.
	 * @throws FileSystemException if the given string is longer than the maximum allowed name size.
	 */
	public void setName(String name)
			throws FileSystemException {
		if (name.length() >= MAX_NAME_SIZE) {
			throw new FileSystemException("Name is longer than 16 chars!");
		}
		nameSize = name.length();
		for (int i = 0; i < nameSize; i++) {
			this.name[i] = (byte) name.charAt(i);
		}
	}

	public int getLastAllocatedBlock() {
		if (isEmpty()) {
			return -1;
		}
		return directBlocks[allocatedBlockCount - 1];
	}

	/**
	 * Checks whether the directory or file, represented by the index node, is empty.
	 *
	 * @return true if the directory/file is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return allocatedBlockCount < 2;
	}

	/**
	 * Returns the number of the index node's parent node.
	 *
	 * @return the first element of the direct block list, which represents the node's parent.
	 */
	public int getParent() {
		return directBlocks[0];
	}

	/**
	 * Checks whether the file is at the maximum size.
	 *
	 * @return true if the amount of allocated blocks has reached the max amount, false otherwise.
	 */
	public boolean isMaxSize() {
		return allocatedBlockCount >= MAX_DIRECT_BLOCKS;
	}

	/**
	 * Initializes the index node's fields.
	 */
	private void initialize() {
		directBlocks = new int[MAX_DIRECT_BLOCKS];
		ArrayManipulator.fillArray(directBlocks, -1);
		allocatedBlockCount = 0;
		size = 0;
		type = FileType.DIRECTORY;
		nameSize = 0;
		name = new byte[MAX_NAME_SIZE];
	}
}