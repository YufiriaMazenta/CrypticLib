package crypticlib.database.sql;

public abstract class SqlField {

    private String name;
    private SqlFieldType argumentType;

    public SqlField(SqlFieldType argumentType, String name) {
        this.argumentType = argumentType;
        this.name = name;
    }

}
