package com.yoanpetrov.filesystemsimulator.exceptions;

/**
 * This exception is thrown whenever an error occurs while a FileSystem object is functioning.
 */
public class FileSystemException extends Exception {

	public FileSystemException(String errorMessage) {
		super(errorMessage);
	}
}
