package com.seangull.persistence;

import org.junit.Test;

import java.util.List;

public class PersistenceTest {

    @Test
    public void mysqlPojoGenerateTest() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/persistence?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false";
        String username  = "root";
        String password = "123456";
        String[] tables = new String[]{"user"};
        String packageName = "com.seangull.persistence";
        String dirPath = "D:/pojo";

        PojoGenerator.mysqlGenerate(jdbcUrl,username,password,tables,packageName,dirPath);
    }

    @Test
    public void conditionTest(){
        SqlCondition sqlCondition = new SqlCondition("id = 1");
        System.out.println(sqlCondition);

        sqlCondition.or("username = 'admin'");
        System.out.println(sqlCondition);

        sqlCondition.and(new SqlCondition("password = '123456'")
                .and("update_time like '%2022%'"));
        System.out.println(sqlCondition);

        sqlCondition.or(new SqlCondition("id > 2"));
        System.out.println(sqlCondition);

        sqlCondition.append("order by id asc");
        System.out.println(sqlCondition);

        sqlCondition.append("limit 0,10");
        System.out.println(sqlCondition);
    }

    @Test
    public void sqlInjectionTest(){
        String username = "admin";
        String password = "123456";
        if(SqlCondition.isSqlInjection(username) || SqlCondition.isSqlInjection(password)){
            System.out.println(String.format("sql injection with username: %s , password: %s",
                    username,
                    password));
        } else {
            String sql = String.format("select * from user where username='%s' and password='%s';",
                    username,
                    password);
            System.out.println(sql);
        }
    }

    @Test
    public void executeTest() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/persistence?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false";
        String username = "root";
        String password = "123456";

        HikaricpInteractor hikaricpInteractor = new HikaricpInteractor(jdbcUrl,username,password);

        /* select */
        /* select for HashMap<String,Object> list */
        List<?> userHashMapList = hikaricpInteractor.execute("select * from user;",null);
        System.out.println(userHashMapList);
        /* select for pojo list */
//        List<User> userList = (List<User>) hikaricpInteractor.execute("select * from user;",User.class);
//        System.out.println(userList);

        /* insert */
//        hikaricpInteractor.execute("insert into user (username,password) values ('wangwu','123456');",null);

        /* update */
//        hikaricpInteractor.execute("update user set password=concat(password,'*') where username='wangwu';",null);

        /* delete */
//        hikaricpInteractor.execute("delete from user where username='wangwu';",null);
    }

    @Test
    public void selectTest() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/persistence?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false";
        String username = "root";
        String password = "123456";

        HikaricpInteractor hikaricpInteractor = new HikaricpInteractor(jdbcUrl,username,password);
        SqlCondition sqlCondition = new SqlCondition("id=1");
        List<User> userList = (List<User>) hikaricpInteractor.select(User.class,sqlCondition);
        System.out.println(userList);
    }

    @Test
    public void insertTest() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/persistence?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false";
        String username = "root";
        String password = "123456";

        HikaricpInteractor hikaricpInteractor = new HikaricpInteractor(jdbcUrl,username,password);
        User user = new User();
        user.setUsername("wangwu");
        user.setPassword("123456");
        hikaricpInteractor.insert(user);
    }

    @Test
    public void deleteTest() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/persistence?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false";
        String username = "root";
        String password = "123456";

        HikaricpInteractor hikaricpInteractor = new HikaricpInteractor(jdbcUrl,username,password);
        SqlCondition sqlCondition = new SqlCondition("username = 'wangwu'");
        hikaricpInteractor.delete(User.class,sqlCondition);
    }

    @Test
    public void upateTest() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/persistence?serverTimezone=UTC&characterEncoding=UTF-8&useSSL=false";
        String username = "root";
        String password = "123456";

        HikaricpInteractor hikaricpInteractor = new HikaricpInteractor(jdbcUrl,username,password);
        User user = new User();
        user.setUsername("wangwu");
        user.setPassword("123456*");
        SqlCondition sqlCondition = new SqlCondition("username = 'wangwu'");
        hikaricpInteractor.update(user,sqlCondition);
    }

}
