package ru.yandex.javacourse.schedule.custom;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NodeTest {

    private Node<String, Integer> node;

    @BeforeEach
    public void setUp() {
        node = new Node<>("key", 123);
    }

    @Test
    public void testGetKey() {
        assertEquals("key", node.getKey());
    }

    @Test
    public void testGetValue() {
        assertEquals(123, node.getValue());
    }

    @Test
    public void testSetKey() {
        node.setKey("newKey");
        assertEquals("newKey", node.getKey());
    }

    @Test
    public void testSetValue() {
        node.setValue(999);
        assertEquals(999, node.getValue());
    }

    @Test
    public void testPrevNextSettersAndGetters() {
        Node<String, Integer> prevNode = new Node<>("prevKey", 1);
        Node<String, Integer> nextNode = new Node<>("nextKey", 2);
        Node<String, Integer> nextNextNode = new Node<>("nextNextKey", 3);
        node.setPrev(prevNode);
        node.setNext(nextNode);
        node.getNext().setNext(nextNextNode);
        assertEquals(prevNode, node.getPrev());
        assertEquals(nextNode, node.getNext());
        assertEquals(nextNextNode, node.getNext().getNext());
    }
}
