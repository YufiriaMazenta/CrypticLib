package crypticlib.database.table;

import crypticlib.database.annotation.Field;
import crypticlib.database.annotation.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表元数据，描述数据库表的信息
 */
public class TableInfo {

    private static final Map<Class<?>, TableInfo> CACHE = new ConcurrentHashMap<>();

    private final Class<?> entityClass;
    private final String tableName;
    private final List<ColumnInfo> columns;
    private final ColumnInfo idColumn;
    private final List<ColumnInfo> nonIdColumns;

    public TableInfo(Class<?> entityClass, String tableName, List<ColumnInfo> columns, ColumnInfo idColumn) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.columns = columns;
        this.idColumn = idColumn;
        this.nonIdColumns = new ArrayList<>();
        for (ColumnInfo column : columns) {
            if (!column.isId()) {
                nonIdColumns.add(column);
            }
        }
    }

    /**
     * 从实体类解析表信息（带缓存）
     */
    public static TableInfo of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, TableInfo::parse);
    }

    /**
     * 解析实体类的注解信息
     */
    private static TableInfo parse(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        String tableName;
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            tableName = tableAnnotation.name();
        } else {
            tableName = entityClass.getSimpleName().toLowerCase();
        }

        List<ColumnInfo> columns = new ArrayList<>();
        ColumnInfo idColumn = null;

        // 解析所有字段（包括父类）
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                // 跳过静态和 transient 字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                // 只解析有 @Field 注解的字段
                if (field.getAnnotation(Field.class) == null) {
                    continue;
                }

                ColumnInfo columnInfo = ColumnInfo.fromField(field);
                columns.add(columnInfo);

                if (columnInfo.isId()) {
                    if (idColumn != null) {
                        throw new IllegalArgumentException("实体类 " + entityClass.getName() + " 有多个主键，只支持单主键");
                    }
                    idColumn = columnInfo;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("实体类 " + entityClass.getName() + " 没有定义任何列");
        }

        return new TableInfo(entityClass, tableName, columns, idColumn);
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public ColumnInfo getIdColumn() {
        return idColumn;
    }

    public List<ColumnInfo> getNonIdColumns() {
        return nonIdColumns;
    }

    /**
     * 根据列名查找列信息
     */
    public ColumnInfo getColumn(String columnName) {
        for (ColumnInfo column : columns) {
            if (column.getColumnName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    /**
     * 创建实体对象实例
     */
    public Object newInstance() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法创建实体类实例: " + entityClass.getName(), e);
        }
    }

}
