package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
	protected int id;
	protected String name;
	protected TaskStatus status;
	protected String description;
	protected TaskType type;
	protected Duration duration;
	protected LocalDateTime startTime;

	public Task(String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
		this.name = name;
		this.description = description;
		this.status = status;
		this.duration = duration;
		this.startTime = startTime;
		type = TaskType.TASK;
	}

	public Task(int id, String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
		this(name, description, status, duration, startTime);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskType getType() {
		return type;
	}

	public Optional<LocalDateTime> getStartTime() {
		return Optional.ofNullable(startTime);
	}

	public Optional<Duration> getDuration() {
		return Optional.ofNullable(duration);
	}

	public Optional<LocalDateTime> getEndTime() {
		if (startTime == null || duration == null) {
			return Optional.empty();
		}
		return Optional.of(startTime.plus(duration));
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return id == task.id;
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status='" + status + '\'' +
				", description='" + description + '\'' +
				", startTime='" + startTime + '\'' +
				", duration='" + duration + '\'' +
				", endTime='" + (getEndTime().isPresent() ? getEndTime().get() : null) + '\'' +
				'}';
	}
}
