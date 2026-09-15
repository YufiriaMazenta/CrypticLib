package crypticlib.database.table;

import crypticlib.database.annotation.Field;

import java.lang.reflect.ParameterizedType;

/**
 * 列元数据，描述数据库表中一列的信息
 */
public class ColumnInfo {

    private final String columnName;
    private final Class<?> javaType;
    private final java.lang.reflect.Field field;
    private final boolean isId;
    private final boolean generated;
    private final boolean nullable;
    private final boolean unique;
    private final String defaultValue;
    private final boolean foreign;
    private final Field.ColumnType columnType;
    private final int length;

    public ColumnInfo(java.lang.reflect.Field field, String columnName, Class<?> javaType,
                      boolean isId, boolean generated, boolean nullable, boolean unique,
                      String defaultValue, boolean foreign,
                      Field.ColumnType columnType, int length) {
        this.field = field;
        this.columnName = columnName;
        this.javaType = javaType;
        this.isId = isId;
        this.generated = generated;
        this.nullable = nullable;
        this.unique = unique;
        this.defaultValue = defaultValue;
        this.foreign = foreign;
        this.columnType = columnType;
        this.length = length;
    }

    /**
     * 从字段的注解解析列信息
     */
    public static ColumnInfo fromField(java.lang.reflect.Field field) {
        field.setAccessible(true);

        Field fieldAnnotation = field.getAnnotation(Field.class);
        if (fieldAnnotation == null) {
            return null;
        }

        String columnName = fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
        boolean isId = fieldAnnotation.id();
        boolean generated = isId && fieldAnnotation.generated();
        boolean nullable = fieldAnnotation.nullable();
        boolean unique = fieldAnnotation.unique();
        String defaultValue = fieldAnnotation.defaultValue();
        boolean foreign = fieldAnnotation.foreign();
        Field.ColumnType columnType = fieldAnnotation.type();
        int length = fieldAnnotation.length();

        if (length < 0) {
            throw new IllegalArgumentException("@Field.length of field " + field.getName() + " must not be negative");
        }
        if (length > 0 && columnType != Field.ColumnType.AUTO && columnType != Field.ColumnType.VARCHAR) {
            throw new IllegalArgumentException("@Field.length of field " + field.getName()
                + " is only supported for VARCHAR columns, but the declared type is " + columnType);
        }

        // 确定 Java 类型
        Class<?> javaType = field.getType();

        return new ColumnInfo(field, columnName, javaType, isId, generated, nullable, unique, defaultValue, foreign,
            columnType, length);
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public java.lang.reflect.Field getField() {
        return field;
    }

    public boolean isId() {
        return isId;
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isForeign() {
        return foreign;
    }

    /**
     * 列类型，AUTO 表示按 Java 类型自动识别
     */
    public Field.ColumnType getColumnType() {
        return columnType;
    }

    /**
     * 列长度，0 表示使用默认长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 从实体对象中获取该列的值
     */
    public Object getValue(Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read field value: " + field.getName(), e);
        }
    }

    /**
     * 设置实体对象中该列的值
     */
    public void setValue(Object entity, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set field value: " + field.getName(), e);
        }
    }

}
