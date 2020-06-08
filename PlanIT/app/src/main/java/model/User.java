package model;

import com.example.planit.utils.Utils;

public class User {

    private Integer id;
    private String name;
    private String lastName;
    private String password;
    private String email;
    private String colour;

    public User(String email) {
        this.email = email;
    }

    public User(Integer id, String email) {
        this.id = id;
        this.email = email;
    }

    public User(Integer id, String email, String name, String lastName, String colour) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.colour = colour;
        this.email = email;
        this.colour = Utils.getRandomColor();
    }

    public User(String name, String lastName, String password, String username) {
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.email = username;
        this.colour = Utils.getRandomColor();
    }

    public User(String name, String lastName, String email) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.colour = Utils.getRandomColor();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", colour='" + colour + '\'' +
                '}';
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
