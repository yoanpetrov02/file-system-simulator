package com.yoanpetrov.filesystemsimulator;

/**
 * Marks an object as testable. A Testable object must have a printDebug() method for testing during development.
 */
public interface Testable {

	/**
	 * Prints useful debugging information about the object.
	 */
	void printDebug();
}
