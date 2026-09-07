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

    public ColumnInfo(java.lang.reflect.Field field, String columnName, Class<?> javaType,
                      boolean isId, boolean generated, boolean nullable, boolean unique,
                      String defaultValue, boolean foreign) {
        this.field = field;
        this.columnName = columnName;
        this.javaType = javaType;
        this.isId = isId;
        this.generated = generated;
        this.nullable = nullable;
        this.unique = unique;
        this.defaultValue = defaultValue;
        this.foreign = foreign;
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

        // 确定 Java 类型
        Class<?> javaType = field.getType();

        return new ColumnInfo(field, columnName, javaType, isId, generated, nullable, unique, defaultValue, foreign);
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
     * 从实体对象中获取该列的值
     */
    public Object getValue(Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法读取字段值: " + field.getName(), e);
        }
    }

    /**
     * 设置实体对象中该列的值
     */
    public void setValue(Object entity, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法设置字段值: " + field.getName(), e);
        }
    }

}
