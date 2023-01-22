package com.yoanpetrov.filesystemsimulator.filestructures.container;

import java.io.IOException;
import java.io.RandomAccessFile;
import com.yoanpetrov.filesystemsimulator.utils.ArrayManipulator;

/**
 * A sequence of 512 bytes, representing a file system block.
 */
public abstract class Block {

	byte[] bytes;

	public Block() {
		bytes = new byte[512];
	}

	public Block(byte[] bytes) {
		this();
		setBytes(bytes);
	}

	/**
	 * Reads 512 bytes into the bytes array of the object, from the file's current position.
	 * If an I/O error occurs, the byte array gets reset.
	 *
	 * @param file the file to read the bytes from.
	 */
	public void read(RandomAccessFile file) {
		try {
			file.readFully(bytes);
		} catch (IOException e) {
			bytes = new byte[512];
		}
	}

	/**
	 * Writes the bytes array into the file, starting from its current position.
	 * Stops writing and returns if an I/O error occurs.
	 *
	 * @param file the file to write the bytes to.
	 */
	public void write(RandomAccessFile file) {
		try {
			file.write(bytes);
		} catch (IOException e) {
			System.err.println("Error while writing block to file.");
		}
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = new byte[512];
		ArrayManipulator.copyArray(bytes, this.bytes, bytes.length);
	}
}
