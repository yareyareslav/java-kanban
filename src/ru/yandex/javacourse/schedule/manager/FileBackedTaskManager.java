package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {
    Path filePath;
    public FileBackedTaskManager(Path filePath) {
        super();
        this.filePath = filePath;
    }

    private String formatFileLine(Task task) {
        int id = task.getId();
        String name = task.getName();
        TaskStatus status = task.getStatus();
        String description = task.getDescription();

        String type;
        String epicId = "";
        if (task.getClass() == Subtask.class) {
            type = "SUBTASK";
            epicId = Integer.toString(((Subtask) task).getEpicId());
        } else if (task.getClass() == Epic.class) {
            type = "EPIC";
        } else {
            type = "TASK";
        }

        return String.format("%d,%s,%s,%s,%s,%s\r\n", id, type, name, status, description, epicId);
    }

    private void save() throws RuntimeException {
        try (Writer fileWriter = new FileWriter(filePath.toFile())) {
            for (Task task : this.getTasks()) {
                String line = formatFileLine(task);
                fileWriter.write(line);
            }

            for (Epic epic : this.getEpics()) {
                String line = formatFileLine(epic);
                fileWriter.write(line);
            }

            for (Subtask subtask : this.getSubtasks()) {
                String line = formatFileLine(subtask);
                fileWriter.write(line);
            }

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public int addNewTask(Task task) {
        final int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        final int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

}
