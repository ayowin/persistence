package com.seangull.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class PojoGenerator {

    private class Property{
        public Class<?> clazz = null;
        public String name = null;

        public Property(Class<?> clazz,String name){
            this.clazz = clazz;
            this.name = name;
        }
    }

    private String packageName = null;
    private String className = null;
    private List<Property> propertyList = null;
    private String dirPath = null;

    public PojoGenerator(){

    }

    public void setPackageName(String packageName){
        this.packageName = packageName;
    }

    public void setClassName(String className){
        this.className = className;
    }

    public void addProperty(Class<?> clazz,String name ) throws Exception {
        if(clazz == null || name == null){
            throw new NullPointerException(String.format("add property failed with null reference (clazz: %s,name: %s)",
                    clazz.getName(),
                    name));
        }

        if(propertyList == null){
            propertyList = new ArrayList<>();
        }
        propertyList.add(new Property(clazz,name));
    }

    public void setDirPath(String dirPath){
        this.dirPath = dirPath;
    }

    public void generate() throws Exception {
        if (packageName == null ||
                className == null ||
                propertyList == null ||
                dirPath == null) {
            String result = String.format("generate failed with:\n\t" +
                            "package name:%s\n\t" +
                            "class name:%s\n\t" +
                            "property:%s\n\t" +
                            "dirPath:%s\n",
                    packageName,
                    className,
                    propertyList,
                    dirPath);
            throw new NullPointerException(result);
        }

        /* package */
        String code = "";
        code += String.format("package %s;\n\n", packageName);

        /* import */
        Set<String> importPathSet = new HashSet<>();
        for (Property property : propertyList) {
            String importPath = property.clazz.getName();
            if(importPath.indexOf("[L") >= 0){
                importPath = importPath.substring(2,importPath.length()-1);
            }
            importPathSet.add(importPath);
        }
        for(String importPath : importPathSet){
            code += String.format("import %s;\n",
                    importPath);
        }
        code += "\n";

        /* class name */
        code += String.format("public class %s {\n\n", className);

        /* fields */
        for (Property property : propertyList) {
            code += String.format("\tprivate %s %s;\n",
                    property.clazz.getSimpleName(),
                    property.name);
        }

        /* setter & getter */
        for (Property property : propertyList) {
            String firstUpperCasePropertyName = (new StringBuilder())
                    .append(Character.toUpperCase(property.name.charAt(0)))
                    .append(property.name.substring(1)).toString();
            /* setter */
            code += String.format("\n\tpublic void set%s(%s %s){\n" +
                            "\t\tthis.%s = %s;\n" +
                            "\t}\n",
                    firstUpperCasePropertyName,
                    property.clazz.getSimpleName(),
                    property.name,
                    property.name,
                    property.name);
            /* getter */
            code += String.format("\n\tpublic %s get%s(){\n" +
                            "\t\treturn this.%s;\n" +
                            "\t}\n",
                    property.clazz.getSimpleName(),
                    firstUpperCasePropertyName,
                    property.name);
        }
        code += "\n}";

        /* save */
        File file = new File(dirPath);
        if(!file.exists()){
            file.mkdir();
        }
        file = new File(dirPath, String.format("%s.java", className));
        if (!file.exists()) {
            file.createNewFile();
            Writer writer = null;
            writer = new FileWriter(file);
            writer.write(code);
            writer.close();
            System.out.println(String.format("generate %s success",file.getAbsolutePath()));
        } else {
            throw new FileAlreadyExistsException(String.format("generate failed with %s existed",file.getAbsolutePath()));
        }
    }

    public static void mysqlGenerate(String jdbcUrl,String username,String password,
                                     String[] tables,String packageName,String dirPath) throws Exception{
        for(String table : tables){
            PojoGenerator pojoGenerator = new PojoGenerator();
            pojoGenerator.setPackageName(packageName);
            pojoGenerator.setDirPath(dirPath);
            pojoGenerator.setClassName(MysqlPojoFormatter.mysqlTableNameToPojoClassName(table));

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl,username,password);
            Statement statement = connection.createStatement();
            String sql = String.format("select * from %s where 1=0;",table);
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCout = resultSetMetaData.getColumnCount();
            for(int i=1;i<=columnCout;i++){
                int type = resultSetMetaData.getColumnType(i);
                JDBCType jdbcType = JDBCType.valueOf(type);
                String columnName = resultSetMetaData.getColumnName(i);
                columnName = MysqlPojoFormatter.mysqlColumnNameToPojoFieldName(columnName);

                /* match java type for jdbc type  */
                switch (jdbcType){
                    case CHAR:
                    case VARCHAR:
                    case LONGVARCHAR:
                    case CLOB:
                        pojoGenerator.addProperty(String.class,columnName);
                        break;
                    case NUMERIC:
                    case DECIMAL:
                        pojoGenerator.addProperty(BigDecimal.class,columnName);
                        break;
                    case BIT:
                    case BOOLEAN:
                        pojoGenerator.addProperty(Boolean.class,columnName);
                        break;
                    case TINYINT:
                        pojoGenerator.addProperty(Byte.class,columnName);
                        break;
                    case SMALLINT:
                    case INTEGER:
                        pojoGenerator.addProperty(Integer.class,columnName);
                        break;
                    case BIGINT:
                        pojoGenerator.addProperty(Long.class,columnName);
                        break;
                    case FLOAT:
                    case DOUBLE:
                        pojoGenerator.addProperty(Double.class,columnName);
                        break;
                    case BINARY:
                    case VARBINARY:
                    case LONGVARBINARY:
                    case BLOB:
                        pojoGenerator.addProperty(Byte[].class,columnName);
                        break;
                    case DATE:
                        pojoGenerator.addProperty(Date.class,columnName);
                        break;
                    case TIME:
                        pojoGenerator.addProperty(Time.class,columnName);
                        break;
                    case TIMESTAMP:
                        pojoGenerator.addProperty(Timestamp.class,columnName);
                        break;
                    case ARRAY:
                        pojoGenerator.addProperty(Array.class,columnName);
                        break;
                    default:
                        break;
                }
            }
            connection.close();

            pojoGenerator.generate();
        }
    }

}
