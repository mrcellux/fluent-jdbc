package org.codejargon.fluentjdbc.internal.query;

import java.util.Optional;

import org.codejargon.fluentjdbc.api.query.inspection.DatabaseInspection;
import org.codejargon.fluentjdbc.api.query.inspection.MetaDataAccess;
import org.codejargon.fluentjdbc.api.query.inspection.MetaDataResultSet;
import org.codejargon.fluentjdbc.api.query.inspection.MetaDataSelect;

class DatabaseInspectionInternal implements DatabaseInspection {
    private final QueryInternal query;

    DatabaseInspectionInternal(QueryInternal query) {
        this.query = query;
    }

    @Override
    public <T> T accessMetaData(MetaDataAccess<T> access) {
        return query.query(
                connection -> access.access(connection.getMetaData()),
                Optional.of(QueryInfoInternal.of("JDBC Database Inspection")),
                query.config.defaultSqlErrorHandler.get()
        );
    }

    @Override
    public MetaDataSelect selectFromMetaData(MetaDataResultSet select) {
        return new MetaDataSelectInternal(query, select);
    }
}
