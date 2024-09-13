package crypticlib.database.sql;

import java.util.List;

public abstract class SqlTable {

    private String tableName;
    private List<SqlField> arguments;

    public SqlTable(String tableName, List<SqlField> arguments) {
        this.tableName = tableName;
        this.arguments = arguments;
    }

    public abstract String toSql();

}
