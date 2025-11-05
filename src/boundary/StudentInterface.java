package boundary;

//import control.StudentController;
import entity.User;

public class StudentInterface implements CommandLineInterface {
    private User student;
    //private StudentController studentController = new StudentController();

    public StudentInterface(User student) {
        this.student = student;
    }

    @Override
    public void display() {
        System.out.println("Welcome to Student Interface");
    }
}
