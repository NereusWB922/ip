package commands;

/**
 * Exception thrown when an invalid command is provided.
 */
public class InvalidCommandException extends Exception{

    /**
     * Constructs a new InvalidCommandException with a default error message.
     */
    public InvalidCommandException() {
        super("Invalid command is provided!");
    }
}
