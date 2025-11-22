package ru.yandex.javacourse.schedule.custom;

public class Node<K, V> {
    K key;
    V value;

    Node<K, V> prev;
    Node<K, V> next;

    public Node(K key, V value) {
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

    public Node<K, V> getPrev() {
        return prev;
    }

    public Node<K, V> getNext() {
        return next;
    }

    public void setPrev(Node<K, V> prev) {
        this.prev = prev;
    }

    public void setNext(Node<K, V> next) {
        this.next = next;
    }
}
