package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class TaskCsvConverter {
    private static final String HEADING = "id,type,name,status,description,epic,duration,startTime";
    private static final String FORMAT = "%d,%s,%s,%s,%s,%s,%s,%s\r\n";

    public static String getHeading() {
        return HEADING;
    }

    public static String taskToString(Task task) {
        int id = task.getId();
        String name = task.getName();
        TaskStatus status = task.getStatus();
        String description = task.getDescription();
        Duration duration = task.getDuration().orElse(null);
        LocalDateTime start = task.getStartTime().orElse(null);

        return String.format(
                FORMAT,
                id,
                task.getType(),
                name,
                status,
                description,
                null,
                duration,
                start
        );
    }

    public static String taskToString(Subtask subtask) {
        int id = subtask.getId();
        String name = subtask.getName();
        TaskStatus status = subtask.getStatus();
        String description = subtask.getDescription();
        String epicId = Integer.toString(subtask.getEpicId());
        Duration duration = subtask.getDuration().orElse(null);
        LocalDateTime start = subtask.getStartTime().orElse(null);

        return String.format(
                FORMAT,
                id,
                subtask.getType(),
                name,
                status,
                description,
                epicId,
                duration,
                start
        );
    }

    public static Optional<Task> stringToTask(String line) {
        if (line.contains(HEADING)) {
            return Optional.empty();
        }

        String[] lineSplit = line.split(",");

        if (lineSplit.length < HEADING.split(",").length) {
            return Optional.empty();
        }

        int id = Integer.parseInt(lineSplit[0]);
        TaskType type = TaskType.valueOf(lineSplit[1]);
        String name = lineSplit[2];
        TaskStatus status = TaskStatus.valueOf(lineSplit[3]);
        String description = lineSplit[4];
        Duration duration = lineSplit[6].equals("null") ? null : Duration.parse(lineSplit[6]);
        LocalDateTime start = lineSplit[7].equals("null") ? null : LocalDateTime.parse(lineSplit[7]);

        return switch (type) {
            case TaskType.TASK -> Optional.of(new Task(id, name, description, status, duration, start));
            case TaskType.SUBTASK -> {
                int epicId = Integer.parseInt(lineSplit[5]);
                yield Optional.of(new Subtask(id, name, description, status, epicId, duration, start));
            }
            case TaskType.EPIC -> Optional.of(new Epic(id, name, description));
        };
    }


}
