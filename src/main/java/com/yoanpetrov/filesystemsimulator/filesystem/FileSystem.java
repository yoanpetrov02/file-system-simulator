package com.yoanpetrov.filesystemsimulator.filesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import com.yoanpetrov.filesystemsimulator.datastructures.LinkedList;
import com.yoanpetrov.filesystemsimulator.datastructures.StringAppender;
import com.yoanpetrov.filesystemsimulator.exceptions.FileSystemException;
import com.yoanpetrov.filesystemsimulator.filestructures.container.*;
import com.yoanpetrov.filesystemsimulator.filestructures.data.*;
import com.yoanpetrov.filesystemsimulator.utils.ArrayManipulator;
import com.yoanpetrov.filesystemsimulator.utils.StringManipulator;

public class FileSystem {

	public static final byte BYTE_MAX = (byte) 0xff; // 255

	RandomAccessFile containerFile;
	String systemPath;
	DirectoryTree tree;
	SuperBlock superBlock;
	IndexNode rootNode;
	IndexNode currentNode;
	DataBlock currentDataBlock;
	Bitmap currentInodeBitmapBlock;
	Bitmap currentDataBitmapBlock;
	int currentInodeBitmapIndex;
	int currentDataBitmapIndex;

	/**
	 * Constructs a FileSystem object, creating/overriding a file at the given path and creating a container with
	 * the given size (in bytes) there.
	 * @param systemPath the path to the file where the container should be created.
	 * @param size the maximum size in bytes of the file system's data segment (Note, the file system will require more
	 *             space than just the data segment for the information that it uses to manage the files).
	 * @throws FileSystemException if an error occurs while initializing the container or the object.
	 */
	public FileSystem(String systemPath, long size)
			throws FileSystemException {
		this.systemPath = systemPath;
		currentNode = new IndexNode();
		currentInodeBitmapBlock = new Bitmap();
		currentDataBitmapBlock = new Bitmap();
		currentInodeBitmapIndex = 0;
		currentDataBitmapIndex = 0;
		currentDataBlock = new DataBlock();
		tree = new DirectoryTree("root", 0);
		try {
			containerFile = new RandomAccessFile(systemPath, "rw");
			initialize(size);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while initializing the file system");
		}
	}

	/**
	 * Creates a file with the given name and file type.
	 * @param name the name of the new file.
	 * @param type the type of the new file.
	 * @throws FileSystemException if a file or directory with the same name already exists,
	 * or if the maximum directory size if reached, or if an i/o error occurs.
	 */
	public void makeFile(String name, FileType type)
			throws FileSystemException {
		if (tree.fileExists(name) || tree.dirExists(name)) {
			throw new FileSystemException(
					"A file/directory with the same name already exists");
		}
		try {
			int newInode = allocateInodeBlock();
			int parent = tree.getCurrentDir().inodeNumber;

			if (newInode != -1) {
				IndexNode resultNode = new IndexNode();
				resultNode.setName(name);
				resultNode.setType(type);
				resultNode.addDirectBlock(parent);
				writeIndexNode(resultNode, newInode);

				IndexNode parentNode = new IndexNode();
				readIndexNode(parentNode, parent);
				parentNode.addDirectBlock(newInode);
				writeIndexNode(parentNode, parent);
			}
			tree.addChild(name, newInode, type);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while creating the file");
		}
	}

	/**
	 * Deletes the current directory if it's empty.
	 * @throws FileSystemException if the current directory is not empty, or if an i/o error occurs.
	 */
	public void removeDir()
			throws FileSystemException {
		try {
			int currentIndex = tree.getCurrentDir().inodeNumber;
			readIndexNode(currentNode, currentIndex);
			if (!currentNode.isEmpty()) {
				throw new FileSystemException(
						"The current directory is not empty!");
			}
			int parentIndex = currentNode.getParent();
			removeDirectBlock(parentIndex, currentIndex);
			freeInodeBlock(currentIndex);
			tree.removeCurrent();
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while removing the current directory");
		}
	}

	/**
	 * Prints a list of the content in the current directory on the screen.
	 */
	public void listCurrentDir() {
		LinkedList<DirectoryTree.Node> nodes =
				tree.getCurrentDir().childNodes;
		System.out.print(".. ");
		nodes.print();
	}

	/**
	 * Changes the current directory to the specified one in the given path sequence.
	 * @param path the path sequence to the target directory.
	 * @throws FileSystemException if one of the elements in the sequence does not point to an existing directory.
	 */
	public void changeDir(String path)
			throws FileSystemException {
		if ("/".equals(path)) {
			tree.goToRoot();
			return;
		}
		if ("..".equals(path)) {
			goToParentDir();
			return;
		}
		String[] sequence =
				StringManipulator.split(path, '/');
		tree.goTo(sequence);
	}

	/**
	 * Copies the bytes from the source file to the destination file.
	 * @param sourceName the name of the source file.
	 * @param destinationName the name of the destination file.
	 * @throws FileSystemException if the source file doesn't exist or is a directory,
	 * if the destination file already exists or is a directory, or if an i/o error occurs.
	 */
	public void copyFile(String sourceName, String destinationName)
			throws FileSystemException {
		validateCopy(sourceName, destinationName);
		try {
			makeFile(destinationName, FileType.FILE);
			int sourceNumber =
					tree.getChild(sourceName).inodeNumber;
			int destNumber =
					tree.getChild(destinationName).inodeNumber;
			copyFileBlocks(sourceNumber, destNumber);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while copying the file");
		}
	}

	/**
	 * Deletes the file with the given name.
	 * @param fileName the name of the file to delete.
	 * @throws FileSystemException if the file doesn't exist or if an i/o error occurs.
	 */
	public void deleteFile(String fileName)
			throws FileSystemException {
		if (!tree.fileExists(fileName)) {
			throw new FileSystemException(
					"The specified file does not exist");
		}
		try {
			int inodeNumber = tree.getChild(fileName).inodeNumber;
			readIndexNode(currentNode, inodeNumber);
			for (int i = 1; i < currentNode.getAllocatedBlockCount(); i++) {
				wipeDataBlock(currentNode.getDirectBlocks()[i]);
			}
			removeDirectBlock(tree.getChild(fileName).inodeNumber, inodeNumber);
			freeInodeBlock(inodeNumber);
			tree.removeChild(fileName);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while deleting the file");
		}
	}

	/**
	 * Prints the content of the file on the screen.
	 * @param fileName the name of the file.
	 * @throws FileSystemException if the file does not exist or is a directory, or if an i/o error occurs.
	 */
	public void printFile(String fileName)
			throws FileSystemException {
		validatePrint(fileName);
		try {
			printBlocks(tree.getChild(fileName).inodeNumber);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while printing the file");
		}
	}

	/**
	 * Writes the given bytes to the specified file. If the file already exists, it gets overridden.
	 * @param fileName the name of the file.
	 * @param bytes the bytes to be written to the file.
	 * @throws FileSystemException if the file name points to a directory or if an i/o error occurs.
	 */
	public void writeToFile(String fileName, byte[] bytes)
			throws FileSystemException {
		validateWrite(fileName);
		try {
			makeFile(fileName, FileType.FILE);
			int inodeNumber = tree.getChild(fileName).inodeNumber;
			int neededBlocks = calculateNeededBlocks(bytes.length);
			writeBytesToBlocks(bytes, inodeNumber, neededBlocks);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while writing to the file");
		}
	}

	/**
	 * Appends the given bytes at the end of the specified file.
	 * If the file does not exist, this method calls writeToFile().
	 * @param fileName the name of the file.
	 * @param bytes the bytes to be appended to the file.
	 * @throws FileSystemException if the file name points to a directory or if an i/o error occurs.
	 */
	public void appendToFile(String fileName, byte[] bytes)
			throws FileSystemException {
		try {
			if (tree.dirExists(fileName)) {
				throw new FileSystemException(
						"The given name points to a directory");
			}
			if (!tree.fileExists(fileName)) {
				writeToFile(fileName, bytes);
				return;
			}
			int inodeNumber = tree.getChild(fileName).inodeNumber;
			appendBytesToBlocks(bytes, inodeNumber);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while appending to the file");
		}
	}

	/**
	 * Imports the file from the given external path to the destination file.
	 * @param externalPath the path in the external file system to import the file from.
	 * @param destinationFile the name of the file to copy the external file to.
	 * @throws FileSystemException if the external file doesn't exist, or if the destination file already exists/is
	 * a directory, or if an i/o error occurs.
	 */
	public void importFile(String externalPath, String destinationFile)
			throws FileSystemException {
		validateImport(externalPath, destinationFile);
		makeFile(destinationFile, FileType.FILE);
		try {
			importBlocks(externalPath, destinationFile);
		} catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while importing the file");
		}
	}

	/**
	 * Exports the given file to the external file system at the given external path.
	 * @param file the file to be exported.
	 * @param externalPath the path to the external file to copy the bytes to.
	 * @throws FileSystemException if the internal file doesn't exist, or if the external file already exists,
	 * or if an i/o error occurs.
	 */
	public void exportFile(String file, String externalPath)
			throws FileSystemException {
		validateExport(file, externalPath);

		try {
			exportBlocks(file, externalPath);
		}
		catch (IOException e) {
			throw new FileSystemException(
					"An i/o error occurred while exporting the file");
		}
	}

	/**
	 * Marks a block as allocated in the inode bitmap.
	 *
	 * @return the number of the block that was allocated.
	 */
	public int allocateInodeBlock()
			throws IOException {
		int oldIndex = currentInodeBitmapIndex;
		int inodeBitmapLength =
				superBlock.getDataBitmapOffset() - superBlock.getInodeBitmapOffset();

		for (int i = 0; i < inodeBitmapLength; i++) {
			readBitmap(
					currentInodeBitmapBlock,
					superBlock.getInodeBitmapOffset(), i);
			int firstFreeBit =
					currentInodeBitmapBlock.getFirstFreeBit();
			if (firstFreeBit != -1) {
				currentInodeBitmapBlock.resetBit(firstFreeBit);
				writeBitmap(
						currentInodeBitmapBlock,
						superBlock.getInodeBitmapOffset(),
						currentInodeBitmapIndex);
				return (i * superBlock.getBlockSize()) + firstFreeBit;
			}
		}
		currentInodeBitmapIndex = oldIndex;
		return -1;
	}

	/**
	 * Marks a block as free in the inode bitmap.
	 *
	 * @param inodeBlockNumber the number of the inode block to be freed.
	 */
	public void freeInodeBlock(int inodeBlockNumber)
			throws IOException {
		int blockToSeek = inodeBlockNumber / 4096;
		readBitmap(
				currentInodeBitmapBlock,
				superBlock.getInodeBitmapOffset(), blockToSeek);
		currentInodeBitmapBlock.setBit(
				(blockToSeek * 4096) + inodeBlockNumber);
		writeBitmap(
				currentInodeBitmapBlock,
				superBlock.getInodeBitmapOffset(),
				currentInodeBitmapIndex);
	}

	/**
	 * Marks a block as allocated in the data bitmap.
	 *
	 * @return the number of the block that was allocated.
	 */
	public int allocateDataBlock()
			throws IOException {
		int oldIndex = currentDataBitmapIndex;
		int dataBitmapLength =
				superBlock.getInodeBlockOffset() - superBlock.getDataBitmapOffset();
		for (int i = 0; i < dataBitmapLength; i++) {
			readBitmap(
					currentDataBitmapBlock,
					superBlock.getDataBitmapOffset(), i);
			int firstFreeBit =
					currentDataBitmapBlock.getFirstFreeBit();
			if (firstFreeBit != -1) {
				currentDataBitmapBlock.resetBit(firstFreeBit);
				writeBitmap(
						currentDataBitmapBlock,
						superBlock.getDataBitmapOffset(), currentDataBitmapIndex);
				return (i * superBlock.getBlockSize()) + firstFreeBit;
			}
		}
		currentDataBitmapIndex = oldIndex;
		return -1;
	}

	/**
	 * Marks a block as free in the data bitmap.
	 *
	 * @param dataBlockNumber the number of the data block to be freed.
	 */
	public void freeDataBlock(int dataBlockNumber)
			throws IOException {
		int blockToSeek = dataBlockNumber / 4096;
		readBitmap(
				currentDataBitmapBlock,
				superBlock.getDataBitmapOffset(), blockToSeek);
		currentDataBitmapBlock.setBit(
				(blockToSeek * 4096) + dataBlockNumber);
		writeBitmap(
				currentDataBitmapBlock,
				superBlock.getDataBitmapOffset(), currentDataBitmapIndex);
	}

	/**
	 * Reads the specified index node from the container into the given IndexNode.
	 *
	 * @param node            the destination for the read index node.
	 * @param indexNodeNumber the number of the index node.
	 */
	public void readIndexNode(IndexNode node, int indexNodeNumber)
			throws IOException {
		int blockToSeek =
				superBlock.getInodeBlockOffset() +
				indexNodeNumber / (superBlock.getBlockSize() / IndexNode.INODE_SIZE);
		long additional = 0;
		if (indexNodeNumber % 2 != 0) {
			additional = 256;
		}

		containerFile.seek(
				(long) blockToSeek * superBlock.getBlockSize() + additional);
		node.read(containerFile);
	}

	/**
	 * Writes the specified index node to the container.
	 *
	 * @param node            the index node to be written to the file.
	 * @param indexNodeNumber the number of the index node.
	 */
	public void writeIndexNode(IndexNode node, int indexNodeNumber)
			throws IOException {
		int blockToSeek =
				superBlock.getInodeBlockOffset() +
				indexNodeNumber / (superBlock.getBlockSize() / IndexNode.INODE_SIZE);
		long additional = 0;
		if (indexNodeNumber % 2 != 0) {
			additional = 256;
		}

		containerFile.seek(
				(long) blockToSeek * superBlock.getBlockSize() + additional);
		node.write(containerFile);
	}

	/**
	 * Loads the index node with the given number, adds the given block to its list of direct blocks and writes it
	 * back to the container.
	 * @param indexNodeNumber the number of the index node.
	 * @param blockToAdd the direct block to add.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file/directory size is already reached.
	 */
	public void addDirectBlock(int indexNodeNumber, int blockToAdd)
			throws IOException, FileSystemException {
		readIndexNode(currentNode, indexNodeNumber);
		currentNode.addDirectBlock(blockToAdd);
		writeIndexNode(currentNode, indexNodeNumber);
	}

	/**
	 * Loads the index node with the given number, removes the given block from its list of direct blocks and writes it
	 * back to the container.
	 *
	 * @param indexNodeNumber the number of the index node.
	 * @param blockToRemove   the direct block to remove.
	 * @throws IOException if an i/o error occurs.
	 */
	public void removeDirectBlock(int indexNodeNumber, int blockToRemove)
			throws IOException {
		readIndexNode(currentNode, indexNodeNumber);
		currentNode.removeDirectBlock(blockToRemove);
		writeIndexNode(currentNode, indexNodeNumber);
	}

	/**
	 * Reads the specified data block from the container into the given DataBlock object.
	 *
	 * @param block           the destination for the read data block.
	 * @param dataBlockNumber the number of the data block.
	 */
	public void readDataBlock(DataBlock block, int dataBlockNumber)
			throws IOException {
		int blockToSeek =
				superBlock.getDataBlockOffset() + dataBlockNumber;
		containerFile.seek(
				(long) blockToSeek * superBlock.getBlockSize());
		block.read(containerFile);
	}

	/**
	 * Writes the specified data block to the container.
	 * @param block the block to be written to the file.
	 * @param dataBlockNumber the number of the data block.
	 * @throws IOException if an i/o error occurs.
	 */
	public void writeDataBlock(DataBlock block, int dataBlockNumber)
			throws IOException {
		int blockToSeek =
				superBlock.getDataBlockOffset() + dataBlockNumber;
		containerFile.seek(
				(long) blockToSeek * superBlock.getBlockSize());
		block.write(containerFile);
	}

	public String getSystemPath() {
		return tree.getPath();
	}

	/**
	 * Reads the specified bitmap from the container into the given Bitmap object.
	 *
	 * @param bitmap       the destination for the read bitmap.
	 * @param offset       the offset for either the inode bitmap or the data bitmap, the corresponding current bitmap index
	 *                     will be updated accordingly.
	 * @param bitmapNumber the number of the bitmap.
	 */
	private void readBitmap(Bitmap bitmap, int offset, int bitmapNumber)
			throws IOException {
		int blockToSeek = offset + bitmapNumber;
		if (bitmapNumber == superBlock.getInodeBitmapOffset()) {
			currentInodeBitmapIndex = bitmapNumber;
		} else {
			currentDataBitmapIndex = bitmapNumber;
		}

		containerFile.seek(
				(long) blockToSeek * superBlock.getBlockSize());
		bitmap.read(containerFile);
	}

	/**
	 * Writes the specified bitmap to the container.
	 *
	 * @param bitmap       the bitmap to be written to the file.
	 * @param offset       the offset for either the inode bitmap or the data bitmap, the corresponding current bitmap index
	 *                     will be updated accordingly.
	 * @param bitmapNumber the number of the bitmap.
	 */
	private void writeBitmap(Bitmap bitmap, int offset, int bitmapNumber)
			throws IOException {
		int blockToSeek = offset + bitmapNumber;
		if (bitmapNumber == superBlock.getInodeBitmapOffset()) {
			currentInodeBitmapIndex = bitmapNumber;
		} else {
			currentDataBitmapIndex = bitmapNumber;
		}

		containerFile.seek(
				(long) blockToSeek * superBlock.getBlockSize());
		bitmap.write(containerFile);
	}

	/**
	 * Initializes the super block of the file system and creates the container.
	 * @param size the maximum size of the data segment (in bytes).
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if a file system error occurs.
	 */
	private void initialize(long size)
			throws IOException, FileSystemException {
		superBlock = new SuperBlock();
		superBlock.initialize(size);
		createFileSystem();
	}

	/**
	 * Overrides the container file, and segments it for a new file system.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if a file system error occurs.
	 */
	private void createFileSystem()
			throws IOException, FileSystemException {
		deleteExistingFileContent();
		containerFile.seek(
				superBlock.getTotalBlockCount() * 512L);
		containerFile.writeByte(0);
		containerFile.seek(0);
		superBlock.write(containerFile);
		initializeBitmaps();
		initializeRootNode();
	}

	/**
	 * Deletes the existing content in the container file.
	 * @throws IOException if an i/o error occurs.
	 */
	private void deleteExistingFileContent()
			throws IOException {
		new PrintWriter(systemPath).close();
	}

	/**
	 * Initializes the bitmap blocks of the file system and writee them to the container file.
	 * @throws IOException if an i/o error occurs.
	 */
	private void initializeBitmaps()
			throws IOException {
		byte[] bitmapBytes = new byte[512];
		ArrayManipulator.fillArray(bitmapBytes, BYTE_MAX);
		Bitmap bitmap = new Bitmap(bitmapBytes);

		containerFile.seek(
				superBlock.getInodeBitmapOffset() * 512L);
		long bitmapBlockCount =
				superBlock.getInodeBlockOffset() - superBlock.getInodeBitmapOffset();
		for (int i = 0; i < bitmapBlockCount; i++) {
			bitmap.write(containerFile);
		}
	}

	/**
	 * Initializes the root index node and writes it to the container file.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if a file system error occurs.
	 */
	private void initializeRootNode()
			throws IOException, FileSystemException {
		rootNode = new IndexNode();
		rootNode.setName("root");
		rootNode.addDirectBlock(allocateInodeBlock());
		containerFile.seek(
				superBlock.getInodeBlockOffset() * 512L);
		rootNode.write(containerFile);
	}

	/**
	 * Changes the current directory to the parent directory,
	 * if it exists (if it does not exist, the current dir is root).
	 */
	private void goToParentDir() {
		if (tree.getCurrentDir().parent == null) {
			return;
		}
		tree.goToParent();
	}

	/**
	 * Validates the source and destination for a copyFile() call.
	 * @param src the name of the source file.
	 * @param dest the name of the destination file.
	 * @throws FileSystemException if the source file doesn't exist or is a directory, or if the destination file
	 * already exists or is a directory.
	 */
	private void validateCopy(String src, String dest)
			throws FileSystemException {
		if (!tree.fileExists(src)) {
			throw new FileSystemException(
					"The specified file to copy does not exist or is a directory");
		}
		if (tree.fileExists(dest) || tree.dirExists(dest)) {
			throw new FileSystemException(
					"A file/directory with the same name as the destination file already exists");
		}
	}

	/**
	 * Copies the blocks from the source index node to the destination index node.
	 * @param sourceNumber the number of the source index node.
	 * @param destNumber the number of the destination index node.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file size is reached.
	 */
	private void copyFileBlocks(int sourceNumber, int destNumber)
			throws IOException, FileSystemException {
		IndexNode sourceNode = new IndexNode();
		IndexNode destinationNode = new IndexNode();
		readIndexNode(sourceNode, sourceNumber);
		readIndexNode(destinationNode, destNumber);

		copyDataBlocks(sourceNode, destinationNode);
		writeIndexNode(destinationNode, destNumber);
	}

	/**
	 * Copies the direct blocks from one index node to another.
	 * @param from the index node to copy the blocks from.
	 * @param to the index node to copy the blocks to.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file size is reached.
	 */
	private void copyDataBlocks(IndexNode from, IndexNode to)
			throws IOException, FileSystemException {
		for (int i = 1; i < from.getAllocatedBlockCount(); i++) {
			readDataBlock(
					currentDataBlock,
					from.getDirectBlocks()[i]);
			int allocatedBlock = allocateDataBlock();
			if (allocatedBlock != -1) {
				to.addDirectBlock(allocatedBlock);
				writeDataBlock(currentDataBlock, allocatedBlock);
			}
		}
	}

	/**
	 * Deletes the contents of a data block in the container, setting them to the byte value of 0.
	 * @param dataBlockNumber the number of the data block.
	 * @throws IOException if an i/o error occurs.
	 */
	private void wipeDataBlock(int dataBlockNumber)
			throws IOException {
		readDataBlock(
				currentDataBlock,
				dataBlockNumber);
		ArrayManipulator.fillArray(
				currentDataBlock.getBytes(), (byte) 0);
		freeDataBlock(dataBlockNumber);
		writeDataBlock(
				currentDataBlock,
				dataBlockNumber);
	}

	/**
	 * Validates the name of the file for a printFile() call.
	 * @param fileName the name of the file.
	 * @throws FileSystemException if the file does not exist or if it's a directory.
	 */
	private void validatePrint(String fileName)
			throws FileSystemException {
		if (!tree.fileExists(fileName)) {
			throw new FileSystemException(
					"The specified file does not exist");
		}
		if (tree.getChild(fileName).type == FileType.DIRECTORY) {
			throw new FileSystemException(
					"Directories cannot be printed");
		}
	}

	/**
	 * Prints the content of the data blocks, pointed to by the direct blocks of the given index node.
	 * @param inodeNumber the number of the index node.
	 * @throws IOException if an i/o error occurs.
	 */
	private void printBlocks(int inodeNumber)
			throws IOException {
		DataBlock buffer = new DataBlock();
		StringAppender result = new StringAppender();
		readIndexNode(currentNode, inodeNumber);
		int[] blocks =
				currentNode.getAllocatedDirectBlocks();
		for (int block : blocks) {
			readDataBlock(buffer, block);
			appendValidChars(
					result,
					new String(buffer.getBytes()));
			System.out.print(result);
			result = new StringAppender();
		}
		System.out.println();
	}

	/**
	 * Appends the characters whose value is different from the byte value for 0.
	 * @param appender the appender to append the characters to.
	 * @param chars the characters to append.
	 */
	private void appendValidChars(StringAppender appender, String chars) {
		for (int i = 0; i < chars.length(); i++) {
			if (chars.charAt(i) != (char)(byte)0) {
				appender.append(
						String.valueOf(chars.charAt(i)));
			}
		}
	}

	/**
	 * Validates the given file name for a writeToFile() call, deleting the content of the file if it already exists.
	 * @param fileName the name of the file
	 * @throws FileSystemException if the name points to an existing directory.
	 */
	private void validateWrite(String fileName)
			throws FileSystemException {
		if (tree.dirExists(fileName)) {
			throw new FileSystemException(
					"The given name points to a directory");
		}
		if (tree.fileExists(fileName)) {
			deleteFile(fileName);
		}
	}

	/**
	 * Calculates the needed amount of blocks for the specified amount of bytes.
	 * @param amountOfBytes the amount of bytes.
	 * @return the calculated amount of blocks.
	 */
	private int calculateNeededBlocks(int amountOfBytes) {
		return (int) Math.ceil(amountOfBytes / 512f);
	}

	/**
	 * Calculates the needed amount of blocks for the specified amount of bytes.
	 * @param amountOfBytes the amount of bytes.
	 * @return the calculated amount of blocks.
	 */
	private int calculateNeededBlocks(long amountOfBytes) {
		return (int) Math.ceil(amountOfBytes / 512f);
	}

	/**
	 * Writes the given bytes to the file at the given index node number.
	 * @param bytes the bytes to write.
	 * @param inodeNumber the number of the index node.
	 * @param neededBlocksCount the amount of needed blocks to write the bytes to.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file size is reached.
	 */
	private void writeBytesToBlocks(byte[] bytes, int inodeNumber, int neededBlocksCount)
			throws IOException, FileSystemException {
		byte[] buffer;
		int allocatedBlock;
		for (int i = 0; i < neededBlocksCount; i++) {
			int start = i * 512;
			int end = 512;
			if (i == neededBlocksCount - 1) {
				end = bytes.length;
			}
			buffer = ArrayManipulator.subArray(bytes, start, end);
			allocatedBlock = allocateDataBlock();
			if (allocatedBlock != -1) {
				addDirectBlock(inodeNumber, allocatedBlock);
				currentDataBlock = new DataBlock(buffer);
				writeDataBlock(currentDataBlock, allocatedBlock);
			}
		}
	}

	/**
	 * Appends the given bytes to the file at the given index node number.
	 * @param bytes the bytes to append.
	 * @param inodeNumber the number of the index node.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file size is reached.
	 */
	private void appendBytesToBlocks(byte[] bytes, int inodeNumber)
			throws IOException, FileSystemException {
		readIndexNode(currentNode, inodeNumber);
		readDataBlock(
				currentDataBlock,
				currentNode.getLastAllocatedBlock());
		int lastBlockFreeBytes =
				512 - ArrayManipulator.getElementCount(currentDataBlock.getBytes());
		int neededBlocks =
				calculateBlocksAppend(bytes, lastBlockFreeBytes);
		if (lastBlockFreeBytes != 0) {
			currentDataBlock.appendBytes(
					ArrayManipulator.subArray(bytes, 0, lastBlockFreeBytes));
			writeDataBlock(
					currentDataBlock,
					currentNode.getLastAllocatedBlock());
			bytes = ArrayManipulator.subArray(bytes, lastBlockFreeBytes, bytes.length);
		}
		writeBytesToBlocks(bytes, inodeNumber, neededBlocks);
	}

	/**
	 * Calculates the amount of needed blocks for the append operation.
	 * @param bytes the bytes to calculate the needed blocks for.
	 * @param lastBlockFreeBytes the amount of free bytes in the last block of the file where bytes are written.
	 * @return the calculated amount of blocks.
	 */
	private int calculateBlocksAppend(byte[] bytes, int lastBlockFreeBytes) {
		int bytesLeft = bytes.length;
		bytesLeft -= lastBlockFreeBytes;
		return calculateNeededBlocks(bytesLeft);
	}

	/**
	 * Validates the external path and the destination file for an importFile() call.
	 * @param extPath the path to the external file.
	 * @param destFile the name of the file to import the bytes to.
	 * @throws FileSystemException if the external file doesn't exist
	 * or if the destination file already exists or is a directory.
	 */
	private void validateImport(String extPath, String destFile)
			throws FileSystemException {
		if (Files.notExists(Path.of(extPath))) {
			throw new FileSystemException(
					"The external file does not exist");
		}
		if (tree.fileExists(destFile) || tree.dirExists(destFile)) {
			throw new FileSystemException(
					"A file/directory with the same name as the destination file already exists");
		}
	}

	/**
	 * Imports the blocks from the external source file to the destination file.
	 * @param src the path to the source file in the external file system.
	 * @param dest the name of the destination file.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file size is reached.
	 */
	private void importBlocks(String src, String dest)
			throws IOException, FileSystemException {
		try (RandomAccessFile srcFile = new RandomAccessFile(src, "r")) {
			long len = srcFile.length();
			int neededBlocks = calculateNeededBlocks(len);
			IndexNode destNode = new IndexNode();
			readIndexNode(
					destNode,
					tree.getChild(dest).inodeNumber);
			importBlocksFromFile(neededBlocks, srcFile, destNode);
			writeIndexNode(
					destNode,
					tree.getChild(dest).inodeNumber);
		}
	}

	/**
	 * Imports the given amount of blocks from the specified file, and adds the numbers of the newly allocated
	 * data blocks to the direct block list of the index node.
	 * @param neededBlocksCount the amount of blocks needed to store the external file's bytes.
	 * @param file the file to read the blocks from.
	 * @param dest the index node of the file to write the blocks to.
	 * @throws IOException if an i/o error occurs.
	 * @throws FileSystemException if the maximum file size is reached.
	 */
	private void importBlocksFromFile(int neededBlocksCount, RandomAccessFile file, IndexNode dest)
			throws IOException, FileSystemException {
		byte[] buffer = new byte[512];
		for (int i = 0; i < neededBlocksCount; i++) {
			if (i == neededBlocksCount - 1) {
				buffer = new byte[512];
				file.read(buffer, 0, (int)(file.length() - (i * 512)));
			} else {
				file.read(buffer, 0, 512);
			}
			int allocatedBlock = allocateDataBlock();
			if (allocatedBlock != -1) {
				dest.addDirectBlock(allocatedBlock);
				DataBlock newBlock = new DataBlock(buffer);
				writeDataBlock(newBlock, allocatedBlock);
			}
		}
	}

	/**
	 * Validates the file name and the external path for an exportFile() call.
	 * @param fileName the name of the file to export.
	 * @param extPath the path to the external file to export the bytes to.
	 * @throws FileSystemException if the given file does not exist, or if the external file already exists.
	 */
	private void validateExport(String fileName, String extPath)
			throws FileSystemException {
		if (!tree.fileExists(fileName)) {
			throw new FileSystemException(
					"The specified file does not exist");
		}
		if (Files.exists(Path.of(extPath))) {
			throw new FileSystemException(
					"The external path points to a file that already exists");
		}
	}

	/**
	 * Exports the blocks from the given internal file to the external file, pointed to by the given path.
	 * @param from the file to copy the blocks from.
	 * @param to the path to the external file to copy the blocks to.
	 * @throws IOException if an i/o error occurs.
	 */
	private void exportBlocks(String from, String to)
			throws IOException {
		try (RandomAccessFile ext = new RandomAccessFile(to, "rw")) {
			int inodeNumber = tree.getChild(from).inodeNumber;
			readIndexNode(currentNode, inodeNumber);
			for (int i = 1; i < currentNode.getAllocatedBlockCount(); i++) {
				readDataBlock(currentDataBlock, currentNode.getDirectBlocks()[i]);
				if (i == currentNode.getAllocatedBlockCount() - 1) {
					ext.write(ArrayManipulator.subArray(
							currentDataBlock.getBytes(),
							0,
							ArrayManipulator.getElementCount(currentDataBlock.getBytes())));
				} else {
					ext.write(currentDataBlock.getBytes());
				}
			}
		}
	}
}
