package control;

import entity.Internship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
//import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.time.LocalDate;

public class CompanyRepresentativeController {
    private final Map<String, Internship> internships = new HashMap<>();

    public CompanyRepresentativeController() {
        Path internshipPath = Paths.get("data/sample_internship_list.csv");
        loadInternships(internshipPath);
    }

    private void loadInternships(Path csvPath) {
        if (csvPath == null || !Files.exists(csvPath)) {
            System.err.println("Internship CSV not found: " + csvPath);
            return;
        }

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(cols -> cols.length > 0)
                    .forEach(cols -> {
                        String id = cols.length > 0 ? cols[0].trim() : "";
                        if (id.isEmpty()) return;

                        String title = cols.length > 1 ? cols[1].trim() : "";
                        String description = cols.length > 2 ? cols[2].trim() : "";
                        String level = cols.length > 3 ? cols[3].trim() : "";
                        String preferredMajor = cols.length > 4 ? cols[4].trim() : "";
                        LocalDate openingDate = LocalDate.now();
                        if (cols.length > 5) {
                            try {
                                String dateString = cols[5].trim();
                                openingDate = LocalDate.parse(dateString);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        LocalDate closingDate = LocalDate.now();
                        if (cols.length > 6) {
                            try {
                                String dateString = cols[6].trim();
                                closingDate = LocalDate.parse(dateString);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        String status = cols.length > 7 ? cols[7].trim() : "";
                        String companyName = cols.length > 8 ? cols[8].trim() : "";
                        String representatives = cols.length > 9 ? cols[9].trim() : "";
                        String numberOfSlots = cols.length > 10 ? cols[10].trim() : "";

                        Boolean visibility = false;
                        if (cols.length > 11) {
                            try {
                                visibility = Boolean.parseBoolean(cols[11].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        Internship internship = new Internship(title, description, level, preferredMajor, openingDate, closingDate, status, companyName, representatives, numberOfSlots, visibility);
                        // Student student = new Student(id, name, pw, email, year, major);
                        internships.put(id, internship);
                    });
        } catch (IOException e) {
            System.err.println("Failed to read student CSV: " + e.getMessage());
        }
    }

    public Boolean createInternship(String title, String description, String level, String preferredMajor,
             LocalDate openingDate, LocalDate closingDate, String status,
             String companyName, String representativeID, Integer slots) {

        Boolean visibility = false;

        java.util.function.Function<String, String> esc = s -> {
            if (s == null) s = "";
            String out = s.replace("\"", "\"\"");
            if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
                out = "\"" + out + "\"";
            }
            return out;
        };

        String line = String.join(",",
                esc.apply(title),
                esc.apply(description),
                esc.apply(level),
                esc.apply(preferredMajor),
                esc.apply(openingDate.toString()),
                esc.apply(closingDate.toString()),
                esc.apply(status),
                esc.apply(companyName),
                esc.apply(representativeID),
                esc.apply(Integer.toString(slots)),
                esc.apply(Boolean.toString(visibility))
        );

        Path path = Paths.get("data/sample_internship_list.csv");
        try {
            Files.write(path, Collections.singletonList(line), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);

            // CompanyRepresentative companyRep = new CompanyRepresentative(email, name, password, companyName, department, position, email, status);
            // companyReps.put(email, companyRep);
            Internship internship = new Internship(title, description, level, preferredMajor, openingDate, closingDate, status, companyName, representativeID, line, visibility);
            internships.put(internship.getUUID().toString(), internship);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write company rep CSV: " + e.getMessage());
            return false;
        }
    }
}
