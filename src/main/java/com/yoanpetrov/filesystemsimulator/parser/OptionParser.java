package com.yoanpetrov.filesystemsimulator.parser;

import com.yoanpetrov.filesystemsimulator.exceptions.FileSystemException;
import com.yoanpetrov.filesystemsimulator.filestructures.data.FileType;
import com.yoanpetrov.filesystemsimulator.filesystem.FileSystem;
import com.yoanpetrov.filesystemsimulator.utils.StringManipulator;

public class OptionParser {

	private final FileSystem fileSystem;

	public OptionParser(FileSystem system) {
		fileSystem = system;
	}

	/**
	 * Executes the given command by splitting it and passing it to the parse() method.
	 * @param command the command to execute.
	 * @return true if the program should stop, false in all other cases, including when invalid input is passed.
	 */
	public boolean executeCommand(String command) {
		String[] args = StringManipulator.split(command, ' ', '"');
		if (args.length == 0)
			return false;

		try {
			validateCommand(args);
		 } catch (FileSystemException e) {
			printError(e);
			return false;
		}
		return parse(args);
	}

	/**
	 * Validates the command after it has been split into tokens.
	 * @param args the command tokens.
	 * @throws FileSystemException if an invalid command is passed.
	 */
	private void validateCommand(String[] args)
			throws FileSystemException {
		for (int i = 0; i < args.length; i++) {
			args[i] = StringManipulator.removeQuotes(args[i]);
			if ("".equals(args[i])) {
				if ("write".equals(args[0])) {
					if ((args.length == 3 && i == 2) || (args.length == 4 && i == 3))
						continue;
				}
				if ("import".equals(args[0])) {
					if (args.length == 5 && i == 4)
						continue;
				}
				throw new FileSystemException("An empty argument cannot be passed here");
			}
		}
	}

	/**
	 * Parses the given command tokens and executes the command.
	 * If any errors appear, their error message is printed by printError() and printArgsError().
	 * @param args the command tokens.
	 * @return true if the program should stop, false in all other cases, including when an error occurs.
	 */
	private boolean parse(String[] args) {
		String commandName = args[0];
		boolean exit = false;

		switch (commandName) {
			case "help" -> printHelp();
			case "mkdir" -> mkdir(args);
			case "rmdir" -> rmdir(args);
			case "ls" -> ls();
			case "cd" -> cd(args);
			case "cp" -> cp(args);
			case "rm" -> rm(args);
			case "cat" -> cat(args);
			case "write" -> write(args);
			case "import" -> importFile(args);
			case "export" -> exportFile(args);
			case "exit" -> exit = true;
			default -> System.out.println("ERROR - Invalid command");
		}
		return exit;
	}

	/**
	 * Creates a directory in the file system.
	 * @param args the command's arguments
	 */
	private void mkdir(String[] args) {
		if (args.length < 2) {
			printArgsError("mkdir");
			return;
		}
		try {
			fileSystem.makeFile(args[1], FileType.DIRECTORY);
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Removes the current directory if it's empty.
	 * @param args the command's arguments.
	 */
	private void rmdir(String[] args) {
		try {
			fileSystem.removeDir();
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Lists the content of the current directory.
	 */
	private void ls() {
		fileSystem.listCurrentDir();
	}

	/**
	 * Changes the current directory.
	 * @param args the command's arguments.
	 */
	private void cd(String[] args) {
		if (args.length < 2) {
			printArgsError("cd");
			return;
		}
		try {
			fileSystem.changeDir(args[1]);
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Copies a file to another file in the file system.
	 * @param args the command's arguments.
	 */
	private void cp(String[] args) {
		if (args.length < 3) {
			printArgsError("cp");
			return;
		}
		try {
			fileSystem.copyFile(args[1], args[2]);
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Deletes a file from the file system.
	 * @param args the command's arguments.
	 */
	private void rm(String[] args) {
		if (args.length < 2) {
			printArgsError("rm");
			return;
		}
		try {
			fileSystem.deleteFile(args[1]);
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Prints the content of a file on the screen.
	 * @param args the command's arguments.
	 */
	private void cat(String[] args) {
		if (args.length < 2) {
			printArgsError("cat");
			return;
		}
		try {
			fileSystem.printFile(args[1]);
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Writes the given string to a file in the file system. If the file exists, it gets overridden.
	 * The additional +append option can be passed as an argument, which appends the given string to the file's end
	 * instead of overriding its content.
	 *
	 * @param args the command's arguments.
	 */
	private void write(String[] args) {
		if (args.length < 3) {
			printArgsError("write");
			return;
		}
		try {
			if ("+append".equals(args[1])) {
				if (args.length < 4) {
					printArgsError("write");
					return;
				}
				fileSystem.appendToFile(args[2], args[3].getBytes());
			} else {
				fileSystem.writeToFile(args[1], args[2].getBytes());
			}
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Imports a file from the external file system into the internal file system.
	 * The additional +append option can be passed as an argument along with a string,
	 * which appends the given string to the imported file's end.
	 * @param args the command's arguments.
	 */
	private void importFile(String[] args) {
		if (args.length < 3) {
			printArgsError("import");
			return;
		}
		try {
			if ("+append".equals(args[1])) {
				if (args.length < 5) {
					printArgsError("import");
					return;
				}
				fileSystem.importFile(args[2], args[3]);
				fileSystem.appendToFile(args[3], args[4].getBytes());
			} else {
				fileSystem.importFile(args[1], args[2]);
			}
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Exports a file from the internal file system to the external file system.
	 * @param args the command's arguments.
	 */
	private void exportFile(String[] args) {
		if (args.length < 3) {
			printArgsError("export");
			return;
		}
		try {
			fileSystem.exportFile(args[1], args[2]);
		} catch (FileSystemException e) {
			printError(e);
		}
	}

	/**
	 * Prints the usages of all available commands.
	 */
	private void printHelp() {
		Command.printCommandUsages();
	}

	/**
	 * Prints an error message.
	 * @param e the exception to get the error message from.
	 */
	private static void printError(FileSystemException e) {
		System.out.println("ERROR - " + e.getMessage());
	}

	/**
	 * Prints an error message when not enough arguments have been
	 * passed to a command.
	 * @param commandName the name of the command.
	 */
	private void printArgsError(String commandName) {
		if (Command.exists(commandName))
			System.out.println("ERROR - Not enough arguments provided! "
					+ "Usage: " + Command.usageOf(commandName));
	}
}
