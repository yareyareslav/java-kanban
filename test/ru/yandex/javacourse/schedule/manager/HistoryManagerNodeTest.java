package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistoryManagerNodeTest {

    private HistoryManagerNode<Integer> node;

    @BeforeEach
    public void setUp() {
        node = new HistoryManagerNode<>( 123);
    }

    @Test
    public void testGetValue() {
        assertEquals(123, node.getValue());
    }

    @Test
    public void testSetValue() {
        node.setValue(999);
        assertEquals(999, node.getValue());
    }

    @Test
    public void testPrevNextSettersAndGetters() {
        HistoryManagerNode<Integer> prevNode = new HistoryManagerNode<>(1);
        HistoryManagerNode<Integer> nextNode = new HistoryManagerNode<>(2);
        HistoryManagerNode<Integer> nextNextNode = new HistoryManagerNode<>(3);
        node.setPrev(prevNode);
        node.setNext(nextNode);
        node.getNext().setNext(nextNextNode);
        assertEquals(prevNode, node.getPrev());
        assertEquals(nextNode, node.getNext());
        assertEquals(nextNextNode, node.getNext().getNext());
    }
}
