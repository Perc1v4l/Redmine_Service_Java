package org.example;

import com.taskadapter.redmineapi.Params;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class IssuesWorker {

    public IssuesWorker(RedmineManager mgr) throws RedmineException {
        console(mgr);
    }

    private void console(RedmineManager mgr) throws RedmineException {
        final List<Project> projects = mgr.getProjectManager().getProjects();

        System.out.println("Выберите проект: ");
        for (final Project project : projects) {
            System.out.println(project.getId() + ") " + project.getIdentifier());
        }
        final Scanner scanner = new Scanner(System.in);
        int project_id = scanner.nextInt();

        final String message = """
            Выберите задание
            1) Вывести все задачи
            2) Вывести задачи с высоким приотритетом
            3) Вывести отклонённые задачи
            4) Вывести задачи в процессе выполнения
            5) Вывести завершённые задачи
            6) Вывести задачи выполненные на 50 и более процентов
            7) Вывести задачи, не учитывая подзадачи
            10) Повторить собщение
            0) Выход
            """;

        System.out.println(message);

        int userChoice;
        do {
            System.out.println("Выберите задание: ");
            userChoice = scanner.nextInt();
            switch (userChoice) {
                case 1 -> outAllTasksNew(mgr, project_id);
                case 2 -> outHighPriorityTasksNew(mgr, project_id);
                case 3 -> outRejectTasksNew(mgr, project_id);
                case 4 -> outInProcessTasksNew(mgr, project_id);
                case 5 -> outClosedTasksNew(mgr, project_id);
                case 6 -> outHalfCompletedTasks(mgr, project_id);
                case 7 -> outWithoutSubTasks(mgr, project_id);
                case 10 -> System.out.println(message);
                case 0 -> System.out.println("До свидания");
                default -> System.out.println("Невереный ввод");
            }
        } while (userChoice != 0);
    }

    @Deprecated
    private static void outHalfCompletedTasksD(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Half completed tasks");

        Params params = new Params()
            .add("status_id", "*")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getDoneRatio() >= 50)
            .forEach(System.out::println);
    }
    private static void outHalfCompletedTasks(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Задачи выполненные на 50 и более процентов");

        Params params = new Params()
            .add("status_id", "*")
            .add("project_id", Integer.toString(project_id))
            .add("from", "50")
            .add("to", "100");

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    private static void outWithoutSubTasks(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Задачи, не учитывая подзадачи");

        Params params = new Params()
            .add("status_id", "*")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getParentId() == null)
            .forEach(System.out::println);
    }

    private static void outAllTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Все задачи");

        Params params = new Params()
            .add("status_id", "*")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(issue -> {
                if (issue.getParentId() == null) {
                    System.out.println(issue);
                } else {
                    System.out.println("\t->" + issue);
                }
            });
    }

    private static void outHighPriorityTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Задачи с высоким приоритетом");

        Params params = new Params()
            .add("status_id", "*")
            .add("priority_id", "1")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    private static void outRejectTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Отклонённые задачи");

        Params params = new Params()
            .add("status_id", "4")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    private static void outInProcessTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Задачи в процессе выполнения");

        Params params = new Params()
            .add("status_id", "2")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    private static void outClosedTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Завершённые задачи");

        Params params = new Params()
            .add("status_id", "3")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }
}
