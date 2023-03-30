package org.example;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.TimeEntryManager;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.User;

import org.javatuples.Pair;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
                case 2 -> task2(mgr);
                case 3 -> task3(mgr);
                case 0 -> System.out.println("Good Bye");
            }
        } while (userChoice != 0);
    }

    public void task1(RedmineManager mgr) throws RedmineException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.dd.yyyy");

        TimeEntryManager timeEntryManager = mgr.getTimeEntryManager();

        final Map<String, String> params = new HashMap<>();

        System.out.println("Введите начальную дату");
        Date startDate = java.sql.Date.valueOf(LocalDate.parse(scanner.next(), formatter));

        System.out.println("Введите конечную дату");
        Date endDate = java.sql.Date.valueOf(LocalDate.parse(scanner.next(), formatter));

        final List<User> users = mgr.getUserManager().getUsers();

        System.out.println("Выберите сотрудника");
        users.forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));

        params.put("user_id", scanner.next());
        params.put("from", startDate.toString());
        params.put("to", endDate.toString());

        List<TimeEntry> elements = timeEntryManager.getTimeEntries(params).getResults();
        elements = elements.stream()
                .sorted(Comparator.comparing(TimeEntry::getSpentOn))
                .toList();

        Map<Date, Float> dateHoursMap = new HashMap<>();

        for (TimeEntry element : elements) {
            if (dateHoursMap.containsKey(element.getSpentOn())) {
                float sumWorkTime = element.getHours() +
                        dateHoursMap.get(element.getSpentOn());
                dateHoursMap.put(element.getSpentOn(), sumWorkTime);
            } else {
                dateHoursMap.put(element.getSpentOn(), element.getHours());
            }
        }

        Set<Map.Entry<Date, Float>> mapset = dateHoursMap.entrySet();

        System.out.println("Рабочие дни и время сотрудника:");
        mapset.forEach(System.out::println);

        System.out.println("Дни, в которые сотрудник работал менее 8 часов");
        for (Date date = startDate; date.equals(endDate); date = DateUtil.addDays(date, 1)) {
            if (dateHoursMap.containsKey(date)) {
                if (dateHoursMap.get(date) < 8) {
                    System.out.println("Дата: " + date + "\tКол-во часов: " + dateHoursMap.get(date));
                }
            } else {
                System.out.println("Дата: " + date + "\tКол-во часов: " + 0);
            }
        }
    }

    public static class DateUtil {
        public static Date addDays(Date date, int days) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, days);
            return cal.getTime();
        }
    }

    public void task2(RedmineManager mgr) throws RedmineException {
        TimeEntryManager timeEntryManager = mgr.getTimeEntryManager();

        final Map<String, String> params = new HashMap<>();

        System.out.print("Введите месяц: ");
        int month = scanner.nextInt();

        System.out.print("Введите год: ");
        int year = scanner.nextInt();

        final List<User> users = mgr.getUserManager().getUsers();
        System.out.println("Выберите сотрудника");
        users.forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        Date startDate = calendar.getTime();

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        Date endDate = calendar.getTime();

        params.put("user_id", scanner.next());
        params.put("from", startDate.toString());
        params.put("to", endDate.toString());

        List<TimeEntry> elements = timeEntryManager.getTimeEntries(params).getResults();
        elements = elements.stream()
                .sorted(Comparator.comparing(TimeEntry::getSpentOn))
                .toList();

        float sumOfHoursInWeek = 0;
        calendar.set(year, month, 1);
        int weekInMonth = calendar.get(Calendar.WEEK_OF_MONTH);

        List<Float> hoursInWeek = new ArrayList<>();

        for (TimeEntry element : elements) {
            calendar.setTime(element.getSpentOn());

            if (calendar.get(Calendar.WEEK_OF_MONTH) == weekInMonth) {
                sumOfHoursInWeek += element.getHours();
            } else {
                hoursInWeek.add(sumOfHoursInWeek);
                sumOfHoursInWeek = element.getHours();
                weekInMonth = calendar.get(Calendar.WEEK_OF_MONTH);
            }
        }

        System.out.println("Рабочие часы сотрудника по неделям");
        hoursInWeek.forEach(el -> System.out.println("Неделя " + hoursInWeek.indexOf(el) + ": " + el));

        System.out.println("Недели, в которых рабочее время сотрудника составило менее 40 часов");
        hoursInWeek.forEach(el ->
        {
            if (el < 40) {
                System.out.println("Неделя " + hoursInWeek.indexOf(el) + ": " + el);
            }
        });
    }

    public void task3(RedmineManager mgr) throws RedmineException {
        float[] weeks = {
                0,
                0, 40, 40, 40,
                40, 40, 40, 23, 40,
                31, 40, 40, 40,
                40, 40, 40, 40,
                32, 24, 40, 40, 40,
                40, 32, 40, 40,
                40, 40, 40, 40, 40,
                40, 40, 40, 40,
                40, 40, 40, 40,
                40, 40, 40, 40, 39,
                32, 40, 40, 40,
                40, 40, 40, 40
        };

        TimeEntryManager timeEntryManager = mgr.getTimeEntryManager();

        final Map<String, String> params = new HashMap<>();

        System.out.print("Введите месяц: ");
        int month = scanner.nextInt();

        System.out.print("Введите год: ");
        int year = scanner.nextInt();

        final List<User> users = mgr.getUserManager().getUsers();
        System.out.println("Выберите сотрудника");
        users.forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        Date startDate = calendar.getTime();

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        Date endDate = calendar.getTime();

        params.put("user_id", scanner.next());
        params.put("from", startDate.toString());
        params.put("to", endDate.toString());

        List<TimeEntry> elements = timeEntryManager.getTimeEntries(params).getResults();
        elements = elements.stream()
                .sorted(Comparator.comparing(TimeEntry::getSpentOn))
                .toList();

        float sumOfHoursInWeek = 0;
        calendar.set(year, month, 1);
        int weekInMonth = calendar.get(Calendar.WEEK_OF_MONTH);

        List<Pair<Integer, Float>> hoursInWeek = new ArrayList<>();

        for (TimeEntry element : elements) {
            calendar.setTime(element.getSpentOn());

            if (calendar.get(Calendar.WEEK_OF_MONTH) == weekInMonth) {
                sumOfHoursInWeek += element.getHours();
            } else {
                hoursInWeek.add(new Pair<>(weekInMonth, sumOfHoursInWeek));
                sumOfHoursInWeek = element.getHours();
                weekInMonth = calendar.get(Calendar.WEEK_OF_MONTH);
            }
        }

        System.out.println("Рабочие часы сотрудника по неделям");
        hoursInWeek.forEach(el -> System.out.println("Неделя " + el.getValue0() + ": " + el.getValue1()));

        System.out.println("Недели, в которых рабочее время сотрудника составило менее 40 часов");
        hoursInWeek.forEach(el ->
        {
            if (el.getValue1() < weeks[el.getValue0()]) {
                System.out.println("Неделя " + hoursInWeek.indexOf(el) + ": " + el);
            }
        });
    }
}