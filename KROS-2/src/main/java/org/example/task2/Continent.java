package org.example.task2;

import java.util.ArrayList;
import java.util.List;

public class Continent implements SearchableHistory {
    private String name;
    private List<TerritorialDivision> history;

    public Continent(String name) {
        this.name = name;
        this.history = new ArrayList<>();
    }

    public void addDivision(List<String> states, int startYear, int endYear) {
        history.add(new TerritorialDivision(states, startYear, endYear));
    }

    @Override
    public void searchDivisionByYear(int year) {
        System.out.println("Пошук територіального поділу для материка " + name + " у " + year + " році:");
        boolean found = false;

        for (TerritorialDivision division : history) {
            if (year >= division.startYear && year <= division.endYear) {
                System.out.println(division);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Інформації про територіальний поділ у цьому році немає.");
        }
    }

    // Внутрішній клас
    public class TerritorialDivision {
        private List<String> states;
        private int startYear;
        private int endYear;

        public TerritorialDivision(List<String> states, int startYear, int endYear) {
            this.states = states;
            this.startYear = startYear;
            this.endYear = endYear;
        }

        @Override
        public String toString() {
            return String.format("Період: %d - %d рр. | Держави: %s",
                    startYear, endYear, String.join(", ", states));
        }
    }
}
