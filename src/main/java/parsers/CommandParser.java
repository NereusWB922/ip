package parsers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

import commands.AddTaskCommand;
import commands.Command;
import commands.CommandType;
import commands.DeleteTaskCommand;
import commands.ExitCommand;
import commands.FindTasksOnDateCommand;
import commands.InvalidCommandException;
import commands.ListTasksCommand;
import commands.MarkTaskCommand;
import tasks.Deadline;
import tasks.Event;
import tasks.Task;
import tasks.ToDo;

/**
 * A parser class for interpreting user input and generating corresponding Command objects.
 */
public class CommandParser extends Parser<Command>{

    /**
     * Parses the given full command string and generates the corresponding Command object.
     * 
     * @param fullCommand The full user input command string.
     * @return The Command object representing the parsed command.
     * @throws InvalidCommandFormatException If the command format is invalid.
     * @throws InvalidCommandTypeException  If the command type is invalid.
     */
    @Override
    public Command parse(String fullCommand) throws InvalidCommandFormatException, InvalidCommandTypeException {
        String[] inputs = fullCommand.split(" ", 2);
        String cmdStr = inputs[0];

        CommandType cmd;

        try {
            cmd = CommandType.getCommandType(cmdStr);
        } catch (InvalidCommandException e) {
            throw new InvalidCommandTypeException("Invalid Command!");
        }

        Command command = null;

        switch (cmd) {
        case BYE:
            command = newExitCommand(inputs);
            break;
        case LIST:
            command = newListCommand(inputs);
            break;
        case MARK:
            command = newMarkCommand(inputs, true);
            break;
        case UNMARK:
            command = newMarkCommand(inputs, false);
            break;
        case TODO:
            command = newAddCommand(inputs, cmd);
            break;
        case DEADLINE:
            command = newAddCommand(inputs, cmd);
            break;
        case EVENT:
            command = newAddCommand(inputs, cmd);
            break;
        case DELETE:
            command = newDeleteCommand(inputs);
            break;
        case DATE:
            command = newDateCommand(inputs);
            break;
        }

        return command;
    }

    private Command newExitCommand(String[] inputs) throws InvalidCommandFormatException {
        if (inputs.length > 1) {
            throw new InvalidCommandFormatException("No argument is needed!" + "\nFormat: " + 
                    CommandType.BYE.getCommandFormat());
        }
        return new ExitCommand();
    }

    private Command newListCommand(String[] inputs) throws InvalidCommandFormatException {
        if (inputs.length > 1) {
            throw new InvalidCommandFormatException("No argument is needed!" + "\nFormat: " +  
                    CommandType.LIST.getCommandFormat());
        }
        return new ListTasksCommand();
    }

    private Command newMarkCommand(String[] inputs, boolean status) throws InvalidCommandFormatException {
        if (inputs.length == 1) {
            throw new InvalidCommandFormatException("No argument is provided!" + "\nFormat: " +  
                    (status ? CommandType.MARK.getCommandFormat() : CommandType.UNMARK.getCommandFormat()));
        } 

        try {
            int index = Integer.parseInt(inputs[1]) - 1;
            return new MarkTaskCommand(index, status, 
                    status ? CommandType.MARK : CommandType.UNMARK);
        } catch (NumberFormatException e) {
            throw new InvalidCommandFormatException("Please provide a valid task number!" +  "\nFormat: " + 
                    (status ? CommandType.MARK.getCommandFormat() : CommandType.UNMARK.getCommandFormat()));
        }
    }

    private Command newDeleteCommand(String[] inputs) throws InvalidCommandFormatException {
        if (inputs.length == 1) {
            throw new InvalidCommandFormatException("No argument is provided!" + "\nFormat: " +
                    CommandType.DELETE.getCommandFormat());
        } 

        try {
            int index = Integer.parseInt(inputs[1]) - 1;
            return new DeleteTaskCommand(index);
        } catch (NumberFormatException e) {
            throw new InvalidCommandFormatException("Please provide a valid task number!" + "\nFormat: " +
                    CommandType.DELETE.getCommandFormat());
        }
    }

    private Command newDateCommand(String[] inputs) throws InvalidCommandFormatException {
        if (inputs.length == 1) {
            throw new InvalidCommandFormatException("No argument is provided!" + "\nFormat: " +
                    CommandType.DATE.getCommandFormat());
        }

        LocalDate target = null;

        String dateStr = inputs[1];

        try {
            target = LocalDate.parse(dateStr, Task.DATE_INPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidCommandFormatException("Invalid date format!");
        }

        final LocalDate FINAL_TARGET = target;

        Predicate<Task> isOnDate = t -> {
            if (t instanceof Deadline) {
                Deadline d = (Deadline) t;
                return d.isHappeningOnDate(FINAL_TARGET);
            } else if (t instanceof Event) {
                Event e = (Event) t;
                return e.isHappeningOnDate(FINAL_TARGET);
            }
            return false;
        };

        return new FindTasksOnDateCommand(isOnDate, dateStr);
    }

    private Command newAddCommand(String[] inputs, CommandType type) throws InvalidCommandFormatException {
        if (inputs.length == 1) {
            throw new InvalidCommandFormatException("No argument is provided!" + "\nFormat: " +
                    type.getCommandFormat());
        }

        String taskInfo = inputs[1];

        if (type == CommandType.TODO) {
            Task target = new ToDo(taskInfo);

            return new AddTaskCommand(target, CommandType.TODO);

        } else if (type == CommandType.DEADLINE) {
            // todo: check number of /by
            String[] deadlineInfos = taskInfo.split(" /by ");

            if (deadlineInfos.length == 1) {
                throw new InvalidCommandFormatException("Missing deadline!" + "\nFormat: " +
                    type.getCommandFormat());
            }

            String deadlineDesc = deadlineInfos[0];
            LocalDate by = null;

            try {
                by = LocalDate.parse(deadlineInfos[1], Task.DATE_INPUT_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new InvalidCommandFormatException("Invalid date format!" + "\nFormat: " +
                    type.getCommandFormat());
            }

            Task target = new Deadline(deadlineDesc, by);

            return new AddTaskCommand(target, CommandType.DEADLINE);

        } else {
            // todo: check number of /from, /to, check order
            String[] eventInfos = taskInfo.split(" /from ");

            if (eventInfos.length < 2) {
                throw new InvalidCommandFormatException("Missing /from argument." + "\nFormat: " +
                    type.getCommandFormat());
            } else if (eventInfos.length > 2) {
                throw new InvalidCommandFormatException("Only one /from argument is needed." + "\nFormat: " +
                    type.getCommandFormat());
            }

            String eventDesc = eventInfos[0];
            String[] eventDuration = eventInfos[1].split(" /to ");

            if (eventDuration.length < 2) {
                throw new InvalidCommandFormatException("Missing /to argument!" + "\nFormat: " +
                    type.getCommandFormat());
            } else if (eventDuration.length > 2) {
                throw new InvalidCommandFormatException("Only one /to argument is needed." + "\nFormat: " +
                    type.getCommandFormat());
            }

            LocalDate from = null;
            LocalDate to = null;

            try {
                from = LocalDate.parse(eventDuration[0], Task.DATE_INPUT_FORMATTER);
                to = LocalDate.parse(eventDuration[1], Task.DATE_INPUT_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new InvalidCommandFormatException("Invalid date format!" + "\nFormat: " +
                    type.getCommandFormat());
            }

            Task target = new Event(eventDesc, from, to);

            return new AddTaskCommand(target, CommandType.EVENT);
        }
    }
}
