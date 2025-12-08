package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.FileBackedTaskManager;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Введите путь к файлу, в который будут сохраняться задачи: ");
		String enteredPath = scanner.nextLine();
		Path path = Paths.get(enteredPath);

		if (!path.toFile().isFile()) {
			try {
				Files.createFile(path);
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}

		FileBackedTaskManager fileManager = Managers.getFileBacked(path.toFile());

		System.out.println("Хотите загрузить задачи из имеющегося файла? (Yes / No) ");
		String loadAnswer = scanner.nextLine();
		if (loadAnswer.equalsIgnoreCase("yes")) {
			System.out.println("Введите путь к файлу, из которого будут загружены задачи: ");
			String enteredStorePath = scanner.nextLine();
			Path storePath = Paths.get(enteredStorePath);
			while (!storePath.toFile().isFile()) {
				System.out.println("Некорректный путь к файлу. Введите Exit, если передумали загружать сохраненные задачи из файла");
				System.out.println("Введите путь к файлу, из которого будут загружены задачи: ");
				enteredStorePath = scanner.nextLine();

				if (enteredStorePath.equalsIgnoreCase("exit")) {
					storePath = null;
					break;
				}

				storePath = Paths.get(enteredStorePath);
			}

			if (storePath != null) {
				fileManager.loadFromFile(storePath.toFile());
			}
		}

	}

	private static void checkHistory() {
		System.out.println("--- EXTRA TASK ---");
		TaskManager extraTM = Managers.getDefault();

		Task extraTask1 = new Task("Task #1", "Task1 description", NEW);
		Task extraTask2 = new Task("Task #2", "Task2 description", NEW);
		extraTM.addNewTask(extraTask1);
		extraTM.addNewTask(extraTask2);

		Epic extraEpic1 = new Epic("Epic #1", "Epic1 with 3 subtasks");
		Epic extraEpic2 = new Epic("Epic #2", "Epic2 without any subtasks");
		extraTM.addNewEpic(extraEpic1);
		extraTM.addNewEpic(extraEpic2);

		int extraEpic1Id = extraEpic1.getId();
		Subtask extraSubtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, extraEpic1Id);
		Subtask extraSubtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, extraEpic1Id);
		Subtask extraSubtask3 = new Subtask("Subtask #3-1", "Subtask1 description", DONE, extraEpic1Id);
		extraTM.addNewSubtask(extraSubtask1);
		extraTM.addNewSubtask(extraSubtask2);
		extraTM.addNewSubtask(extraSubtask3);

		extraTM.getTask(extraTask1.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.getSubtask(extraSubtask1.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.getTask(extraTask2.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.getTask(extraTask1.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.getSubtask(extraSubtask3.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.getSubtask(extraSubtask1.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.getEpic(extraEpic1.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.deleteTask(extraTask1.getId());

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		extraTM.deleteEpic(extraEpic1Id);

		System.out.println("\nИстория:");
		for (Task task : extraTM.getHistory()) {
			System.out.println(task);
		}

		System.out.println("------------------");
	}

	private static void printAllTasks(TaskManager manager) {
		System.out.println("Задачи:");
		for (Task task : manager.getTasks()) {
			System.out.println(task);
		}
		System.out.println("Эпики:");
		for (Task epic : manager.getEpics()) {
			System.out.println(epic);
//			System.out.println("--> Подзадачи эпика:");
			for (Task task : manager.getEpicSubtasks(epic.getId())) {
				System.out.println("--> " + task);
			}
		}
		System.out.println("Подзадачи:");
		for (Task subtask : manager.getSubtasks()) {
			System.out.println(subtask);
		}

		System.out.println("История:");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}
	}
}
