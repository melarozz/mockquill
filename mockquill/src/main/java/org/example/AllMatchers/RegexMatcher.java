package org.example.AllMatchers;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Matches a String argument against a regular expression.
 */
public class RegexMatcher implements ArgumentMatcher<String> {
    private final Pattern pattern;

    public RegexMatcher(String regex) {
        try {
            this.pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + regex, e);
        }
    }

    @Override
    public boolean matches(String argument) {
        return argument != null && pattern.matcher(argument).matches();
    }
}
