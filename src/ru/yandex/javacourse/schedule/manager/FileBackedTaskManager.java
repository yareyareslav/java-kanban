package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;

import java.io.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    private void save() {
        try (Writer fileWriter = new FileWriter(file)) {
            if (file.length() == 0) {
                fileWriter.write("id,type,name,status,description,epic\r\n");
            }
            for (Task task : this.getTasks()) {
                String line = TaskCsvConverter.formatFileLine(task);
                fileWriter.write(line);
            }

            for (Epic epic : super.getEpics()) {
                String line = TaskCsvConverter.formatFileLine(epic);
                fileWriter.write(line);
            }

            for (Subtask subtask : super.getSubtasks()) {
                String line = TaskCsvConverter.formatFileLine(subtask);
                fileWriter.write(line);
            }

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadFromFile(File file) {
        try (Reader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains("id,type,name,status,description,epic")) {
                    continue;
                }

                String[] lineSplit = line.split(",");

                if (lineSplit.length < 5) {
                    break;
                }

                int id = Integer.parseInt(lineSplit[0]);
                String type = lineSplit[1];
                String name = lineSplit[2];
                TaskStatus status = TaskStatus.valueOf(lineSplit[3]);
                String description = lineSplit[4];

                switch (type) {
                    case "TASK":
                        Task task = new Task(id, name, description, status);
                        this.addNewTask(task);
                        break;
                    case "SUBTASK":
                        int epicId = Integer.parseInt(lineSplit[5]);
                        Subtask subtask = new Subtask(id, name, description, status, epicId);
                        this.addNewSubtask(subtask);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(id, name, description);
                        this.addNewEpic(epic);
                        break;
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
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


            ManagerPrinter.printAllTasks(manager);

            FileBackedTaskManager restored = Managers.getFileBacked(storage);
            restored.loadFromFile(storage);

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
