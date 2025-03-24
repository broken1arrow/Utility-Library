package org.broken.arrow.database.library.construct.query.utlity;

public enum QueryType {
    SELECT, DELETE, DROP, CREATE, CREATE_IF_NOT_EXISTS, UPDATE, WITH, INSERT, NON;

    public static QueryType getType(final String type) {
      if (type == null) return NON;
      String typeUp = type.toUpperCase();
      for (QueryType queryType : QueryType.values()) {
        if (queryType.toString().equals(typeUp)) {
          return queryType;
        }
      }
      return NON;
    }

    public static QueryType getStartsWithType(final String type) {
      if (type == null) return NON;
      String typeUp = type.toUpperCase();
      for (QueryType queryType : QueryType.values()) {
        if (typeUp.startsWith(queryType.toString())) {
          return queryType;
        }
      }
      return NON;
    }

  }
