package com.seangull.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlCondition {

    private String condition = "";

    public SqlCondition(String condition){
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }

    public SqlCondition append(String fragment){
        condition += String.format(" %s",fragment);
        return this;
    }

    public SqlCondition and(String fragment){
        condition += " and ";
        condition += fragment;
        return this;
    }

    public SqlCondition and(SqlCondition sqlCondition){
        condition = String.format("(%s) and (%s)",
                condition,
                sqlCondition.getCondition());
        return this;
    }

    public SqlCondition or(String flagment){
        condition += " or ";
        condition += flagment;
        return this;
    }

    public SqlCondition or(SqlCondition sqlCondition){
        condition = String.format("(%s) or (%s)",
                condition,
                sqlCondition.getCondition());
        return this;
    }

    public static boolean isSqlInjection(String value){
        Pattern pattern = Pattern.compile("(')|" +
                "(--)|" +
                "(/\\*)|" +
                "(\\b(" +
                "SELECT|select|" +
                "UPDATE|update|" +
                "UNION|union|" +
                "AND|and|" +
                "OR|or|" +
                "DELETE|delete|" +
                "INSERT|insert|" +
                "TRUNCATE|truncate|" +
                "CHAR|char|" +
                "INTO|into|" +
                "SUBSTR|substr|" +
                "ASCII|ascii|" +
                "DECLARE|declare|" +
                "COUNT|count|" +
                "MASTER|master|" +
                "DROP|drop|" +
                "EXEC|exec|" +
                "EXECUTE|execute" +
                ")\\b)");
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    @Override
    public String toString() {
        return condition;
    }
}
