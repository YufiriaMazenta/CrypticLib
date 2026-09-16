package crypticlib.database.table;

import crypticlib.database.annotation.Field;
import crypticlib.database.annotation.Table;
import crypticlib.util.ReflectionHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 表元数据，描述数据库表的信息
 */
public class TableInfo {

    /**
     * 以 ClassValue 缓存解析结果：值随 Class 一起被回收，不会在插件卸载后继续持有插件类加载器
     */
    private static final ClassValue<TableInfo> CACHE = new ClassValue<TableInfo>() {
        @Override
        protected TableInfo computeValue(Class<?> type) {
            return parse(type);
        }
    };

    private final Class<?> entityClass;
    private final String tableName;
    private final List<ColumnInfo> columns;
    private final ColumnInfo idColumn;
    private final List<ColumnInfo> nonIdColumns;
    private final List<ColumnInfo> insertColumns;

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
        // 自增主键由数据库生成，插入时不能写入；非自增主键是调用方赋的值，必须随插入一起写入
        this.insertColumns = idColumn.isGenerated() ? nonIdColumns : columns;
    }

    /**
     * 从实体类解析表信息（带缓存）
     */
    public static TableInfo of(Class<?> entityClass) {
        return CACHE.get(entityClass);
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
                if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isTransient(field.getModifiers())) {
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
                        throw new IllegalArgumentException("Entity class " + entityClass.getName()
                            + " declares multiple primary keys, only a single primary key is supported");
                    }
                    idColumn = columnInfo;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Entity class " + entityClass.getName() + " does not declare any column");
        }

        if (idColumn == null) {
            throw new IllegalArgumentException("Entity class " + entityClass.getName()
                + " has no primary key, mark one field with @Field(id = true)");
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
     * 插入语句要写入的列：自增主键由数据库生成，需要排除；非自增主键由调用方赋值，必须包含。
     * <p>
     * 方言生成 INSERT 语句与 DAO 绑定插入参数必须使用同一份列集合，否则会出现主键未被写入的问题。
     */
    public List<ColumnInfo> getInsertColumns() {
        return insertColumns;
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
     * 根据列名查找列信息，找不到时抛出带可选列名的异常
     * <p>
     * 构建器里的列名是手写字符串，拼错时在构建期直接报错，好过运行时由数据库抛语法错误
     */
    public ColumnInfo requireColumn(String columnName) {
        ColumnInfo column = getColumn(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Unknown column \"" + columnName + "\" of table \"" + tableName
                + "\", available columns: " + getColumnNames());
        }
        return column;
    }

    /**
     * 所有列名，用于异常提示
     */
    public String getColumnNames() {
        StringBuilder builder = new StringBuilder();
        for (ColumnInfo column : columns) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(column.getColumnName());
        }
        return builder.toString();
    }

    /**
     * 创建实体对象实例
     * <p>
     * 走 {@link ReflectionHelper} 的 MethodHandle 路径，构造器句柄由工具类缓存，避免每次查询都重新查找构造器；
     * 同时允许实体类与其无参构造器不是 public（例如静态内部类）
     */
    public Object newInstance() {
        try {
            return ReflectionHelper.getDeclaredConstructor(entityClass).invoke();
        } catch (Throwable e) {
            throw new RuntimeException("Cannot instantiate entity class: " + entityClass.getName(), e);
        }
    }

}
