package ru.yandex.javacourse.schedule.manager;

public class ManagerPrinter {
    public static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getTasks().forEach(System.out::println);

        System.out.println("Эпики:");
        manager.getEpics().forEach(epic -> {
            System.out.println(epic);
            System.out.println("--> Подзадачи эпика:");
            manager.getEpicSubtasks(epic.getId())
                    .forEach(task -> System.out.println("-->" + task));
        });

        System.out.println("Подзадачи:");
        manager.getSubtasks().forEach(System.out::println);
    }
}
