import manager.TaskManager;
import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import manager.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefaultTaskManager();

        Task task1 = new Task("Переезд", "Собрать вещи и перевезти", Status.NEW);
        Task task2 = new Task("Учёба", "Сделать проект по Java", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Организовать праздник", "Свадьба");
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Забронировать ресторан", "Позвонить и договориться", Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Купить цветы", "Выбрать букеты", Status.NEW, epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(sub1.getId());

        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) System.out.println(task);

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
            for (Subtask sub : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + sub);
            }
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) System.out.println(subtask);

        System.out.println("История:");
        for (Task task : manager.getHistory()) System.out.println(task);
    }
}