package org.example.task1;

public class Abonent {
    private String surname;
    private String name;
    private String patronymic;
    private String address;
    private int localCallTime;
    private int internationalCallTime;

    public Abonent(String surname, String name, String patronymic, String address,
                   int localCallTime, int internationalCallTime) throws InvalidAbonentDataException, NegativeTimeException {
        if (surname == null || surname.isEmpty() || name == null || name.isEmpty()) {
            throw new InvalidAbonentDataException("Прізвище та ім'я не можуть бути порожніми.");
        }
        if (localCallTime < 0 || internationalCallTime < 0) {
            throw new NegativeTimeException("Час переговорів не може бути від'ємним.");
        }

        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.address = address;
        this.localCallTime = localCallTime;
        this.internationalCallTime = internationalCallTime;
    }

    public int getLocalCallTime() { return localCallTime; }
    public int getInternationalCallTime() { return internationalCallTime; }

    @Override
    public String toString() {
        return String.format("Абонент: %s %s %s | Адреса: %s | Внутр. час: %d хв | Міжнар. час: %d хв",
                surname, name, patronymic, address, localCallTime, internationalCallTime);
    }
}