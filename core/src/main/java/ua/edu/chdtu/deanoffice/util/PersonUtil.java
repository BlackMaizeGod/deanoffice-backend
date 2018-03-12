package ua.edu.chdtu.deanoffice.util;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class PersonUtil {

    public static String toCapitalizedCase(String string) {
        try {
            if (string == null) {
                return null;
            }

            if (string.isEmpty()) {
                return "";
            }

            return wordsToCapitalizedCase(string);
        } catch (Exception exception) {
            throw exception;
        }
    }

    private static String wordsToCapitalizedCase(String string) {
        List<String> words = asList(string.split("\\s+"));
        return words.stream()
                .filter(s -> !s.isEmpty())
                .map(s -> wordToCapitalizedCase(s))
                .collect(Collectors.joining(" "));
    }

    private static String wordToCapitalizedCase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
