package tracker;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Переезд", "Собрать вещи и перевезти");
        Task task2 = new Task("Учёба", "Сделать проект по Java");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Организовать праздник", "Свадьба");
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Забронировать ресторан", "Позвонить и договориться", Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Купить цветы", "Выбрать букеты", Status.NEW, epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        Epic epic2 = new Epic("Купить квартиру", "Собрать документы и найти жильё");
        manager.addEpic(epic2);

        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        System.out.println("После завершения одной подзадачи: " + epic1);

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        System.out.println("После завершения всех подзадач: " + epic1);

        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("После удаления:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
    }
}