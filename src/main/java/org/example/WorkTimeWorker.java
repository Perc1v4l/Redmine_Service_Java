package org.example;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.TimeEntryManager;
import com.taskadapter.redmineapi.bean.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WorkTimeWorker {

    Scanner scanner = new Scanner(System.in);

    public WorkTimeWorker(RedmineManager mgr) throws RedmineException {
        console(mgr);
    }

    private void console(RedmineManager mgr) throws RedmineException {
        System.out.println("""
                Выберите задание:
                1) Показать дни, когда суммарная трудоёмкость по задачам была менее 8 часов;
                2) Показать недели, в которых было менее 40 часов;
                3) Показать недели, в которых было менее 40 часов, сравнивая с производственным календарём.
                """);

        int userChoice;
        do {
            System.out.println("Choose which tasks you want to see");
            userChoice = scanner.nextInt();
            switch (userChoice) {
                case 1 -> task1(mgr);
                case 0 -> System.out.println("Good Bye");
            }
        } while(userChoice !=0);
    }

    public void task1(RedmineManager mgr) throws RedmineException {
        TimeEntryManager timeEntryManager = mgr.getTimeEntryManager();
        final Map<String, String> params = new HashMap<>();
        System.out.println("Введите начальную дату");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.dd.yyyy");
        LocalDate startDate = LocalDate.parse(scanner.next(), formatter);

        System.out.println("Введите конечную дату");
        LocalDate endDate = LocalDate.parse(scanner.next(), formatter);

        final List<User> users = mgr.getUserManager().getUsers();

        System.out.println("Select the user whose work time you want to see");
        users.forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));


        params.put("user_id", scanner.next());
        params.put("spent_on", startDate.toString());//todo

        final List<TimeEntry> elements = timeEntryManager.getTimeEntries(params).getResults();
    }
}