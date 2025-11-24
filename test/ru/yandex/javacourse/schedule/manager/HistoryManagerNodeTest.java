package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistoryManagerNodeTest {

    private HistoryManagerNode<String, Integer> node;

    @BeforeEach
    public void setUp() {
        node = new HistoryManagerNode<>("key", 123);
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
        HistoryManagerNode<String, Integer> prevNode = new HistoryManagerNode<>("prevKey", 1);
        HistoryManagerNode<String, Integer> nextNode = new HistoryManagerNode<>("nextKey", 2);
        HistoryManagerNode<String, Integer> nextNextNode = new HistoryManagerNode<>("nextNextKey", 3);
        node.setPrev(prevNode);
        node.setNext(nextNode);
        node.getNext().setNext(nextNextNode);
        assertEquals(prevNode, node.getPrev());
        assertEquals(nextNode, node.getNext());
        assertEquals(nextNextNode, node.getNext().getNext());
    }
}
