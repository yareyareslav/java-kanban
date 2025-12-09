package ru.yandex.javacourse.schedule.tasks;

public class TaskCsvConverter {
    public static String formatFileLine(Task task) {
        int id = task.getId();
        String name = task.getName();
        TaskStatus status = task.getStatus();
        String description = task.getDescription();
        String type = "TASK";

        return String.format("%d,%s,%s,%s,%s,\r\n", id, type, name, status, description);
    }

    public static String formatFileLine(Subtask subtask) {
        int id = subtask.getId();
        String name = subtask.getName();
        TaskStatus status = subtask.getStatus();
        String description = subtask.getDescription();
        String type = "SUBTASK";
        String epicId = Integer.toString(subtask.getEpicId());

        return String.format("%d,%s,%s,%s,%s,%s\r\n", id, type, name, status, description, epicId);
    }

    public static String formatFileLine(Epic epic) {
        int id = epic.getId();
        String name = epic.getName();
        TaskStatus status = epic.getStatus();
        String description = epic.getDescription();

        String type = "EPIC";

        return String.format("%d,%s,%s,%s,%s,\r\n", id, type, name, status, description);
    }


}
