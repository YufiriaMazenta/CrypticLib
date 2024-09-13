package crypticlib.database.sql;

import org.jetbrains.annotations.Nullable;

public abstract class VariableFieldType extends SqlFieldType {

    protected Integer length;

    public VariableFieldType(String typeName, @Nullable Integer length) {
        super(typeName);
        this.length = length;
    }

    public @Nullable Integer length() {
        return length;
    }

    public VariableFieldType setLength(@Nullable Integer length) {
        this.length = length;
        return this;
    }

    @Override
    public String fullName() {
        if (length == null)
            return typeName;
        else
            return typeName + "(" + length + ")";
    }
}
