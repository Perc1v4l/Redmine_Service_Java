package org.example;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws RedmineException {

        final RedmineManager mgr = RedmineManagerFactory.createWithApiKey("http://localhost:8080/",
            "9af5b8f742ef1fa0427d2d9b764a8d3c870ec11a");
        mgr.setObjectsPerPage(100);

        System.out.println("""
                1) Просмотр задач;
                2) Просмотр трудоёмкости.
                """);
        Scanner scanner = new Scanner(System.in);
        int userChoice;

        do {
            userChoice = scanner.nextInt();
            switch (userChoice) {
                case 1 -> new IssuesWorker(mgr);
                case 2 -> new WorkTimeWorker(mgr);
                case 0 -> System.out.println("До свидания");
                default -> System.out.println("Неверный ввод");
            }
        } while (userChoice != 0);
    }
}
//15.03.2023 //14.04.2023