import manager.FileBackedTaskManager;
import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        //  Создание обычных задач
        Task task1 = new Task(
                "Переезд",
                "Собрать вещи и перевезти",
                Status.NEW,
                Duration.ofMinutes(120),
                LocalDateTime.of(2025, 1, 10, 12, 0)
        );

        Task task2 = new Task(
                "Учёба",
                "Сделать проект по Java",
                Status.NEW,
                Duration.ofMinutes(180),
                LocalDateTime.of(2025, 1, 11, 15, 0)
        );

        manager.addTask(task1);
        manager.addTask(task2);

        // Создание эпика
        Epic epic1 = new Epic(
                "Организовать праздник",
                "Свадьба"
        );
        manager.addEpic(epic1);

        // Создание подзадач
        Subtask sub1 = new Subtask(
                "Забронировать ресторан",
                "Позвонить и договориться",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 12, 10, 0),
                epic1.getId()
        );

        Subtask sub2 = new Subtask(
                "Купить цветы",
                "Выбрать букеты",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 12, 12, 0),
                epic1.getId()
        );

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        //  Выводим всё, что в менеджере
        System.out.println("=== Текущее состояние менеджера ===");
        printAllTasks(manager);

        //  Проверяем загрузку из файла
        System.out.println("\n=== Восстановление из файла ===");
        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(file);
        printAllTasks(restored);
    }

    private static void printAllTasks(FileBackedTaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
            for (Subtask sub : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("  --> " + sub);
            }
        }

        System.out.println("\nПодзадачи:");
        for (Subtask sub : manager.getAllSubtasks()) {
            System.out.println(sub);
        }
    }
}