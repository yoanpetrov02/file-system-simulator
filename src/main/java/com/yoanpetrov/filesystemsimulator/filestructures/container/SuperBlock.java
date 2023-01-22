package com.yoanpetrov.filesystemsimulator.filestructures.container;

import java.io.IOException;
import java.io.RandomAccessFile;
import com.yoanpetrov.filesystemsimulator.Testable;

/**
 * Represents a super block file system structure.
 */
public class SuperBlock implements Testable {

	static final int BYTES_TAKEN_IN_FILE = 30;

	short blockSize;
	int totalBlockCount;
	long maxSizeBytes;
	int inodeBitmapOffset;
	int dataBitmapOffset;
	int inodeBlockOffset;
	int dataBlockOffset;

	public SuperBlock() {
		blockSize = 512;
	}

	/**
	 * Writes the super block to the given file, at the file's current position.
	 *
	 * @param file the file to write the super block to.
	 */
	public void write(RandomAccessFile file)
			throws IOException {
		file.writeShort(blockSize);
		file.writeInt(totalBlockCount);
		file.writeLong(maxSizeBytes);
		file.writeInt(inodeBitmapOffset);
		file.writeInt(dataBitmapOffset);
		file.writeInt(inodeBlockOffset);
		file.writeInt(dataBlockOffset);
		file.skipBytes(512 - BYTES_TAKEN_IN_FILE);
	}

	/**
	 * Reads the super block from the given file, starting at the file's current position.
	 *
	 * @param file the file to read the super block from.
	 */
	public void read(RandomAccessFile file)
			throws IOException {
		blockSize = file.readShort();
		totalBlockCount = file.readInt();
		maxSizeBytes = file.readLong();
		inodeBitmapOffset = file.readInt();
		dataBitmapOffset = file.readInt();
		inodeBlockOffset = file.readInt();
		dataBlockOffset = file.readInt();
	}

	public short getBlockSize() {
		return blockSize;
	}

	public int getTotalBlockCount() {
		return totalBlockCount;
	}

	public int getInodeBitmapOffset() {
		return inodeBitmapOffset;
	}

	public int getDataBitmapOffset() {
		return dataBitmapOffset;
	}

	public int getInodeBlockOffset() {
		return inodeBlockOffset;
	}

	public int getDataBlockOffset() {
		return dataBlockOffset;
	}

	/**
	 * Prints useful debugging information about the object.
	 */
	@Override
	public void printDebug() {
		System.out.printf("Block size: %d%n", blockSize);
		System.out.printf("Total number of blocks: %d%n", totalBlockCount);
		System.out.printf("Max file system size: %d%n", maxSizeBytes);
		System.out.printf("Inode bitmap offset: %d%n", inodeBitmapOffset);
		System.out.printf("Data bitmap offset: %d%n", dataBitmapOffset);
		System.out.printf("Inode block offset: %d%n", inodeBlockOffset);
		System.out.printf("Data block offset: %d%n", dataBlockOffset);
	}

	/**
	 * Initializes the fields of the super block, calculating the offsets and setting them,
	 * depending on the given max size of the container.
	 *
	 * @param maxSizeBytes the max size of the container.
	 */
	public void initialize(long maxSizeBytes) {
		this.maxSizeBytes = maxSizeBytes;
		calculateOffsets();
	}

	/**
	 * Calculates the offsets of the super block.
	 */
	private void calculateOffsets() {
		int dataBlockCount = (int) Math.ceil(maxSizeBytes / 512.0);
		int maxFileCount = (int) Math.ceil(dataBlockCount / 56.0);
		short superBlockCount = 1;
		int inodeBitmapBlockCount = (int) Math.ceil(maxFileCount / 4096.0);
		int dataBitmapBlockCount = (int) Math.ceil(dataBlockCount / 4096.0);
		int inodeBlockCount = inodeBitmapBlockCount * 4096 / 2;
		totalBlockCount = superBlockCount + inodeBitmapBlockCount +
				dataBitmapBlockCount + inodeBlockCount + dataBlockCount;
		setOffsets(inodeBitmapBlockCount, dataBitmapBlockCount, inodeBlockCount);
	}

	/**
	 * Sets the offsets of the super block.
	 *
	 * @param inodeBitmapBlockCount the calculated inode bitmap block count.
	 * @param dataBitmapBlockCount  the calculated data bitmap block count.
	 * @param inodeBlockCount       the calculated inode block count.
	 */
	private void setOffsets(int inodeBitmapBlockCount, int dataBitmapBlockCount, int inodeBlockCount) {
		inodeBitmapOffset = 1;
		dataBitmapOffset = inodeBitmapOffset + inodeBitmapBlockCount;
		inodeBlockOffset = dataBitmapOffset + dataBitmapBlockCount;
		dataBlockOffset = inodeBlockOffset + inodeBlockCount;
	}
}
