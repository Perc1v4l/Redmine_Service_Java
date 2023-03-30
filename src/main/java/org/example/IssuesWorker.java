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

        System.out.println("Select the project whose tasks you want to see");
        for (final Project project : projects) {
            System.out.println(project.getId() + ") " + project.getIdentifier());
        }
        final Scanner scanner = new Scanner(System.in);
        int project_id = scanner.nextInt();

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

        int userChoice;
        do {
            System.out.println("Choose which tasks you want to see");
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
                case 0 -> System.out.println("Good Bye");
                default -> System.out.println("Wrong number");
            }
        } while (userChoice != 0);
    }

    @Deprecated
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
    @Deprecated
    public static void outHighPriorityTasks(@NotNull List<Issue> issues) {
        System.out.println("High priority tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getPriorityText().equals("high"))
            .forEach(System.out::println);
    }
    @Deprecated
    public static void outRejectTasks(@NotNull List<Issue> issues) {
        System.out.println("Reject tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getStatusName().equals("reject"))
            .forEach(System.out::println);
    }
    @Deprecated
    public static void outInProcessTasks(@NotNull List<Issue> issues) {
        System.out.println("In process tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getStatusName().equals("In process"))
            .forEach(System.out::println);
    }
    @Deprecated
    public static void outClosedTasks(@NotNull List<Issue> issues) {
        System.out.println("Closed tasks");
        issues.stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getStatusName().equals("Closed"))
            .forEach(System.out::println);
    }

    public static void outHalfCompletedTasks(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
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

    public static void outWithoutSubTasks(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Without subtasks");

        Params params = new Params()
            .add("status_id", "*")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .filter(issue -> issue.getParentId() == null)
            .forEach(System.out::println);
    }

    public static void outAllTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("All tasks");

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

    public static void outHighPriorityTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("High priority tasks");

        Params params = new Params()
            .add("status_id", "*")
            .add("priority_id", "1")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    public static void outRejectTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Reject tasks");

        Params params = new Params()
            .add("status_id", "4")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    public static void outInProcessTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("In process tasks");

        Params params = new Params()
            .add("status_id", "2")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }

    public static void outClosedTasksNew(@NotNull RedmineManager mgr, int project_id) throws RedmineException {
        System.out.println("Closed tasks");

        Params params = new Params()
            .add("status_id", "3")
            .add("project_id", Integer.toString(project_id));

        ResultsWrapper<Issue> issues = mgr.getIssueManager().getIssues(params);

        issues.getResults().stream()
            .sorted(Comparator.comparing(Issue::getId))
            .forEach(System.out::println);
    }
}
