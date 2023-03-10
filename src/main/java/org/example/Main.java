package org.example;

import com.taskadapter.redmineapi.Params;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws RedmineException {
        //final Integer queryId = null;

        Params params = new Params()
            .add("status_id", "*");

        final RedmineManager mgr = RedmineManagerFactory.createWithApiKey("http://localhost:8080/",
            "9af5b8f742ef1fa0427d2d9b764a8d3c870ec11a");
        mgr.setObjectsPerPage(100);

        final List<Project> projects = mgr.getProjectManager().getProjects();

        System.out.println("Select the project whose tasks you want to see");
        for (final Project project : projects) {
            System.out.println(project.getId() + ") " + project.getIdentifier());
        }
        final Scanner scanner = new Scanner(System.in);

        params.add("project_key",
            projects.get(scanner.nextInt() - 1)
                .getIdentifier());
        //params.add("query_id", String.valueOf(queryId));

        //final List<Issue> issues = mgr.getIssueManager().getIssues(projectKey, queryId);
        final ResultsWrapper<Issue> issues = mgr.getIssueManager()
            .getIssues(params);

        final String message = """
            Choose which tasks you want to see
            1) All tasks
            2) High priority
            3) Reject status
            4) In process
            5) Closed
            6) Progress > 50%
            7) Without subtasks
            10) Repeat this message
            0) Exit
            """;

        System.out.println(message);

        int users_choice;
        do {
            System.out.println("Choose which tasks you want to see");
            users_choice = scanner.nextInt();
            switch (users_choice) {
                case 1 -> outAllTasks(issues.getResults());
                case 2 -> outHighPriorityTasks(issues.getResults());
                case 3 -> outRejectTasks(issues.getResults());
                case 4 -> outInProcessTasks(issues.getResults());
                case 5 -> outClosedTasks(issues.getResults());
                case 6 -> outHalfCompletedTasks(issues.getResults());
                case 7 -> outWithoutSubTasks(issues.getResults());
                case 10 -> System.out.println(message);
                case 0 -> System.out.println("Good Bye");
                default -> System.out.println("Wrong number");
            }
        } while (users_choice != 0);
    }

    public static void outAllTasks(@NotNull List<Issue> issues) {
        System.out.println("All tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(issue -> {
                if (issue.getParentId() == null) {
                    System.out.println(issue);
                } else {
                    System.out.println("\t->" + issue);
                }
            });
    }

    public static void outHighPriorityTasks(@NotNull List<Issue> issues) {
        System.out.println("High priority tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getPriorityText().equals("high"))
            .forEach(System.out::println);
    }

    public static void outRejectTasks(@NotNull List<Issue> issues) {
        System.out.println("Reject tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getStatusName().equals("reject"))
            .forEach(System.out::println);
    }

    public static void outInProcessTasks(@NotNull List<Issue> issues) {
        System.out.println("In process tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getStatusName().equals("In process"))
            .forEach(System.out::println);
    }

    public static void outClosedTasks(@NotNull List<Issue> issues) {
        System.out.println("Closed tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getStatusName().equals("Closed"))
            .forEach(System.out::println);
    }

    public static void outHalfCompletedTasks(@NotNull List<Issue> issues) {
        System.out.println("Half completed tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getDoneRatio() > 50)
            .forEach(System.out::println);
    }

    public static void outWithoutSubTasks(@NotNull List<Issue> issues) {
        System.out.println("Without subtasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getParentId() == null)
            .forEach(System.out::println);
    }
}