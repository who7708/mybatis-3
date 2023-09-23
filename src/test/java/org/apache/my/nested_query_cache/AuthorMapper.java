package org.apache.my.nested_query_cache;

import org.apache.ibatis.domain.blog.Author;

public interface AuthorMapper {

  Author selectAuthor(int id);

}
