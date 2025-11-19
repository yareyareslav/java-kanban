package ru.yandex.javacourse.schedule.manager;

import java.util.*;

import ru.yandex.javacourse.schedule.custom.Node;
import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private Node<Integer, Task> head;
	private Node<Integer, Task> tail;
	final private Map<Integer, Node<Integer, Task>> history = new HashMap<>();

	private void removeNode(Node<Integer, Task> node) {
		if (history.containsKey(node.getKey())) {
			Node<Integer, Task> prev = node.getPrev();
			Node<Integer, Task> next = node.getNext();
			prev.setNext(next);

			history.remove(node.getKey(), node);
		}
	}

	@Override
	public void remove(int id) {
		Node<Integer, Task> node = history.get(id);
		removeNode(node);
	}

	@Override
	public void add(Task task) {
		Integer taskId = task.getId();
		Node<Integer, Task> node = new Node<>(task.getId(), task);
		if (history.containsKey(taskId)) {
			removeNode(node);
		}

		Node<Integer, Task> newEntry = new Node<>(task.getId(), task);
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
		Node<Integer, Task> currentNode = head;
		while (currentNode != null) {
			result.add(currentNode.getValue());
			currentNode = currentNode.getNext();
		}
		return result;
	}
}
