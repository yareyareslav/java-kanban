package ru.yandex.javacourse.schedule.manager;

import java.util.*;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private HistoryManagerNode<Task> head;
	private HistoryManagerNode<Task> tail;
	private final Map<Integer, HistoryManagerNode<Task>> history = new HashMap<>();

	@Override
	public void remove(int id) {
		HistoryManagerNode<Task> node = history.get(id);
		if (node == null) return;
		if (history.containsKey(id)) {
			HistoryManagerNode<Task> prev = node.getPrev();
			HistoryManagerNode<Task> next = node.getNext();
			if (prev != null) {
				prev.setNext(next);
			} else {
				head = next;
			}
			if (next != null) {
				next.setPrev(prev);
			} else {
				tail = prev;
			}

			history.remove(id, node);
		}
	}

	@Override
	public void add(Task task) {
		int taskId = task.getId();
		HistoryManagerNode<Task> newEntry = new HistoryManagerNode<>(task);
		remove(taskId);

		history.put(taskId, newEntry);

		if (tail == null) {
			head = tail = newEntry;
		} else {
			tail.setNext(newEntry);
			newEntry.setPrev(tail);
			tail = newEntry;
		}
	}

	@Override
	public ArrayList<Task> getTasks() {
		ArrayList<Task> result = new ArrayList<>();
		if (head == null) return result;
		HistoryManagerNode<Task> currentNode = head;
		while (currentNode != null) {
			result.add(currentNode.getValue());
			currentNode = currentNode.getNext();
		}
		return result;
	}
}
