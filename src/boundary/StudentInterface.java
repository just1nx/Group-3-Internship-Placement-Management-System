package boundary;

import entity.User;

public class StudentInterface implements CommandLineInterface {
    private User student;

    public StudentInterface(User student) {
        this.student = student;
    }

    @Override
    public void display() {
        System.out.println("Welcome to Student Interface");
    }
}
