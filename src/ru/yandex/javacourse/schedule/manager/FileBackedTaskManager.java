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

    public static void main(String[] args) {
        try {
            File storage = args.length > 0 ? new File(args[0]) : File.createTempFile("kanban-demo", ".csv");
            if (!storage.exists()) {
                storage.createNewFile();
            }

            FileBackedTaskManager manager = Managers.getFileBacked(storage);

            Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
            Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.IN_PROGRESS);
            manager.addNewTask(task1);
            manager.addNewTask(task2);

            Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
            manager.addNewEpic(epic1);
            Subtask sub1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId());
            Subtask sub2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.DONE, epic1.getId());
            manager.addNewSubtask(sub1);
            manager.addNewSubtask(sub2);

            manager.printAllTasks();

            FileBackedTaskManager restored = Managers.getFileBacked(storage);
            restored.loadFromFile(storage);

            boolean sameTasks = restored.getTasks().size() == manager.getTasks().size();
            boolean sameEpics = restored.getEpics().size() == manager.getEpics().size();
            boolean sameSubtasks = restored.getSubtasks().size() == manager.getSubtasks().size();

            System.out.println("---------");
            restored.printAllTasks();

            if (sameTasks && sameEpics && sameSubtasks) {
                System.out.println("Данные успешно восстановлены из файла: " + storage.getAbsolutePath());
            } else {
                System.out.println("Ошибка восстановления данных из файла: " + storage.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
