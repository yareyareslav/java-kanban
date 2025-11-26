package ru.yandex.javacourse.schedule.manager;

public class Node<V> {
    private final V value;

    private Node<V> prev;
    private Node<V> next;

    public Node(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public Node<V> getPrev() {
        return prev;
    }

    public Node<V> getNext() {
        return next;
    }

    public void setPrev(Node<V> prev) {
        this.prev = prev;
    }

    public void setNext(Node<V> next) {
        this.next = next;
    }
}
