package org.apache.my;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.typehandler.StringTrimmingTypeHandler;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.After;
import org.junit.Before;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Chris
 * @version 1.0.0
 * @since 2023/12/07
 */
public class TestBase {
  SqlSession sqlSession = null;

  public static UnpooledDataSource createUnpooledDataSource(String resource) throws IOException {
    // Properties props = Resources.getResourceAsProperties(resource);
    Properties props = getProperties(resource);
    UnpooledDataSource ds = new UnpooledDataSource();
    ds.setDriver(props.getProperty("driver"));
    ds.setUrl(props.getProperty("url"));
    ds.setUsername(props.getProperty("username"));
    ds.setPassword(props.getProperty("password"));
    return ds;
  }

  public static PooledDataSource createPooledDataSource(String resource) throws IOException {
    // Properties props = Resources.getResourceAsProperties(resource);
    Properties props = getProperties(resource);
    PooledDataSource ds = new PooledDataSource();
    ds.setDriver(props.getProperty("driver"));
    ds.setUrl(props.getProperty("url"));
    ds.setUsername(props.getProperty("username"));
    ds.setPassword(props.getProperty("password"));
    return ds;
  }

  private static Properties getProperties(String resource) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(resource);
    Properties props = new Properties();
    props.load(fileInputStream);
    return props;
  }

  @Before
  public void init() throws IOException {
    PooledDataSource dataSource = createPooledDataSource("D:\\data\\db.properties");
    // environments
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);

    // configuration
    Configuration configuration = new Configuration(environment);
    // // properties
    // Properties props = getProperties("D:\\data\\db.properties");
    // configuration.setVariables(props);

    // // settings
    // configuration.setLazyLoadingEnabled(true);
    // // to test legacy style reference (#{0} #{1})
    // configuration.setUseActualParamName(false);
    configuration.setCacheEnabled(false);
    // configuration.setMultipleResultSetsEnabled(true);
    // configuration.setUseColumnLabel(true);
    // configuration.setUseGeneratedKeys(false);
    // configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
    // configuration.setDefaultStatementTimeout(25);
    configuration.setLogImpl(StdOutImpl.class);

    // typeAliases
    // configuration.getTypeAliasRegistry().registerAlias(Blog.class);
    // configuration.getTypeAliasRegistry().registerAlias(Post.class);
    // configuration.getTypeAliasRegistry().registerAlias(Author.class);
    configuration.getTypeAliasRegistry().registerAliases("org.apache.ibatis.domain.blog");

    // typeHandlers
    // configuration.getTypeHandlerRegistry().register("");
    configuration.getTypeHandlerRegistry().register(String.class, JdbcType.CHAR, StringTrimmingTypeHandler.class);
    configuration.getTypeHandlerRegistry().register(String.class, JdbcType.VARCHAR, StringTrimmingTypeHandler.class);

    // plugins
    // configuration.getInterceptors().add()
    // configuration.getInterceptors().add(new MybatisInterceptor())
    MybatisInterceptor mybatisInterceptor = new MybatisInterceptor();
    // mybatisInterceptor.setProperties();
    configuration.addInterceptor(mybatisInterceptor);

    // mappers
    // configuration.addMapper(BoundBlogMapper.class);
    // configuration.addMapper(BoundAuthorMapper.class);
    configuration.addMappers("org.apache.ibatis.domain.blog.mappers");
    configuration.addMappers("org.apache.my.nested_query_cache");

    // SqlSession
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    sqlSession = sqlSessionFactory.openSession();
  }

  @After
  public void destroy() {
    if (sqlSession != null) {
      sqlSession.close();
    }
  }

}
