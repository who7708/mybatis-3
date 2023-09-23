package org.apache.my;

import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.my.nested_query_cache.AuthorMapper;
import org.apache.my.nested_query_cache.BlogMapper;
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
            // BlogMapper mapper = session.getMapper(BlogMapper.class);
            // Blog blog = mapper.selectBlog(1);
            // System.out.println(blog);

            AuthorMapper authorMapper = session.getMapper(AuthorMapper.class);
            for (int i = 0; i < 10; i++) {
                Author author = authorMapper.selectAuthor(101);
                System.out.println(author);
            }
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
