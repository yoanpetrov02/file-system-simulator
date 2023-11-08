package com.yoanpetrov.filesystemsimulator.filestructures.container;

/**
 * Represents a bitmap file system structure.
 */
public class Bitmap extends Block {

	public Bitmap() {
		super();
	}

	public Bitmap(byte[] bytes) {
		super(bytes);
	}

	/**
	 * Sets the bit at the given position.
	 *
	 * @param whichBit the position of the bit.
	 *                 The position will be interpreted from left to right,
	 *                 not right to left like usual binary numbers work.
	 */
	public void setBit(int whichBit) {
		int indexInBytesArr = whichBit / 8;
		int position = 7 - (whichBit - indexInBytesArr * 8);
		bytes[indexInBytesArr] |= (byte) (1 << position); // sets the bit at position to 1
	}

	/**
	 * Resets the bit at the given position.
	 *
	 * @param whichBit the position of the bit.
	 *                 The position will be interpreted from left to right,
	 *                 not right to left like usual binary numbers work.
	 */
	public void resetBit(int whichBit) {
		int indexInBytesArr = whichBit / 8;
		int position = 7 - (whichBit - indexInBytesArr * 8);
		bytes[indexInBytesArr] &= (byte) ~(1 << position); // sets the bit at position to 0
	}

	/**
	 * Finds the index of the first free bit (the first bit that is set) in the bitmap.
	 *
	 * @return the index of the found bit, -1 if there are no free bits.
	 */
	public int getFirstFreeBit() {
		int index = getFirstFreeByte();
		if (index != -1) {
			int bitNumber = getFirstFreeBitInByte(bytes[index]);
			return (index * 8) + bitNumber;
		}
		return -1;
	}

	/**
	 * Finds the first byte in the bitmap that isn't fully allocated (has at least 1 free bit).
	 *
	 * @return the index of the found byte, -1 if there are no free bytes.
	 */
	private int getFirstFreeByte() {
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] != 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds the first free bit (the first bit that is set) in the given byte.
	 *
	 * @param bits the byte to be searched.
	 * @return the index of the found bit within the byte, -1 if there isn't a free bit.
	 */
	private static int getFirstFreeBitInByte(byte bits) {
		for (int pos = 7; pos >= 0; pos--) {
			if ((bits & (1 << pos)) != 0) {
				return 7 - pos;
			}
		}
		return -1;
	}
}
