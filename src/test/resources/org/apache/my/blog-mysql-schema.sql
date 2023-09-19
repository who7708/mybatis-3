DROP TABLE if exists comment;
DROP TABLE if exists post_tag;
DROP TABLE if exists tag;
DROP TABLE if exists post;
DROP TABLE if exists blog;
DROP TABLE if exists author;
DROP PROCEDURE if exists selectTwoSetsOfAuthors;
DROP PROCEDURE if exists insertAuthor;
DROP PROCEDURE if exists selectAuthorViaOutParams;

CREATE TABLE if not exists author
(
    id                INT          NOT NULL auto_increment,
    username          VARCHAR(255) NOT NULL,
    password          VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL,
    bio               blob,
    favourite_section VARCHAR(25),
    PRIMARY KEY (id)
);

CREATE TABLE if not exists blog
(
    id        INT NOT NULL auto_increment,
    author_id INT NOT NULL,
    title     VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE if not exists post
(
    id         INT          NOT NULL auto_increment,
    blog_id    INT,
    author_id  INT          NOT NULL,
    created_on TIMESTAMP    NOT NULL,
    section    VARCHAR(25)  NOT NULL,
    subject    VARCHAR(255) NOT NULL,
    body       blob         NOT NULL,
    draft      INT          NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (blog_id) REFERENCES blog (id)
);

CREATE TABLE if not exists tag
(
    id   INT          NOT NULL auto_increment,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists post_tag
(
    post_id INT NOT NULL,
    tag_id  INT NOT NULL,
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE if not exists comment
(
    id      INT NOT NULL auto_increment,
    post_id INT NOT NULL,
    name    LONG VARCHAR NOT NULL,
    comment LONG VARCHAR NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists node
(
    id        INT NOT NULL,
    parent_id INT,
    PRIMARY KEY (id)
);

# CREATE PROCEDURE selectTwoSetsOfAuthors(DP1 INTEGER, DP2 INTEGER) PARAMETER STYLE JAVA
# LANGUAGE JAVA
# READS SQL DATA
# DYNAMIC RESULT SETS 2
# EXTERNAL NAME 'org.apache.ibatis.databases.blog.StoredProcedures.selectTwoSetsOfTwoAuthors';
#
# CREATE PROCEDURE insertAuthor(DP1 INTEGER, DP2 VARCHAR(255), DP3 VARCHAR(255), DP4 VARCHAR(255)) PARAMETER STYLE JAVA
# LANGUAGE JAVA
# EXTERNAL NAME 'org.apache.ibatis.databases.blog.StoredProcedures.insertAuthor';
#
# CREATE PROCEDURE selectAuthorViaOutParams(ID INTEGER, OUT USERNAME VARCHAR(255), OUT PASSWORD VARCHAR(255),
#                                           OUT EMAIL VARCHAR(255), OUT BIO VARCHAR(255)) PARAMETER STYLE JAVA
# LANGUAGE JAVA
# EXTERNAL NAME 'org.apache.ibatis.databases.blog.StoredProcedures.selectAuthorViaOutParams';
