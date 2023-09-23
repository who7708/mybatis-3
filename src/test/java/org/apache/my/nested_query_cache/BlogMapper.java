package org.apache.my.nested_query_cache;

import org.apache.ibatis.domain.blog.Blog;

public interface BlogMapper {

  Blog selectBlog(int id);

  Blog selectBlogUsingConstructor(int id);

}
