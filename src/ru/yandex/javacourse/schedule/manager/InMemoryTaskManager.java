package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
	protected final Map<Integer, Epic> epics = new HashMap<>();
	protected final Map<Integer, Subtask> subtasks = new HashMap<>();

	private final Comparator<Task> prioritizedTasksComparator = (t1, t2) -> {
        Optional<LocalDateTime> maybeStart1 = t1.getStartTime();
        Optional<LocalDateTime> maybeStart2 = t2.getStartTime();
        if (maybeStart1.isEmpty() && maybeStart2.isEmpty()) {
            return 0;
        }
        if (maybeStart1.isPresent() && maybeStart2.isEmpty()) {
            return 1;
        }
        if (maybeStart1.isEmpty()) {
            return -1;
        }
        LocalDateTime start1 = maybeStart1.get();
        LocalDateTime start2 = maybeStart2.get();

        if (start1.isBefore(start2)) {
            return 1;
        }
        if (start2.isEqual(start2)) {
            return 0;
        }
        return -1;
    };

	private final TreeSet<Task> prioritizedTasks = new TreeSet<>(prioritizedTasksComparator);
	private int generatorId = 1;
	private final HistoryManager historyManager = Managers.getDefaultHistory();

	private int generateId() {
		while (tasks.containsKey(generatorId) || subtasks.containsKey(generatorId) || epics.containsKey(generatorId)) {
			++generatorId;
		}
		return generatorId;
	}

	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		ArrayList<Subtask> tasks = new ArrayList<>();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		for (int id : epic.getSubtaskIds()) {
			tasks.add(subtasks.get(id));
		}
		return tasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		historyManager.add(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		historyManager.add(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		historyManager.add(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
		int id = task.getId();
		final Task savedTask = tasks.get(id);
		if (id == 0 || savedTask != null) {
			id = generateId();
			task.setId(id);
		}
		tasks.put(id, task);
		prioritizedTasks.add(task);
		return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
		int id = epic.getId();
		final Task savedEpic = epics.get(id);
		if (id == 0 || savedEpic != null) {
			id = generateId();
			epic.setId(id);
		}
		epics.put(id, epic);
		prioritizedTasks.add(epic);
		return id;

	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		int id = subtask.getId();
		final Subtask savedSubtask = subtasks.get(id);
		if (id == 0 || savedSubtask != null) {
			id = generateId();
			subtask.setId(id);
		}
		subtasks.put(id, subtask);
		prioritizedTasks.add(subtask);
		epic.addSubtaskId(subtask.getId());
		updateEpicTime(subtask);
		updateEpicStatus(epicId);
		return id;
	}

	@Override
	public void updateTask(Task task) {
		final int id = task.getId();
		final Task savedTask = tasks.get(id);
		if (savedTask == null) {
			return;
		}
		tasks.put(id, task);
		prioritizedTasks.add(task);
	}

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		savedEpic.setName(epic.getName());
		savedEpic.setDescription(epic.getDescription());
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		final int id = subtask.getId();
		final int epicId = subtask.getEpicId();
		final Subtask savedSubtask = subtasks.get(id);
		if (savedSubtask == null) {
			return;
		}
		final Epic epic = epics.get(epicId);
		if (epic == null) {
			return;
		}
		subtasks.put(id, subtask);
		updateEpicTime(subtask);
		updateEpicStatus(epicId);
	}

	@Override
	public void deleteTask(int id) {
		Task task = tasks.remove(id);
		prioritizedTasks.remove(task);
		historyManager.remove(id);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		prioritizedTasks.remove(epic);
		historyManager.remove(id);
		for (Integer subtaskId : epic.getSubtaskIds()) {
			Subtask subtask = subtasks.remove(subtaskId);
			prioritizedTasks.remove(subtask);
			historyManager.remove(subtaskId);
		}
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
		if (subtask == null) {
			return;
		}
		prioritizedTasks.remove(subtask);
		historyManager.remove(id);
		Epic epic = epics.get(subtask.getEpicId());
		epic.removeSubtask(id);
		updateEpicTime(subtask);
		updateEpicStatus(epic.getId());
	}

	@Override
	public void deleteTasks() {
		prioritizedTasks.removeAll(tasks.values());
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Epic epic : epics.values()) {
			epic.cleanSubtaskIds();
			epic.setStartTime(null);
			epic.setDuration(null);
			epic.setEndTime(null);
			updateEpicStatus(epic.getId());
		}
		prioritizedTasks.removeAll(subtasks.values());
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
		prioritizedTasks.removeAll(epics.values());
		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getTasks();
	}

	protected void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}

	protected void updateEpicTime(Subtask subtask) {
		if (subtask.getStartTime().isEmpty() || subtask.getEndTime().isEmpty()) {
			return;
		}

		LocalDateTime subtaskStart = subtask.getStartTime().get();
		LocalDateTime subtaskEnd = subtask.getEndTime().get();

		Epic epic = epics.get(subtask.getEpicId());

		if (epic.getStartTime().isEmpty()) {
			epic.setStartTime(subtaskStart);
		}
		if (epic.getEndTime().isEmpty()) {
			epic.setEndTime(subtaskEnd);
		}

		LocalDateTime epicStart = epic.getStartTime().get();
		LocalDateTime epicEnd = epic.getEndTime().get();

		if (epicStart.isAfter(subtaskStart)) {
			epic.setStartTime(subtaskStart);
		}
		if (epicEnd.isBefore(subtaskEnd)) {
			epic.setEndTime(subtaskEnd);
		}
		epic.setDuration(Duration.between(epic.getStartTime().get(), epic.getEndTime().get()));
	}

	public ArrayList<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

}
