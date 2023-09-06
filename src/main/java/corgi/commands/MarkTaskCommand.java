package corgi.commands;

import corgi.storage.Storage;
import corgi.tasks.Task;
import corgi.tasks.TaskList;
import corgi.tasks.TaskListIndexOutOfBoundsException;
import corgi.tasks.TaskStatusException;
import corgi.ui.TextRenderer;

/**
 * Represents a command to mark a task as done or undone in the task list.
 * This command updates the status of a task at the specified index in the task list.
 */
public class MarkTaskCommand extends Command {
    /**
     * The index of the task to be marked.
     */
    private int index;

    /**
     * The new status of the task (true for done, false for undone).
     */
    private boolean status;

    /**
     * Initializes a new MarkTaskCommand instance with the specified index, status, and command type.
     *
     * @param index The index of the task to be marked.
     * @param status The new status of the task (true for done, false for undone).
     * @param type The type of command (CommandType.MARK_DONE or CommandType.MARK_UNDONE).
     */
    public MarkTaskCommand(int index, boolean status, CommandType type) {
        super(false, type);
        this.index = index;
        this.status = status;
    }

    /**
     * Executes the command by marking the task at the specified index with the new status,
     * saving the updated list to storage, and return a message indicating the task's status change.
     *
     * @param list The task list to be updated.
     * @param renderer The text renderer to return formatted message.
     * @param storage The storage for saving and loading tasks (if applicable).
     * @throws CommandExecutionException If an error occurs during command execution.
     */
    @Override
    public String execute(TaskList list, TextRenderer renderer, Storage<Task> storage)
            throws CommandExecutionException {
        try {
            list.mark(this.index, this.status);
            storage.save(list);
            if (status) {
                return renderer.showTaskDone(list.getTaskInfo(this.index));
            } else {
                return renderer.showTaskUndone(list.getTaskInfo(this.index));
            }
        } catch (TaskListIndexOutOfBoundsException e) {
            throw new CommandExecutionException("Invalid index provided!");
        } catch (TaskStatusException e) {
            throw new CommandExecutionException("The task is already in that status!");
        }
    }
}
