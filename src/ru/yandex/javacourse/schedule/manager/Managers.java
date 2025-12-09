package ru.yandex.javacourse.schedule.manager;

import java.io.File;

/**
 * Default managers.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class Managers {
	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}

	public static FileBackedTaskManager getFileBacked(File file) {
		return new FileBackedTaskManager(file);
	}

	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}
}
