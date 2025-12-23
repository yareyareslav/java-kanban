package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacourse.schedule.tasks.*;

import java.io.*;
import java.util.Optional;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    private void save() {
        try (Writer fileWriter = new FileWriter(file)) {
            if (file.length() == 0) {
                fileWriter.write(TaskCsvConverter.getHeading() + "\r\n");
            }
            for (Task task : this.getTasks()) {
                fileWriter.write(TaskCsvConverter.taskToString(task));
            }

            for (Epic epic : super.getEpics()) {
                fileWriter.write(TaskCsvConverter.taskToString(epic));
            }

            for (Subtask subtask : super.getSubtasks()) {
                fileWriter.write(TaskCsvConverter.taskToString(subtask));
            }

        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (Reader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
            while (br.ready()) {
                String line = br.readLine();
                Optional<Task> convertedTask = TaskCsvConverter.stringToTask(line);

                if (convertedTask.isEmpty()) {
                    continue;
                }

                Task task = convertedTask.get();

                switch (task.getType()) {
                    case TaskType.TASK -> manager.tasks.put(task.getId(), task);
                    case TaskType.SUBTASK -> {
                        final int epicId = ((Subtask) task).getEpicId();
                        Epic epic = manager.epics.get(epicId);
                        if (epic == null) {
                            return null;
                        }
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        epic.addSubtaskId(task.getId());
                        manager.updateEpicStatus(epicId);
                        manager.updateEpicTime((Subtask) task);
                    }
                    case TaskType.EPIC -> manager.epics.put(task.getId(), (Epic) task);
                }
            }
        } catch (IOException exception) {
            throw new ManagerSaveException(exception.getMessage());
        }
        return manager;
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

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
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


            ManagerPrinter.printAllTasks(manager);

            FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(storage);

            boolean sameTasks = restored.getTasks().size() == manager.getTasks().size();
            boolean sameEpics = restored.getEpics().size() == manager.getEpics().size();
            boolean sameSubtasks = restored.getSubtasks().size() == manager.getSubtasks().size();

            System.out.println("---------");
            ManagerPrinter.printAllTasks(restored);

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
