package org.apache.my;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * 数据隔离
 *
 * @author Chris
 * @date 2023/10/13
 */
@Intercepts({
        // Signature 与 executor 的方法对应
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MybatisInterceptor implements Interceptor {

    // 环境隔离字段,0-非正式(默认), 1-正式
    private static final String ENV_COLUMN = "profile_type";

    private static final String ENV = "0";

    private static final Set<String> FILTER_DAO = new HashSet<>();

    // 1. 过滤不需要处理的 sql
    // 2. 如何在启动时将 env 传入进来
    // 3.
    static {
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // invocation.getArgs() 对应的就是下面这个里的属性 args
        // @Signature(type = Executor.class, method = "query", args = ...
        final Object[] args = invocation.getArgs();
        if (args.length <= 0 || !(args[0] instanceof MappedStatement)) {
            // 返回，继续执行
            return invocation.proceed();
        }
        MappedStatement ms = (MappedStatement) args[0];
        // args[1], 则对应的是 org.apache.ibatis.plugin.Signature.args 属性的第二个参数 object，即方法入参
        Object parameterObject = args[1];
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        // 获取sql
        String sql = boundSql.getSql();
        // 是否需要处理
        // id = a.b.c.xxxDAO.selectByPrimaryKey
        if (!this.needProcessDao(ms.getId()) || StringUtils.isBlank(sql)) {
            return invocation.proceed();
        }

        // 包装sql后，重置到invocation中
        this.processSql(invocation, sql);

        // 返回，继续执行
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object obj) {
        return Plugin.wrap(obj, this);
    }

    @Override
    public void setProperties(Properties arg0) {
        // doSomething
    }

    /** 此 dao 是否需要处理 */
    private boolean needProcessDao(String id) {
        for (String search : FILTER_DAO) {
            if (StringUtils.contains(id, search)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 包装sql后，重置到invocation中
     *
     * @param invocation
     * @param sql
     * @throws SQLException
     */
    private void processSql(Invocation invocation, String sql) throws SQLException {
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        MappedStatement newStatement = this.newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
        this.processSql(newStatement, sql);
        args[0] = newStatement;
    }

    /** 处理 sql 语句 */
    private void processSql(MappedStatement newStatement, String sql) {
        MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        // 依次处理 INSERT, DELETE, UPDATE, SELECT 语句
        if (SqlCommandType.INSERT == newStatement.getSqlCommandType()) {
            sql = this.processInsertSql(sql);
        } else if (SqlCommandType.DELETE == newStatement.getSqlCommandType()) {
            sql = this.processDeleteSql(sql);
        } else if (SqlCommandType.UPDATE == newStatement.getSqlCommandType()) {
            sql = this.processUpdateSql(sql);
        } else if (SqlCommandType.SELECT == newStatement.getSqlCommandType()) {
            sql = this.processSelectSql(sql);
        }/* else {

        }*/
        msObject.setValue("sqlSource.boundSql.sql", sql);
        // msObject.setValue("boundSql.sql", sql);
    }

    /**
     * 解析 insert 语句
     */
    private String processInsertSql(String sql) {
        try {
            // 此处的运行时类是Insert
            Insert statement = (Insert) CCJSqlParserUtil.parse(sql);
            System.out.printf("processInsertSql before %s \n", statement.toString());
            //添加列
            statement.getColumns().add(new Column(ENV_COLUMN));
            ExpressionList expressionList = (ExpressionList) statement.getWithItemsList();
            expressionList.getExpressions().add(new StringValue(ENV));

            sql = statement.toString();
            System.out.printf("processInsertSql after %s \n", sql);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("processInsertSql parse error, sql %s \n", sql);
        }
        return sql;
    }

    /**
     * 解析 delete 语句
     */
    private String processDeleteSql(String sql) {
        try {
            Delete statement = (Delete) CCJSqlParserUtil.parse(sql);
            System.out.printf("processDeleteSql before %s \n", statement.toString());
            Expression where = statement.getWhere();
            Expression whereExpression = this.processWhereExpression(where);
            statement.setWhere(whereExpression);

            sql = statement.toString();
            System.out.printf("processDeleteSql after %s \n", sql);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("processDeleteSql parse error");
        }
        return sql;
    }

    /**
     * 解析 update 语句
     */
    private String processUpdateSql(String sql) {
        try {
            Update statement = (Update) CCJSqlParserUtil.parse(sql);
            System.out.printf("processUpdateSql before %s \n", statement.toString());
            Expression where = statement.getWhere();
            Expression whereExpression = this.processWhereExpression(where);
            statement.setWhere(whereExpression);

            sql = statement.toString();
            System.out.printf("processUpdateSql after %s \n", sql);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("processUpdateSql parse error \n");
        }
        return sql;
    }

    /**
     * 解析 select 语句
     *
     * @param sql 原始 sql
     * @return 解析后的 sql
     */
    private String processSelectSql(String sql) {
        try {
            Select statement = (Select) CCJSqlParserUtil.parse(sql);
            System.out.printf("processSelectSql before %s \n", statement.toString());

            PlainSelect plainSelect = (PlainSelect) statement.getSelectBody();
            // 原where表达式
            Expression where = plainSelect.getWhere();
            Expression whereExpression = this.processWhereExpression(where);
            plainSelect.setWhere(whereExpression);

            sql = statement.toString();
            System.out.printf("processSelectSql after %s \n", sql);
        } catch (JSQLParserException e) {
            e.printStackTrace();
            System.out.printf("processSelectSql parse error");
        }
        return sql;
    }

    /**
     * where 条件
     *
     * @param where 原条件
     * @return 处理后的条件
     */
    private Expression processWhereExpression(Expression where) {
        // 新增的条件表达式
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(ENV_COLUMN));
        equalsTo.setRightExpression(new StringValue(ENV));

        // 如果没有 where 条件, 直接设置 app_env
        if (Objects.isNull(where)) {
            return equalsTo;
        } else {
            // 用and链接条件, 将 app_env 作为第一个 where 条件
            return new AndExpression(equalsTo, where);
        }
    }

    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder =
                new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    // private String getOperateType(Invocation invocation) {
    //     final Object[] args = invocation.getArgs();
    //     MappedStatement ms = (MappedStatement) args[0];
    //     SqlCommandType commondType = ms.getSqlCommandType();
    //     if (commondType.compareTo(SqlCommandType.SELECT) == 0) {
    //         return "select";
    //     }
    //     if (commondType.compareTo(SqlCommandType.INSERT) == 0) {
    //         return "insert";
    //     }
    //     if (commondType.compareTo(SqlCommandType.UPDATE) == 0) {
    //         return "update";
    //     }
    //     if (commondType.compareTo(SqlCommandType.DELETE) == 0) {
    //         return "delete";
    //     }
    //     return null;
    // }

    /** 定义一个内部辅助类，作用是包装sq */
    private static class BoundSqlSqlSource implements SqlSource {

        private final BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}

