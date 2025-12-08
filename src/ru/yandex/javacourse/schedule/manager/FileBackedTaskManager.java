package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    File file;
    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
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
        try (Writer fileWriter = new FileWriter(file)) {
            if (file.length() == 0) {
                fileWriter.write("id,type,name,status,description,epic\r\n");
            }
            for (Task task : super.getTasks()) {
                String line = formatFileLine(task);
                fileWriter.write(line);
            }

            for (Epic epic : super.getEpics()) {
                String line = formatFileLine(epic);
                fileWriter.write(line);
            }

            for (Subtask subtask : super.getSubtasks()) {
                String line = formatFileLine(subtask);
                fileWriter.write(line);
            }

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void loadFromFile(File file) {
        super.loadFromFile(file);
        save();
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

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }
}
