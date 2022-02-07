// 1. Создать класс «Сотрудник» с полями: ФИО, должность, email, телефон, зарплата, возраст;

import java.text.MessageFormat;

/**
 * Класс Сотрудник
 * @author mayur
 * */
public class Employee
{
    public final String FIO;
    public final String position;
    public final int salary;
    public final int age;
    public final String phoneNumber;
    public final String eMail;
    
    /**
     * 2. Конструктор класса должен заполнять эти поля при создании объекта;
     * */
    public Employee(String _fio, String _position, String _phoneNumber, String _eMail, int _age, int _salary)
    {
        FIO = _fio;
        phoneNumber = _phoneNumber;
        position = _position;
        salary = _salary;
        age = _age;
        eMail = _eMail;
    }
    
    public Employee()
    {
        FIO = eMail = position = phoneNumber =  "";
        age = salary = 0;
    }
    
    /**
     * Перегрузка метода преобразования объекта в строку
     *
     * @param fieldSeparator    строка-разделитель полей объекта
     * */
    public String toString(String fieldSeparator)
    {
        return MessageFormat.format("ФИО: {1}{0}Должность: {2}{0}Зарплата: {3}{0}Возраст: {4}{0}" +
                        "Номер телефона: {5}{0}Электронная почта: {6}{0}", fieldSeparator, FIO, position,
                salary, age, phoneNumber, eMail);
    }
    
    /**
     * Перегрузка метода преобразования объекта в строку
     * */
     public String toString()
    {
        return toString(";\t ");
    }
    
    /**
     * 3. Внутри класса «Сотрудник» написать метод, который выводит информацию об объекте в консоль (каждое поле с новой строки)
     * */
    public void println()
    {
        System.out.println(this.toString(System.lineSeparator()));
    }
    
    /**
     * Метод вывода содержимого объекта в консоль
     * */
    public void print()
    {
        System.out.print(this.toString());
    }
}