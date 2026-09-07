package crypticlib.database;

import crypticlib.database.annotation.Field;
import crypticlib.database.annotation.Table;

@Table(name = "test_users")
public class TestUser {

    @Field(name = "id", id = true, generated = true)
    private long id;

    @Field(name = "username", nullable = false, unique = true)
    private String username;

    @Field(name = "age", defaultValue = "0")
    private int age;

    @Field(name = "balance", defaultValue = "0")
    private double balance;

    public TestUser() {
    }

    public TestUser(String username, int age, double balance) {
        this.username = username;
        this.age = age;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "TestUser{id=" + id + ", username='" + username + "', age=" + age + ", balance=" + balance + "}";
    }

}
