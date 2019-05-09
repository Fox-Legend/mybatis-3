package org.apache.ibatis.builder;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.assertj.core.util.Lists;

import java.sql.PreparedStatement;
import java.util.*;

/**
 * @author huoyu.
 * @date 2019/4/23.
 */
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {
                PreparedStatement.class
        })
})
public class ParameterPlugin implements Interceptor {

    private static int MAPPEDSTATEMENT_INDEX = 0;

    private final static List<String> EN_WORDS = Lists.newArrayList("name");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
        Object parameterObject = metaObject.getValue("parameterObject");
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        if(parameterObject instanceof Map) {

        } else if (parameterObject instanceof Object) {

        }
        List<Integer> encryptIndex = new ArrayList<>();
        int startIndex = 0;

        for (ParameterMapping mapping : parameterMappings) {
            String propertyName = mapping.getProperty();
            if (EN_WORDS.contains(propertyName)) {
                encryptIndex.add(startIndex);
            }
            startIndex++;
        }

        metaObject.setValue("parameterObject", parameterObject);

        return invocation.proceed();
    }



    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
