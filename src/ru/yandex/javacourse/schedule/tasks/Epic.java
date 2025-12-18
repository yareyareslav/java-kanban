package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
	protected ArrayList<Integer> subtaskIds = new ArrayList<>();
	protected LocalDateTime endTime;

	public Epic(int id, String name, String description) {
		super(id, name, description, NEW);
		this.type = TaskType.EPIC;
		this.startTime = null;
		this.duration = null;
	}

	public Epic(String name, String description) {
		super(name, description, NEW);
		this.startTime = null;
		this.duration = null;
	}

	public void addSubtaskId(int id) {
		if (this.getId() == id) return;
		if (subtaskIds.contains(id)) return;
		subtaskIds.add(id);
	}

	public List<Integer> getSubtaskIds() {
		return subtaskIds;
	}

	public void cleanSubtaskIds() {
		subtaskIds.clear();
	}

	public void removeSubtask(int id) {
		subtaskIds.remove(Integer.valueOf(id));
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public Optional<LocalDateTime> getEndTime() {
		return endTime != null ? Optional.of(endTime) : Optional.empty();
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIds +
				", duration='" + duration + '\'' +
				", endTime='" + (getEndTime().isPresent() ? getEndTime().get() : null) + '\'' +
				'}';
	}
}
