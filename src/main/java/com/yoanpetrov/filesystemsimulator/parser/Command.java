package com.yoanpetrov.filesystemsimulator.parser;

import com.yoanpetrov.filesystemsimulator.utils.StringManipulator;

/**
 * Provides utility methods connected to the commands that can be passed to OptionParser.
 */
public final class Command {

	private Command() {}

	/**
	 * List of all existing commands.
	 */
	private static final String[] COMMANDS = {
			"mkdir",
			"rmdir",
			"ls",
			"cd",
			"cp",
			"rm",
			"cat",
			"write",
			"import",
			"export"
	};

	/**
	 * List of the usages of all commands.
	 */
	private static final String[] COMMAND_USAGES = {
			"mkdir <dir_name>",
			"rmdir",
			"ls",
			"cd <name> or cd <name1/name2/...> or cd .. for parent dir or cd / for root dir",
			"cp <source_name> <dest_name>",
			"rm <file_name>",
			"cat <file_name>",
			"write <file_name> \"<content>\" or write +append <file_name> \"<content>\"",
			"import <ext_path> <file_name> or import +append <ext_path> <file_name> \"<content>\"",
			"export <file_name> <ext_path>"
	};

	/**
	 * Takes the given command name and returns its corresponding usage string.
	 *
	 * @param commandName the name of the command.
	 * @return the usage of the command.
	 */
	public static String usageOf(String commandName) {
		commandName = StringManipulator.toLowerCase(commandName);
		switch (commandName) {
			case "mkdir" -> {
				return COMMAND_USAGES[0];
			}
			case "rmdir" -> {
				return COMMAND_USAGES[1];
			}
			case "ls" -> {
				return COMMAND_USAGES[2];
			}
			case "cd" -> {
				return COMMAND_USAGES[3];
			}
			case "cp" -> {
				return COMMAND_USAGES[4];
			}
			case "rm" -> {
				return COMMAND_USAGES[5];
			}
			case "cat" -> {
				return COMMAND_USAGES[6];
			}
			case "write" -> {
				return COMMAND_USAGES[7];
			}
			case "import" -> {
				return COMMAND_USAGES[8];
			}
			case "export" -> {
				return COMMAND_USAGES[9];
			}
			default -> {
				return null;
			}
		}
	}

	/**
	 * Checks whether the given command name exists in the commands list.
	 *
	 * @param commandName the name of the command.
	 * @return true if the command exists, false otherwise.
	 */
	public static boolean exists(String commandName) {
		commandName = StringManipulator.toLowerCase(commandName);
		for (String command : COMMANDS) {
			if (commandName.equals(command)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prints the usages of all commands.
	 */
	public static void printCommandUsages() {
		System.out.println("Commands and their usages:");
		for (String usage : COMMAND_USAGES) {
			System.out.println(usage);
		}
	}
}
