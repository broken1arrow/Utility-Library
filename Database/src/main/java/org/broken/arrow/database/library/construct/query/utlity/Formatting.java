package org.broken.arrow.database.library.construct.query.utlity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Formatting {
  private static final Pattern BETWEEN_PATTERN = Pattern.compile("(?i)\\bBETWEEN\\s+[^\\s]+\\s+AND\\s+[^\\s]+");

  //private static final Pattern pattern = Pattern.compile("(?i)\\band\\s*\\(");
  private static final Pattern checkOR = Pattern.compile("(?i)\\bor\\s*\\(", Pattern.CASE_INSENSITIVE);

  //private static Pattern orPattern = Pattern.compile("^\\s*OR\\b.*\\(\\s*SELECT\\b", Pattern.CASE_INSENSITIVE);

  public static String formatConditions(String sql) {
    String whereClause = removeAfterWhere(sql);
    String formattedWhere = processWhereClause(whereClause);
    return sql.replace(whereClause, formattedWhere);
  }

  private static String removeAfterWhere(String sql) {
    int whereIndex = sql.toUpperCase().indexOf(" WHERE ");
    if (whereIndex == -1 || (whereIndex > 1 && !checkStart(sql))) return sql;

    return sql.substring(whereIndex + 7).trim();
  }

  private static String processWhereClause(String condition) {
    List<String> formattedNested = new ArrayList<>();
    List<String> formatted = new ArrayList<>();

    List<SubQueryPos> subQueryList = shouldWrapInParentheses(condition);
    StringBuilder buildAlreadySetParenthesis = new StringBuilder();

    processQuery(subQueryList, buildAlreadySetParenthesis, formatted);
    wrapAndParentheses(buildAlreadySetParenthesis.toString(), formattedNested);
    final StringBuilder buildQuery = new StringBuilder();
    String spacing = checkParenthesesAppliedCorrectly(formattedNested, formatted, buildQuery);

    return buildQuery.toString().replace(")" + spacing + "(", " ");
  }

  private static String checkParenthesesAppliedCorrectly(List<String> formattedNested, List<String> formatted, StringBuilder buildQuery) {
    String insideParentheses = String.join(" OR ", formattedNested);
    String text = String.join(" OR ", formatted);

    if (!formatted.isEmpty() && !formattedNested.isEmpty()) {
      if (!insideParentheses.startsWith("(") && text.startsWith("(")) {

        if (CalcFunc.getStartsWithType(insideParentheses) == null) {
          insideParentheses = " " + insideParentheses.replace(")", "");
        } else {
          if (text.endsWith(")"))
            insideParentheses = " (" + insideParentheses;
        }
        if (!checkOR.matcher(text).find()) {
          text = text.replace("(", "");
        }else
          text = text.replace("(AND", "AND");
      }
    }

    String spacing = "";
    if (!text.startsWith(" ")) {
      spacing = " ";
    }
    buildQuery.append(insideParentheses);
    buildQuery.append(spacing).append(text);
    return spacing;
  }

  private static void processQuery(final List<SubQueryPos> subQueryList, final StringBuilder build, final List<String> formatted) {
    for (SubQueryPos subQuery : subQueryList) {
      final String query = subQuery.query;
      if (subQuery.getQueryType() != MatchQueryType.NON) {
        int firstPos = subQuery.getFirstPos();
        int lastPos = subQuery.getLastPos();
        if (firstPos >= 0 && lastPos > 0) {
          build.append(query);
        } else if (subQuery.getQueryType() == MatchQueryType.LEFTOVERS) {
          wrapAndParentheses(query, formatted);
        }
      } else {
        build.append(query);
      }
    }
  }

  private static void wrapAndParentheses(String string, List<String> formatted) {
    String[] orParts = string.split("(?i)\\s+OR(?=\\s+)");
    int leadingPart = -2;
    for (String part : orParts) {
      part = part.trim();
      if (part.isEmpty()) {
        int andIndex = orParts.length > 1 ? orParts[1].toLowerCase().indexOf(" and ") : -2;
        if (andIndex > 0) {
          leadingPart = 0;
        }
        continue;
      }
      if (leadingPart == -2) {
        if (containsStandaloneAnd(part)) {
          formatted.add("(" + part + ")");
        } else {
          formatted.add(part);
        }
      } else {
        formatted.add(" OR (" + part + ")");
        leadingPart = -2;
      }
    }
  }

  private static boolean containsStandaloneAnd(String part) {
    Matcher matcher = BETWEEN_PATTERN.matcher(part);
    String partWithoutBetween = matcher.replaceAll("");

    return partWithoutBetween.matches(".*\\bAND\\b.*");
  }

  private static List<SubQueryPos> shouldWrapInParentheses(final String input) {
    boolean insideQuotes = false;
    Map<Integer, SubQueryPos> subQueryPos = new LinkedHashMap<>();

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c == '\'') {
        insideQuotes = !insideQuotes;
      } else if (!insideQuotes) {
        if (c == '(') {
          int firstIndexMatch = checkFirstIndex(input, i);
          String subQueryRaw = buildStringFromCurrentIndex(input, firstIndexMatch);
          int endIndex = subQueryRaw.indexOf(")") + 1;
          String subQuarry = subQueryRaw.substring(0, endIndex);
          i = subQuarry.length() + firstIndexMatch;
          subQueryPos.put(firstIndexMatch, new SubQueryPos(MatchQueryType.HAS_SUB_QUERY, subQuarry, firstIndexMatch, i));
        }
      }
    }
    final List<Integer> list = new ArrayList<>();

    for (SubQueryPos keys : subQueryPos.values()) {
      list.addAll(IntStream.range(keys.getFirstPos(), keys.getLastPos()).sorted().boxed().collect(Collectors.toList()));
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (!list.contains(i)) {
        stringBuilder.append(c);
      }
    }
    subQueryPos.put(-2, new SubQueryPos(MatchQueryType.LEFTOVERS, stringBuilder.toString(), 0, 0));

    return new ArrayList<>(subQueryPos.values());
  }

  private static int checkFirstIndex(String cleanedInput, int currentIndex) {
    int i;
    String text = "";
    for (i = currentIndex; i < cleanedInput.length(); i--) {
      if (i < 0)
        break;
      char c = cleanedInput.charAt(i);
      text += c;
      String upperCase = text.toUpperCase();
      if (upperCase.contains("EREHW")) {
        break;
      }
      if (upperCase.contains("DNA") || upperCase.contains("RO")) {
        i--;
        break;
      }
    }
    return i == -1 ? 0 : i;
  }

  private static String buildStringFromCurrentIndex(String cleanedInput, int currentIndex) {
    int i;
    String text = "";
    for (i = currentIndex; i < cleanedInput.length(); i++) {
      char c = cleanedInput.charAt(i);
      text += c;
    }
    return text;
  }

  private static boolean checkStart(String sql) {
    return QueryType.getStartsWithType(sql) != QueryType.NON;
  }

  public enum MatchQueryType {
    HAS_SUB_QUERY,
    LEFTOVERS,
    NON
  }

  public static class SubQueryPos {
    private final MatchQueryType queryType;
    private final String query;
    private final int firstPos;
    private final int lastPos;

    public SubQueryPos(MatchQueryType queryType, String query, int firstPos, int lastPos) {
      this.queryType = queryType;
      this.query = query;
      this.firstPos = firstPos;
      this.lastPos = lastPos;
    }

    public MatchQueryType getQueryType() {
      return queryType;
    }

    public int getFirstPos() {
      return firstPos;
    }

    public int getLastPos() {
      return lastPos;
    }

    @Override
    public String toString() {
      return "SubQueryPos{" +
              "queryType=" + queryType +
              ", query='" + query + '\'' +
              ", firstPos=" + firstPos +
              ", lastPos=" + lastPos +
              '}';
    }
  }
}

