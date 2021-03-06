package scott.data;

import java.io.File;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

import scott.barleydb.api.core.Environment;
import scott.barleydb.api.core.entity.EntityContext;
import scott.barleydb.bootstrap.EnvironmentDef;


public class AccountingDefinitions {

    private static boolean init = false;
    private static Environment env = null;

    public static EntityContext newContext() {
        try {
            init();
        }
        catch (Exception x) {
            throw new IllegalStateException("Could not initialize environment", x);
        }
        return new EntityContext(env, "scott.data");
    }

    public static synchronized void init() throws Exception {
        if (init) return;

        String dbPath = System.getProperty("databasePath");
        //we expect the database to be stored in it's own directory
        File dbDir = new File(dbPath).getParentFile();
        boolean newDb = true;
        if (dbDir.exists() && dbDir.listFiles().length > 0) {
            newDb = false;
        }

        EnvironmentDef envDef = EnvironmentDef.build()
            .withDataSource()
                .withUrl("jdbc:hsqldb:file:" + dbPath)
                .withDriver("org.hsqldb.jdbcDriver")
                .withUser("sa")
                .withPassword("")
            .end()
            .withSpecs(AccountingSpec.class)
            .withSchemaCreation(newDb);

        env = envDef.create();

        if (newDb) {
            SimpleJdbcTestUtils.executeSqlScript(new SimpleJdbcTemplate(envDef.getDataSource()), new ClassPathResource("/testdata.dml"), false);
        }

        /*
        DriverManagerDataSource dmDataSource = new DriverManagerDataSource();
        dmDataSource.setDriverClassName( "org.hsqldb.jdbcDriver");
        //dmDataSource.setUrl( "jdbc:hsqldb:mem:testdb");
        //dmDataSource.setUrl( "jdbc:hsqldb:file:/home/scott/accounts/database" );
        dmDataSource.setUrl( "jdbc:hsqldb:file:" + dbPath );
        //dmDataSource.setUrl( "jdbc:hsqldb:mem:testdb;close_result=true;hsqldb.applog=3;hsqldb.sqllog=3");
        dmDataSource.setUsername( "sa");
        dmDataSource.setPassword( "");
*/
//        if (newDb) {
//            SimpleJdbcTestUtils.executeSqlScript(new SimpleJdbcTemplate(dmDataSource), new ClassPathResource("/schema.sql"), false);
//            SimpleJdbcTestUtils.executeSqlScript(new SimpleJdbcTemplate(dmDataSource), new ClassPathResource("/testdata.dml"), false);
//        }
//
//        JdbcEntityContextServices services = new JdbcEntityContextServices( dmDataSource );
//        env = new Environment(services);
//        services.setEnvironment(env);
//        services.setSequenceGenerator(new QuickHackSequenceGenerator(env));
//
//        env.addDefinitions(Definitions.start("scott.data")
//                .newEntity(Account.class, "SS_ACCOUNT")
//                    .withKey("id", JavaType.LONG, "ID", JdbcType.BIGINT)
//                    .withValue("name", JavaType.STRING, "NAME", JdbcType.VARCHAR)
//
//                .newEntity(Transaction.class, "SS_TRANSACTION")
//                    .withKey("id", JavaType.LONG, "ID", JdbcType.BIGINT)
//                    .withValue("date", JavaType.UTIL_DATE, "DATE", JdbcType.TIMESTAMP)
//                    .withValue("amount", JavaType.BIGDECIMAL, "AMOUNT", JdbcType.DECIMAL)
//                    .withValue("comment", JavaType.STRING, "COMMENT", JdbcType.VARCHAR)
//                    .withValue("important", JavaType.BOOLEAN, "IMPORTANT", JdbcType.INT)
//                    .withOne("account", Account.class, "ACCOUNT_ID", JdbcType.BIGINT)
//                    .withOne("category", Category.class, "CATEGORY_ID", JdbcType.BIGINT)
//
//                .newEntity(Category.class, "SS_CATEGORY")
//                    .withKey("id", JavaType.LONG, "ID", JdbcType.BIGINT)
//                    .withValue("name", JavaType.STRING, "NAME", JdbcType.VARCHAR)
//                    .withValue("monthlyLimit", JavaType.INTEGER, "MONTH_LIMIT", JdbcType.INT)
//
//                .newEntity(Month.class, "SS_MONTH")
//                    .withKey("id", JavaType.LONG, "ID", JdbcType.BIGINT)
//                    .withValue("starting", JavaType.UTIL_DATE, "STARTING", JdbcType.DATE)
//                    .withValue("startingBalance", JavaType.BIGDECIMAL, "STARTING_BAL", JdbcType.DECIMAL)
//                    .withValue("finished", JavaType.BOOLEAN, "FINISHED", JdbcType.INT)
//
//                .complete()
//                );
//
//          env.getDefinitions("scott.data").registerQueries(new QAccount(), new QTransaction(), new QCategory(), new QMonth());
          init = true;
    }

}

