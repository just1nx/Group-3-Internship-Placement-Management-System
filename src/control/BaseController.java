package control;

public abstract class BaseController {
    // Helper method to escape special characters for CSV format
    protected String escapeCSV(String s) {
        if (s == null) s = "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            out = "\"" + out + "\"";
        }
        return out;
    }

    // Helper method to unquote fields
    protected String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}
