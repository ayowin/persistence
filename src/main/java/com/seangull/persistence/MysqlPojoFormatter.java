package com.seangull.persistence;

public class MysqlPojoFormatter {

    public static String mysqlTableNameToPojoClassName(String tableName){
        String className = new String(tableName);
        boolean isFrontUnderline = true;
        for (int i=0;i<className.length();i++){
            Character character = className.charAt(i);
            if(character == '_'){
                if(i+1 < className.length()){
                    character = className.charAt(i+1);
                    if(Character.isLetter(character)){
                        String prefix = "";
                        if(isFrontUnderline){
                            prefix = className.substring(0,i+1);
                        } else {
                            prefix = className.substring(0,i);
                        }
                        character = Character.toUpperCase(character);
                        String suffix = "";
                        if(i+2 < className.length()){
                            suffix = className.substring(i+2);
                        }
                        className = prefix + character + suffix;
                    }
                }
            } else {
                isFrontUnderline = false;
            }
        }
        className = (new StringBuilder())
                .append(Character.toUpperCase(className.charAt(0)))
                .append(className.substring(1)).toString();
        return className;
    }

    public static String pojoClassNameToMysqlTableName(String className){
        String tableName = new String(className);
        boolean isFirstUpper = true;
        for(int i=0;i<tableName.length();i++){
            Character character = tableName.charAt(i);

            if(Character.isUpperCase(character)){
                String prefix = tableName.substring(0,i);
                if(isFirstUpper){
                    isFirstUpper = false;
                } else {
                    prefix += "_";
                }
                character = Character.toLowerCase(character);
                String suffix = tableName.substring(i+1);
                tableName = prefix + character + suffix;
            }
        }
        return tableName;
    }

    public static String mysqlColumnNameToPojoFieldName(String columnName){
        String fieldName = new String(columnName);
        boolean isFrontUnderline = true;
        for (int i=0;i<fieldName.length();i++){
            Character character = fieldName.charAt(i);
            if(character == '_'){
                if(i+1 < fieldName.length()){
                    character = fieldName.charAt(i+1);
                    if(Character.isLetter(character)){
                        String prefix = "";
                        if(isFrontUnderline){
                            prefix = fieldName.substring(0,i+1);
                            character = Character.toLowerCase(character);
                        } else {
                            prefix = fieldName.substring(0,i);
                            character = Character.toUpperCase(character);
                        }
                        String suffix = "";
                        if(i+2 < fieldName.length()){
                            suffix = fieldName.substring(i+2);
                        }
                        fieldName = prefix + character + suffix;
                    }
                }
            } else {
                isFrontUnderline = false;
            }
        }
        fieldName = (new StringBuilder())
                .append(Character.toLowerCase(fieldName.charAt(0)))
                .append(fieldName.substring(1)).toString();
        return fieldName;
    }

    public static String pojoFieldNameToMysqlColumnName(String fieldName){
        String columnName = new String(fieldName);
        for(int i=0;i<columnName.length();i++){
            Character character = columnName.charAt(i);
            if(Character.isUpperCase(character)){
                String prefix = columnName.substring(0,i);
                prefix += "_";
                character = Character.toLowerCase(character);
                String suffix = columnName.substring(i+1);
                columnName = prefix + character + suffix;
            }
        }
        return columnName;
    }
}
