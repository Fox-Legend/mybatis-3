/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.Properties;

@Intercepts(
      @Signature(
              type = Executor.class,
              method="query",
              args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
              )
)
public class ExamplePlugin implements Interceptor {
  private Properties properties;

  /**
   * NOTE: 执行拦截逻辑
   * 1、通过RowBounds获取分页参数
   * 2、修改MappedStatement的BoundSql，增加limit 分页
   * 3、调用执行方法
   */
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object[] args = invocation.getArgs();
    //获取query方法中的RowBounds参数
    RowBounds rowBounds = (RowBounds) args[2];
    if (rowBounds.equals(RowBounds.DEFAULT)) {
      // 无需分页
      return invocation.proceed();
    }

    //将原 RowBounds 参数设为 RowBounds.DEFAULT，关闭 MyBatis 内置的分页机制
    args[2] = RowBounds.DEFAULT;

    //获取query方法中的MappedStatement参数和传入参数
    MappedStatement mappedStatements = (MappedStatement) args[0];
    Object params = args[1];


    BoundSql boundSql = mappedStatements.getBoundSql(params);

    //获取Sql并增加分页limit
    String sql = boundSql.getSql();
    String limit = String.format("limit %s, %s", "?", "?");
    sql = sql + " " + limit;


    //将新的sql重新塞会MappedStatement -- SqlSource
    SqlSource sqlSource = new StaticSqlSource(mappedStatements.getConfiguration(), sql, boundSql.getParameterMappings());

    MetaObject metaObject = SystemMetaObject.forObject(mappedStatements);
    metaObject.setValue("sqlSource", sqlSource);

    // 通过反射获取并设置 MappedStatement 的 sqlSource 字段
    Field field = MappedStatement.class.getDeclaredField("sqlSource");
    field.setAccessible(true);
    field.set(mappedStatements, sqlSource);

    //调用被拦截方法
    return invocation.proceed();
  }

  @Override
  public Object plugin(Object target) {
    if (target instanceof Executor) {
      return Plugin.wrap(target, this);
    }
    return target;
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public Properties getProperties() {
    return properties;
  }

}
