package org.codejargon.fluentjdbc.internal.query;

import org.codejargon.fluentjdbc.internal.query.namedparameter.SqlAndParamsForNamed;
import org.codejargon.fluentjdbc.internal.support.Preconditions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

abstract class SingleQueryBase {
    protected final String sql;
    protected final QueryInternal query;
    protected final List<Object> params = new ArrayList<>(0);
    protected final Map<String, Object> namedParams = new HashMap<>(0);

    protected SingleQueryBase(QueryInternal query, String sql) {
        this.query = query;
        this.sql = sql;
    }

    protected void addParameter(Object param) {
      Preconditions.checkArgument(namedParams.isEmpty(), "Can not add positional parameters if named parameters are set.");
      this.params.add(param);
    }
    
    protected void addParameters(Object param1, Object param2) {
      Preconditions.checkArgument(namedParams.isEmpty(), "Can not add positional parameters if named parameters are set.");
      this.params.add(param1);
      this.params.add(param2);
    }
    
    protected void addParameters(List<?> params) {
        Preconditions.checkArgument(namedParams.isEmpty(), "Can not add positional parameters if named parameters are set.");
        this.params.addAll(params);
    }

    protected void addParameters(Object... params) {
        addParameters(Arrays.asList(params));
    }

    protected void addNamedParameters(Map<String, ?> namedParams) {
        Preconditions.checkArgument(params.isEmpty(), "Can not add named parameters if positional parameters are set.");
        this.namedParams.putAll(namedParams);
    }

    protected void addNamedParameter(String name, Object parameter) {
        Preconditions.checkArgument(params.isEmpty(), "Can not add named parameters if positional parameters are set.");
        this.namedParams.put(name, parameter);
    }

    protected <T> T runQuery(
            QueryRunnerPreparedStatement<T> queryRunnerPreparedStatement) {
        return runQuery(queryRunnerPreparedStatement, false, PreparedStatementFactory.emptyGenColumns);
    }

    protected <T> T runQueryAndFetch(
            QueryRunnerPreparedStatement<T> queryRunnerPreparedStatement,
            String[] genColumns) {
        return runQuery(queryRunnerPreparedStatement, true, genColumns);
    }

    private <T> T runQuery(
            QueryRunnerPreparedStatement<T> queryRunnerPreparedStatement,
            boolean fetchGenerated,
            String[] genColumns) {
        return query.query(connection -> {
            try (PreparedStatement ps = query.preparedStatementFactory.createSingle(connection, this, fetchGenerated, genColumns)) {
                return queryRunnerPreparedStatement.run(ps);
            }
        }, Optional.of(sql));
    }

    SqlAndParams sqlAndParams(QueryConfig config) {
        return namedParams.isEmpty() ?
                new SqlAndParams(sql, params) :
                SqlAndParamsForNamed.create(
                        config.namedTransformedSqlFactory.create(sql, namedParams),
                        namedParams
                );
    }

    abstract void customizeQuery(PreparedStatement preparedStatement, QueryConfig config) throws SQLException;
}
