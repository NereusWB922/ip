package corgi.commands;

import corgi.storage.Storage;
import corgi.tasks.Task;
import corgi.tasks.TaskList;
import corgi.tasks.TaskListIndexOutOfBoundsException;
import corgi.ui.TextRenderer;

/**
 * Represents a command to delete a task from the task list.
 * This command deletes a task at the specified index from the task list.
 */
public class DeleteTaskCommand extends Command {
    /**
     * The index of the task to be deleted.
     */
    private int targetIdx;

    /**
     * Initializes a new DeleteTaskCommand instance with the specified target index.
     *
     * @param targetIdx The index of the task to be deleted.
     */
    public DeleteTaskCommand(int targetIdx) {
        super(false, CommandType.DELETE);
        this.targetIdx = targetIdx;
    }

    /**
     * Executes the command by deleting the task at the specified index from the task list,
     * saving the updated list to storage, and return message indicating
     * that the task has been deleted.
     *
     * @param list The task list from which the task should be deleted.
     * @param renderer The text renderer to return formatted message.
     * @param storage The storage for saving and loading tasks (if applicable).
     * @throws CommandExecutionException If an error occurs during command execution.
     */
    @Override
    public String execute(TaskList list, TextRenderer renderer, Storage<Task> storage)
            throws CommandExecutionException {
        try {
            String targetTaskInfo = list.getTaskInfo(targetIdx);
            list.remove(targetIdx);
            storage.save(list);
            return renderer.showTaskDeleted(targetTaskInfo, list.size());
        } catch (TaskListIndexOutOfBoundsException e) {
            throw new CommandExecutionException("Invalid index provided!");
        }
    }
}
