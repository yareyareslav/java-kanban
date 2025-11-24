package ru.yandex.javacourse.schedule.manager;

public class HistoryManagerNode<V> {
    V value;

    HistoryManagerNode<V> prev;
    HistoryManagerNode<V> next;

    public HistoryManagerNode(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public HistoryManagerNode<V> getPrev() {
        return prev;
    }

    public HistoryManagerNode<V> getNext() {
        return next;
    }

    public void setPrev(HistoryManagerNode<V> prev) {
        this.prev = prev;
    }

    public void setNext(HistoryManagerNode<V> next) {
        this.next = next;
    }
}
