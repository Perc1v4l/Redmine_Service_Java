package org.example;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.TimeEntryManager;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.User;
import org.javatuples.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WorkTimeWorker {

    Scanner scanner = new Scanner(System.in);
    RedmineManager mgr;

    public WorkTimeWorker(RedmineManager mgr) throws RedmineException {
        this.mgr = mgr;
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
            System.out.println("Выберите задание: ");
            userChoice = scanner.nextInt();
            switch (userChoice) {
                case 1 -> task1(mgr);
                case 2 -> task2(mgr);
                case 3 -> task3(mgr);
                case 0 -> System.out.println("До свидания");
            }
        } while (userChoice != 0);
    }

    private void task1(RedmineManager mgr) throws RedmineException {
        TimeEntryManager timeEntryManager = mgr.getTimeEntryManager();
        final Map<String, String> params = new HashMap<>();
        List<User> users = mgr.getUserManager().getUsers();


        System.out.println("Введите начальную дату");
        Date startDate = formatDates(scanner.next());

        System.out.println("Введите начальную дату");
        Date endDate = formatDates(scanner.next());

        System.out.println("Выберите сотрудника");
        users.stream()
            .sorted(Comparator.comparing(User::getId))
            .forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));


        params.put("user_id", Integer.toString(scanner.nextInt()));
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

        Set<Map.Entry<Date, Float>> mapSet = dateHoursMap.entrySet();
        mapSet = mapSet.stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toCollection(LinkedHashSet::new));

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ");

        System.out.println("Рабочие дни и время сотрудника:");
        mapSet.forEach(el -> System.out.println("Дата: " + dateFormat.format(el.getKey())
            + "\tКол-во часов: " + el.getValue()));

        System.out.println("\nДни, в которые сотрудник работал менее 8 часов");
        Date date = startDate;
        endDate = addDays(endDate, 1);
        while (!date.equals(endDate)) {
            if (dateHoursMap.containsKey(date)) {
                if (dateHoursMap.get(date) < 8) {
                    System.out.println("Дата: " + dateFormat.format(date)
                        + "\tКол-во часов: " + dateHoursMap.get(date));
                }
            } else {
                System.out.println("Дата: " + dateFormat.format(date) + "\tКол-во часов: " + 0);
            }
            date = addDays(date, 1);
        }
    }


    private static Date formatDates(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        return java.sql.Date.valueOf(LocalDate.parse(date, formatter));
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    private void task2(RedmineManager mgr) throws RedmineException {
        Map<String, String> params = new HashMap<>();

        System.out.print("Введите месяц: ");
        int month = scanner.nextInt() - 1;

        System.out.print("Введите год: ");
        int year = scanner.nextInt();

        final List<User> users = mgr.getUserManager().getUsers();

        System.out.println("Выберите сотрудника");
        users.stream()
            .sorted(Comparator.comparing(User::getId))
            .forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        Date startDate = calendar.getTime();

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        Date endDate = calendar.getTime();

        int countOfWeeks = calendar.get(Calendar.WEEK_OF_MONTH);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        params.put("user_id", Integer.toString(scanner.nextInt()));
        params.put("from", dateFormat.format(startDate));
        params.put("to", dateFormat.format(endDate));

        List<TimeEntry> elements = mgr.getTimeEntryManager().getTimeEntries(params).getResults();

        elements = elements.stream()
            .sorted(Comparator.comparing(TimeEntry::getSpentOn))
            .toList();

        float sumOfHoursInWeek = 0;
        calendar.set(year, month, 1);
        int weekInMonth = calendar.get(Calendar.WEEK_OF_MONTH);

        List<Float> hoursInWeek = new ArrayList<>();
        for (int i = 0; i < countOfWeeks; i++) {
            hoursInWeek.add(i, (float) 0);
        }

        for (TimeEntry element : elements) {
            calendar.setTime(element.getSpentOn());

            if (calendar.get(Calendar.WEEK_OF_MONTH) == weekInMonth) {
                sumOfHoursInWeek += element.getHours();
            } else {
                hoursInWeek.set(weekInMonth - 1, sumOfHoursInWeek);
                sumOfHoursInWeek = element.getHours();
                weekInMonth = calendar.get(Calendar.WEEK_OF_MONTH);
            }
        }

        System.out.println("Рабочие часы сотрудника по неделям");
        for (int i = 1; i <= hoursInWeek.size(); i++) {
            System.out.println("Неделя " + i + ": " + hoursInWeek.get(i - 1));
        }

        System.out.println("Недели, в которых рабочее время сотрудника составило менее 40 часов");

        for (int i = 1; i <= hoursInWeek.size(); i++) {
            if (hoursInWeek.get(i - 1) < 40) {
                System.out.println("Неделя " + i + ": " + hoursInWeek.get(i - 1));
            }
        }
    }

    private void task3(RedmineManager mgr) throws RedmineException {
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
        int month = scanner.nextInt() - 1;

        System.out.print("Введите год: ");
        int year = scanner.nextInt();

        final List<User> users = mgr.getUserManager().getUsers();

        System.out.println("Выберите сотрудника");
        users.stream()
            .sorted(Comparator.comparing(User::getId))
            .forEach(user -> System.out.println(user.getId() + ") " + user.getFullName()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        Date startDate = calendar.getTime();

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        Date endDate = calendar.getTime();

        int countOfWeeks = calendar.get(Calendar.WEEK_OF_MONTH);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int userId = scanner.nextInt();

        params.put("user_id", Integer.toString(userId));
        params.put("from", dateFormat.format(startDate));
        params.put("to", dateFormat.format(endDate));

        List<TimeEntry> elements = timeEntryManager.getTimeEntries(params).getResults();
        elements = elements.stream()
            .sorted(Comparator.comparing(TimeEntry::getSpentOn))
            .toList();

        float sumOfHoursInWeek = 0;
        calendar.set(year, month, 1);
        int weekInYear = calendar.get(Calendar.WEEK_OF_YEAR);

        List<Pair<Integer, Float>> hoursInWeek = new ArrayList<>();

        for (int i = 0; i < countOfWeeks; i++) {
            hoursInWeek.add(new Pair<>(weekInYear + i, (float) 0));
        }

        for (TimeEntry element : elements) {
            calendar.setTime(element.getSpentOn());

            if (calendar.get(Calendar.WEEK_OF_YEAR) == weekInYear) {
                sumOfHoursInWeek += element.getHours();
            } else {
                int index = hoursInWeek.indexOf(new Pair<>(weekInYear, (float) 0));
                hoursInWeek.set(index, new Pair<>(weekInYear, sumOfHoursInWeek));
                sumOfHoursInWeek = element.getHours();
                weekInYear = calendar.get(Calendar.WEEK_OF_YEAR);
            }
        }

        System.out.println("Рабочие часы сотрудника по неделям");
        for (int i = 1; i <= hoursInWeek.size(); i++) {
            System.out.println("Неделя " + hoursInWeek.get(i - 1).getValue0()
                + ": " + hoursInWeek.get(i - 1).getValue1()
            + "\tВ календаре: " + weeks[hoursInWeek.get(i - 1).getValue0()]);
        }

        System.out.println("Недели, в которых рабочее время сотрудника было меньше, чем в рабочем календаре");

        for (int i = 1; i <= hoursInWeek.size(); i++) {
            if (hoursInWeek.get(i - 1).getValue1() < weeks[hoursInWeek.get(i - 1).getValue0()]) {
                System.out.println("Неделя " + hoursInWeek.get(i - 1).getValue0() + ": " + hoursInWeek.get(i - 1).getValue1());
            }
        }
    }
}