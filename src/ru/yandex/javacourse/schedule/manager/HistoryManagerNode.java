package ru.yandex.javacourse.schedule.manager;

public class HistoryManagerNode<K, V> {
    K key;
    V value;

    HistoryManagerNode<K, V> prev;
    HistoryManagerNode<K, V> next;

    public HistoryManagerNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public HistoryManagerNode<K, V> getPrev() {
        return prev;
    }

    public HistoryManagerNode<K, V> getNext() {
        return next;
    }

    public void setPrev(HistoryManagerNode<K, V> prev) {
        this.prev = prev;
    }

    public void setNext(HistoryManagerNode<K, V> next) {
        this.next = next;
    }
}
