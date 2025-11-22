package manager;

import model.Task;

import java.util.*;


public class InMemoryHistoryManager implements HistoryManager {


    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;


    // Узел двусвязного списка, хранящий задачу.

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
      Добавляет задачу в конец истории.
      Если такая задача уже есть — удаляет старую запись.
     */

    @Override
    public void add(Task task) {
        if (task == null) return;

        // Если задача уже есть в истории, удаляем старый узел
        if (nodeMap.containsKey(task.getId())) {
            removeNode(nodeMap.get(task.getId()));
        }

        // Добавляем новую задачу в конец списка
        Node newNode = linkLast(task);

        // Сохраняем узел в HashMap по id задачи
        nodeMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node == null) return;

        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) prev.next = next;
        else head = next;

        if (next != null) next.prev = prev;
        else tail = prev;
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }


    //Добавляет задачу в конец двусвязного списка.

    private Node linkLast(Task task) {
        final Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        return newNode;
    }

    //Удаляет узел из двусвязного списка.

    private void removeNode(Node node) {
        if (node == null) return;

        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
    }
}