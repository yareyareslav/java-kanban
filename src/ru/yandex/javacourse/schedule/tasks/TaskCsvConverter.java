package ru.yandex.javacourse.schedule.tasks;

import java.util.Optional;

public class TaskCsvConverter {
    private static final String HEADING = "id,type,name,status,description,epic";

    public static String getHeading() {
        return HEADING;
    }

    public static String formatFileLine(Task task) {
        int id = task.getId();
        String name = task.getName();
        TaskStatus status = task.getStatus();
        String description = task.getDescription();
        TaskType type = TaskType.TASK;

        return String.format("%d,%s,%s,%s,%s,\r\n", id, type, name, status, description);
    }

    public static String formatFileLine(Subtask subtask) {
        int id = subtask.getId();
        String name = subtask.getName();
        TaskStatus status = subtask.getStatus();
        String description = subtask.getDescription();
        TaskType type = TaskType.SUBTASK;
        String epicId = Integer.toString(subtask.getEpicId());

        return String.format("%d,%s,%s,%s,%s,%s\r\n", id, type, name, status, description, epicId);
    }

    public static String formatFileLine(Epic epic) {
        int id = epic.getId();
        String name = epic.getName();
        TaskStatus status = epic.getStatus();
        String description = epic.getDescription();

        TaskType type = TaskType.EPIC;

        return String.format("%d,%s,%s,%s,%s,\r\n", id, type, name, status, description);
    }

    public static Optional<Task> fromFileLineToTask(String line) {
        if (line.contains(HEADING)) {
            return Optional.empty();
        }

        String[] lineSplit = line.split(",");

        if (lineSplit.length < 5) {
            return Optional.empty();
        }

        int id = Integer.parseInt(lineSplit[0]);
        TaskType type = TaskType.valueOf(lineSplit[1]);
        String name = lineSplit[2];
        TaskStatus status = TaskStatus.valueOf(lineSplit[3]);
        String description = lineSplit[4];

        switch (type) {
            case TaskType.TASK:
                return Optional.of(new Task(id, name, description, status));
            case TaskType.SUBTASK:
                int epicId = Integer.parseInt(lineSplit[5]);
                return Optional.of(new Subtask(id, name, description, status, epicId));
            case TaskType.EPIC:
                return Optional.of(new Epic(id, name, description));
            default:
                return Optional.empty();
        }
    }


}
