package com.yoanpetrov.filesystemsimulator.filestructures.container;

import com.yoanpetrov.filesystemsimulator.utils.ArrayManipulator;

/**
 * Represents a data block in a file system.
 */
public class DataBlock extends Block {

	public DataBlock() {
		super();
	}

	public DataBlock(byte[] bytes) {
		super(bytes);
	}

	/**
	 * Appends the given bytes to the end of the data block.
	 *
	 * @param newBytes the bytes to be appended.
	 */
	public void appendBytes(byte[] newBytes) {
		int appendedCount = 0;
		int lastElement = ArrayManipulator.getElementCount(bytes);
		for (int i = lastElement;
			 i < 512 && (appendedCount < newBytes.length);
			 i++) {
			bytes[i] = newBytes[appendedCount++];
		}
	}
}
