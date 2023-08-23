package tasks;

/**
 * Task class storing description and status.
 */
public class Task {
    protected String desc;
    protected boolean status;

    /**
     * Initializes a new task with its description. The task's initial status is set to not done.
     *
     * @param desc The description of the task.
     */
    public Task(String desc) {
        this.desc = desc;
        this.status = false;
    }

    /**
     * Mark task as done.
     */
    public void markAsDone() throws TaskStatusException{
        if(this.status == true) {
            throw new TaskStatusException("The task is already marked as done.");
        }
        this.status = true;
    }

    /**
     * Mark task as not done.
     */
    public void markAsNotDone() throws TaskStatusException{
        if(this.status == false) {
            throw new TaskStatusException("The task is already marked as not done.");
        }
        this.status = false;
    }

    /**
     * Returns an icon representing the status of the task.
     * 
     * @return An icon ("X" for done, " " for not done).
     */
    public String getStatusIcon() {
        return (this.status ? "X" : " ");
    }

    /**
     * Returns a string representation of the task.
     *
     * @return Status icon and description of the task.
     */
    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.desc;
    }
}
