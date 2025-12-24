package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.*;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
	public static void main(String[] args) {
		System.out.println("--- EXTRA TASK ---");
		InMemoryTaskManager extraTM = Managers.getDefault();

		Task extraTask1 = new Task("Task #1", "Task1 description", NEW);
		Task extraTask2 = new Task("Task #2", "Task2 description", NEW, Duration.ofMinutes(50), LocalDateTime.of(2017, 12, 30, 12, 50));
		extraTM.addNewTask(extraTask1);
		extraTM.addNewTask(extraTask2);

		Epic extraEpic1 = new Epic("Epic #1", "Epic1 with 3 subtasks");
		Epic extraEpic2 = new Epic("Epic #2", "Epic2 without any subtasks");
		extraTM.addNewEpic(extraEpic1);
		extraTM.addNewEpic(extraEpic2);

		int extraEpic1Id = extraEpic1.getId();
		Subtask extraSubtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, extraEpic1Id, Duration.ofMinutes(70), LocalDateTime.of(2015, 12, 30, 12, 50));
		Subtask extraSubtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, extraEpic1Id, Duration.ofMinutes(70), LocalDateTime.of(2016, 12, 30, 12, 50));
		Subtask extraSubtask3 = new Subtask("Subtask #3-1", "Subtask1 description", DONE, extraEpic1Id, Duration.ofMinutes(70), LocalDateTime.of(2018, 12, 30, 12, 50));
		extraTM.addNewSubtask(extraSubtask1);
		extraTM.addNewSubtask(extraSubtask2);
		extraTM.addNewSubtask(extraSubtask3);

		System.out.println(extraTM.getPrioritizedTasks());
		ManagerPrinter.printAllTasks(extraTM);

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
			System.out.println("--> Подзадачи эпика:");
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
