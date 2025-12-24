package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import ru.yandex.javacourse.schedule.exceptions.InvalidTaskCompletionTime;
import ru.yandex.javacourse.schedule.tasks.*;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
	protected final Map<Integer, Epic> epics = new HashMap<>();
	protected final Map<Integer, Subtask> subtasks = new HashMap<>();

	private int generatorId = 1;
	private final HistoryManager historyManager = Managers.getDefaultHistory();

	private final Comparator<Task> prioritizedTasksComparator = Comparator
			.<Task, LocalDateTime>comparing(task -> task.getStartTime().orElseThrow())
			.thenComparing(Task::getId);
	private final TreeSet<Task> prioritizedTasks = new TreeSet<>(prioritizedTasksComparator);

	private int generateId() {
		while (tasks.containsKey(generatorId) || subtasks.containsKey(generatorId) || epics.containsKey(generatorId)) {
			++generatorId;
		}
		return generatorId;
	}

	private void addNewInPrioritizedTasks(Task task) {
		Optional<LocalDateTime> maybeStart = task.getStartTime();
		if (maybeStart.isEmpty()) {
			return;
		}
		prioritizedTasks.add(task);
	}

	private boolean doesIntersectByTime(Task task) {
		if (prioritizedTasks.isEmpty()) {
			return false;
		}
		Optional<LocalDateTime> maybeStart = task.getStartTime();
		Optional<LocalDateTime> maybeEnd = task.getEndTime();
		if (maybeStart.isEmpty() || maybeEnd.isEmpty()) {
			return false;
		}
		LocalDateTime startTime = maybeStart.get();
		LocalDateTime endTime = maybeEnd.get();

        return prioritizedTasks.stream()
                .anyMatch(t -> {
//					Можно без проверки, так как в prioritizedTasks попадают только задачи с временем выполнения
                    LocalDateTime curStart = t.getStartTime().get();
                    LocalDateTime curEnd = t.getEndTime().get();
                    return !(endTime.isBefore(curStart) || startTime.isAfter(curEnd));
                });
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
		epic.getSubtaskIds().forEach(id -> tasks.add(subtasks.get(id)));
		return tasks;
	}

	@Override
	public Optional<Task> getTask(int id) {
		final Task task = tasks.get(id);
		if (task == null) {
			return Optional.empty();
		}
		historyManager.add(task);
		return Optional.of(task);
	}

	@Override
	public Optional<Subtask> getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		if (subtask == null) {
			return Optional.empty();
		}
		historyManager.add(subtask);
		return Optional.of(subtask);
	}

	@Override
	public Optional<Epic> getEpic(int id) {
		final Epic epic = epics.get(id);
		if (epic == null) {
			return Optional.empty();
		}
		historyManager.add(epic);
		return Optional.of(epic);
	}

	@Override
	public int addNewTask(Task task) {
		if (doesIntersectByTime(task)) {
			throw new InvalidTaskCompletionTime("Задача пересекается по времени с другими задачами");
		}
		int id = task.getId();
		final Task savedTask = tasks.get(id);
		if (id == 0 || savedTask != null) {
			id = generateId();
			task.setId(id);
		}
		tasks.put(id, task);
		addNewInPrioritizedTasks(task);
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
		return id;

	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		if (doesIntersectByTime(subtask)) {
			throw new InvalidTaskCompletionTime("Подзадача пересекается по времени с другими задачами");
		}
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
		addNewInPrioritizedTasks(subtask);
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
		historyManager.remove(id);

		epic.getSubtaskIds().forEach(subId -> {
			Subtask subtask = subtasks.remove(subId);
			prioritizedTasks.remove(subtask);
			historyManager.remove(subId);
		});
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
		epics.values().forEach(epic -> {
			epic.cleanSubtaskIds();
			epic.setStartTime(null);
			epic.setDuration(null);
			epic.setEndTime(null);
			updateEpicStatus(epic.getId());
		});
		prioritizedTasks.removeAll(subtasks.values());
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
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
