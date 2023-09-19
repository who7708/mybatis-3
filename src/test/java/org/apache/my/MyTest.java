package org.apache.my;

import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.nested_query_cache.BlogMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class MyTest {

    @Test
    public void testSqlSession() throws IOException {
        String resource = "org/apache/my/my-config.xml";
        InputStream resourceAsStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        // sqlSessionFactory.openSession()
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BlogMapper mapper = session.getMapper(BlogMapper.class);
            Blog blog = mapper.selectBlog(1);
            System.out.println(blog);
        }
    }

    // public void testSqlSession2() {
    //   DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
    //   TransactionFactory transactionFactory = new JdbcTransactionFactory();
    //   Environment environment = new Environment("development", transactionFactory, dataSource);
    //   Configuration configuration = new Configuration(environment);
    //   configuration.addMapper(BlogMapper.class);
    //   SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    // }
}
