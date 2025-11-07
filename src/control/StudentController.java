package control;

public class StudentController {
}

//package control;
//
//import entity.Student;
//import entity.Internship;
//import entity.Applications;
//import entity.ApplicationStatus; //need to create enum class in entity package
//import entity.InternshipLevel;   // need to create enum class in entity package
//import entity.WithdrawalRequest; // need to create enum class in entity package
//import entity.WithdrawalStatus; // need to create enum class in entity package
//import control.ApplicationManager;  //need to create ApplicationManager class in control package
//import java.util.List;
//
//public class StudentController {
//    private final InternshipController internshipController;
//    private final ApplicationManager applicationManager;
//    private final DataController dataController;
//
//    public StudentController(InternshipController internshipController,
//                             ApplicationManager applicationManager,
//                             DataController dataController) {
//        this.internshipController = internshipController;
//        this.applicationManager = applicationManager;
//        this.dataController = dataController;
//                             }
//
//    private boolean checkEligibility(Student student, Internship internship) {
//        int year = student.getYeatOfStudy();
//        InternshipLevel level = internship.getLevel();
//        // Implement eligibility logic based on student's major, level, etc.
//
//        [cite_start]
//        if (year <= 2 && level == InternshipLevel.BASIC) {
//            return false;
//        }
//        [cite_end]
//        return true;
//    }
//
//
//    private long getActiveApplicationCount(String studentId) {
//        List<Applications> applications = applicationManager.getApplicationsByStudentId(studentId);
//        return applications.stream()
//                .filter(app -> app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.SUCCESSFUL)
//                .count();
//    }
//
////----------------------Student Applying for an Internship--------------------------------------------------------------------------------------------
//
//    public boolean applyForInternship(String studentId, String internshipTitle) {
//        Student student = dataController.getStudentById(studentId);
//        Internship internship = internshipController.getInternshipByTitle(internshipTitle);
//
//        if (student == null || internship == null) {
//            System.out.println("Error: Invalid student ID or internship title.");
//            return false;
//        }
//
//        if (!checkEligibility(student, internship)) {
//            System.out.println("Error: Student is not eligible for this internship.");
//            return false;
//        }
//
//        if (getActiveApplicationCount(studentId) >= 3) {
//            System.out.println("Error: Student has reached the maximum number of active applications.");
//            return false;
//        }
//
//        if (!opportunity.isVisible() || opportunity.getStatus() != OpportunityStatus.APPROVED) {
//            System.out.println("Error: Internship is not currently visible for applications.");
//            return false;
//        }
//
//        Applications newApplication = new Applications(studentId, internshipTitle, ApplicationStatus.PENDING);
//        applicationManager.addApplication(newApplication);
//        System.out.println("Application submitted successfully.");
//        return true;
//    }
//
////----------------------Student Accpeting a successful placement--------------------------------------------------------------------------------------------
//
//    public boolean acceptPlacement(String studentId, String internshipTitle) {
//        String studentId = student.getStudentId();
//        //find the target application
//        Applications aacceptedapp = applicationManager.getApplication(studentId, internshipTitle);
//
//        if (application == null || application.getStatus() != ApplicationStatus.SUCCESSFUL) {
//            System.out.println("Error: No successful application found for this internship.");
//            return false;
//        }
//
//        if (acceptedapp.isConfirmed())  {
//            System.out.println("Error: Placement has already been accepted.");
//            return false;
//        }
//
//        //1.Accept the placement (setting confirmed flag)
//        acceptedApp.setConfirmed(true);
//
//        //2. update the internship slot count
//        internshipController.incrementInternshipSlot(internshipTitle);
//
//        //3 Withdraw other pending applications of student
//        withdrawOtherApplications(studentId, acceptedApp);
//
//        //4. Record the acceptance into csv file
//        dataController.recordAcceptedPlacement(studentID, internshipTitle);
//        System.out.println("Placement accepted successfully. All other active applications have been withdrawn.");
//        return true;
//    }
//
//    private void withdrawOtherApplications(String studentId, Applications acceptedApp) {
//        List<Applications> allApps = applicationManager.getApplicationsByStudentId(studentId);
//        for (Applications app : allApps) {
//            if (!app.equals(acceptedApp) && app.getStatus() == ApplicationStatus.PENDING) {
//                app.setStatus(ApplicationStatus.UNSUCCESSFUL);
//
//            }
//        }
//        applicationManager.saveApplications();
//    }
//
//    public boolean requestWithdrawal(String studentId, String internshipTitle) {
//        Applications application = applicationManager.getApplication(studentId, internshipTitle);
//
//        if (application == null || application.getStatus() != ApplicationStatus.PENDING) {
//            System.out.println("Error: No pending application found for this internship.");
//            return false;
//        }
//
//        // Create and add withdrawal request
//        WithdrawalRequest request = new WithdrawalRequest(studentId, internshipTitle, WithdrawalStatus.PENDING);
//        applicationManager.addWithdrawalRequest(request);
//        System.out.println("Withdrawal request submitted successfully.");
//        return true;
//    }
//
//    public List<Internship> viewAvailableInternships(String studentId) {
//        Student student = dataController.getStudentById(studentId);
//        if (student == null) {
//            System.out.println("Error: Invalid student ID.");
//            return List.of();
//        }
//
//        List<Internship> allInternships = internshipController.getAllVisibleInternships();
//        return allInternships.stream()
//                .filter(internship -> checkEligibility(student, internship))
//                .collect(Collectors.toList());
//    }
//
//
