package crypticlib.database.sql;

public abstract class SqlFieldType {

    protected final String typeName;

    public SqlFieldType(String typeName) {
        this.typeName = typeName;
    }

    public String typeName() {
        return typeName;
    }

    public String fullName() {
       return typeName;
    }

}
