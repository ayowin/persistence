package com.seangull.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HikaricpInteractor {

    private static Logger logger = LoggerFactory.getLogger(HikaricpInteractor.class);

    private HikariDataSource dataSource = null;

    public HikaricpInteractor(String jdbcUrl,String username,String password){
        this(jdbcUrl,username,password,
                30*1000,10*60*1000,30*60*1000,
                15,5,true);
    }

    public HikaricpInteractor(String jdbcUrl,String username,String password,
                               long connectTimeout,long idleTimeout,long maxLifeTime,
                               int maximumPoolSize,int minimumIdle,boolean autoCommit){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setConnectionTimeout(connectTimeout); /* unit: ms */
        hikariConfig.setIdleTimeout(idleTimeout); /* unit: ms */
        /* 
         * maxLifeTime: 
         *      1. unit: ms, default 30*60*1000. 
         *      2. it should be at least 30 seconds less than any
         *         databases' connection time limit,such as mysql's
         *         wait_timeout. so you can set this value or config
         *         database server's connection time limit.
         */
        hikariConfig.setMaxLifetime(maxLifeTime); 
        hikariConfig.setMaximumPoolSize(maximumPoolSize); /* recommend: (core_count*2) + effective_spindle_count */
        hikariConfig.setMinimumIdle(minimumIdle);
        hikariConfig.setAutoCommit(autoCommit);

        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * execute
     * @param sql: sql statement
     * @param clazz: pojo class
     *             1. only use for select statement,other statements you can pass null.
     *             2. you can also pass null for select statement,it will return a
     *                matched HashMap<String,Object> list.
     * @return a matched list for select statement, null for other statements
     * @throws Exception
     */
    public List<?> execute(String sql, Class<?> clazz) throws Exception {
        List<Object> list = null;

        Connection connection = this.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        logger.info(sql);

        /* state: true for has query result set , false for has non query result */
        boolean state = preparedStatement.execute();
        if(state){
            list = new ArrayList<>();
            ResultSet resultSet = preparedStatement.getResultSet();

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCout = resultSetMetaData.getColumnCount();
            List<String> columnNames = new ArrayList<>();
            for(int i=1;i<=columnCout;i++) {
                String columnName = resultSetMetaData.getColumnName(i);
                columnNames.add(columnName);
            }

            while (resultSet.next()) {
                if(clazz == null){
                    HashMap<String,Object> object = new HashMap<>();
                    for(String columnName : columnNames){
                        Object columnObject = resultSet.getObject(columnName);
                        object.put(columnName,columnObject);
                    }
                    list.add(object);
                } else {
                    Object object = clazz.newInstance();
                    for(String columnName : columnNames){
                        String fieldname = MysqlPojoFormatter.mysqlColumnNameToPojoFieldName(columnName);
                        Field field = clazz.getDeclaredField(fieldname);
                        if(field != null){
                            Object columnObject = resultSet.getObject(columnName);
                            field.setAccessible(true);
                            field.set(object,columnObject);
                        }
                    }
                    list.add(object);
                }
            }

            resultSet.close();
        }

        preparedStatement.close();
        connection.close();

        return list;
    }

    /**
     * select
     * @param clazz: pojo class
     * @param sqlCondition: sql condition
     * @param wherePrefix: add 'where' before sql condition or not
     * @return a matched list
     * @throws Exception
     */
    public List<?> select(Class<?> clazz, SqlCondition sqlCondition,Boolean wherePrefix) throws Exception {
        String sql = null;
        String tableName = MysqlPojoFormatter.pojoClassNameToMysqlTableName(clazz.getSimpleName());
        if(sqlCondition == null){
            sql = String.format("select * from %s;",
                    tableName);
        } else if(wherePrefix) {
            sql = String.format("select * from %s where %s;",
                    tableName,
                    sqlCondition);
        } else {
            sql = String.format("select * from %s %s;",
                    tableName,
                    sqlCondition);
        }

        List<?> list = this.execute(sql,clazz);

        return list;
    }

    /**
     * an overloaded select function that passes true for the parameter 'wherePrefix'
     */
    public List<?> select(Class<?> clazz, SqlCondition sqlCondition) throws Exception {
        return select(clazz,sqlCondition,true);
    }

    /**
     * insert
     * @param object: object you want to insert
     * @throws Exception
     */
    public void insert(Object object) throws Exception {
        String sql = null;

        Class<?> clazz = object.getClass();
        String tableName = MysqlPojoFormatter.pojoClassNameToMysqlTableName(clazz.getSimpleName());

        String fieldNames = null;
        String fieldValues = null;
        Field[] fields = clazz.getDeclaredFields();
        if(fields != null){
            boolean leftBracket = false;
            for (int i=0;i<fields.length;i++){
                Field field = fields[i];
                field.setAccessible(true);
                Object fieldObject = field.get(object);
                if(fieldObject != null){
                    if(!leftBracket){
                        fieldNames = "(";
                        fieldValues = "(";

                        leftBracket = true;
                    }
                    fieldNames += MysqlPojoFormatter.pojoFieldNameToMysqlColumnName(field.getName());
                    fieldNames += ",";

                    fieldValues += String.format("'%s'",fieldObject.toString());
                    fieldValues += ",";
                }
            }
            if(leftBracket){
                fieldNames = fieldNames.substring(0,fieldNames.length()-1);
                fieldNames += ")";

                fieldValues = fieldValues.substring(0,fieldValues.length()-1);
                fieldValues += ")";
            }
        }

        sql = String.format("insert into %s %s values %s;",
                tableName,
                fieldNames,
                fieldValues);

        this.execute(sql,null);
    }

    /**
     * delete
     * @param clazz: pojo class
     * @param sqlCondition: sql condition
     * @throws Exception
     */
    public void delete(Class<?> clazz, SqlCondition sqlCondition) throws Exception {
        String sql = null;

        String tableName = MysqlPojoFormatter.pojoClassNameToMysqlTableName(clazz.getSimpleName());

        if(sqlCondition != null){
            sql = String.format("delete from %s where %s;",
                    tableName,
                    sqlCondition);
        }

        this.execute(sql,null);
    }


    /**
     * update
     * @param object: object you want to update
     * @param sqlCondition: sql condition
     * @throws Exception
     */
    public void update(Object object, SqlCondition sqlCondition) throws Exception {
        String sql = null;

        Class<?> clazz = object.getClass();
        String tableName = MysqlPojoFormatter.pojoClassNameToMysqlTableName(clazz.getSimpleName());

        String fieldStatements = null;
        Field[] fields = clazz.getDeclaredFields();
        if(fields != null){
            for (int i=0;i<fields.length;i++){
                Field field = fields[i];
                field.setAccessible(true);
                Object fieldObject = field.get(object);
                if(fieldObject != null){
                    if(fieldStatements == null){
                        fieldStatements = "";
                    }
                    fieldStatements += String.format("%s='%s',",
                            MysqlPojoFormatter.pojoFieldNameToMysqlColumnName(field.getName()),
                            fieldObject.toString());
                }
            }
        }

        if(fieldStatements != null && sqlCondition != null){
            fieldStatements = fieldStatements.substring(0,fieldStatements.length()-1);
            sql = String.format("update %s set %s where %s;",
                    tableName,
                    fieldStatements,
                    sqlCondition);
        }

        this.execute(sql,null);
    }
}
