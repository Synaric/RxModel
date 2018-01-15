package com.synaric.app.rxmodel.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.synaric.app.rxmodel.converter.Converter;
import com.synaric.app.rxmodel.filter.Filter;
import com.synaric.app.rxmodel.filter.EmptyFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL数据库工具类。
 * Created by Synaric-雍高超 on 2016/8/24 0024.
 */
@SuppressWarnings("unused")
public class SqlUtils {

    /**
     * 定义SQL操作。
     */
    public static class Operations {
        public static final String INSERT = "insert";
        public static final String INSERT_OR_REPLACE = "insert or replace";
        public static final String INSERT_OR_IGNORE = "insert or ignore";
    }

    public static <T> String ofInsert(String op, String tableName, Converter<T> converter, T t) {
        switch (op.toLowerCase()) {
            case Operations.INSERT:
                return SqlUtils.createInsert(tableName, converter, t);
            case Operations.INSERT_OR_REPLACE:
                return SqlUtils.createInsertOrReplace(tableName, converter, t);
            case Operations.INSERT_OR_IGNORE:
                return SqlUtils.createInsertOrIgnore(tableName, converter, t);
            default:
                return null;
        }
    }

    public static <T> String ofBlankInsert(String op, String tableName, int count) {
        switch (op.toLowerCase()) {
            case Operations.INSERT:
                return SqlUtils.createBlankInsert(tableName, count);
            case Operations.INSERT_OR_REPLACE:
                return SqlUtils.createBlankInsertOrReplace(tableName, count);
            case Operations.INSERT_OR_IGNORE:
                return SqlUtils.createBlankInsertOrIgnore(tableName, count);
            default:
                return null;
        }
    }

    /**
     * 生成表创建语句。如果表已经创建，则什么也不做。
     */
    public static  String generateCreateTableSQL(String tableName) {
        return "create table if not exists " + tableName + " (_id VARCHAR primary key, value VARCHAR)";
    }

    /**
     * 生成INSERT语句。
     */
    public static String createInsert(String tableName, String[] values) {
        return createInsertInternal(Operations.INSERT, tableName, values, false);
    }

    /**
     * 生成INSERT语句。
     */
    public static <T> String createInsert(String tableName, Converter<T> converter, T t) {
        return createInsert(tableName, converter, t, false);
    }

    /**
     * 生成INSERT语句。
     */
    public static <T> String createInsert(String tableName, Converter<T> converter, T t, boolean blank) {
        String document = converter.convertToDocument(t);
        String id = converter.bindID(t);
        return createInsertInternal(Operations.INSERT, tableName, new String[]{id, document}, blank);
    }

    /**
     * 生成INSERT语句，插入项目用"?"代替。
     * @param colunmCount 要插入的列数。
     */
    public static String createBlankInsert(String tableName, int colunmCount) {
        if(colunmCount <= 0) throw new IllegalArgumentException("colunmCount must > 0.");
        return createInsertInternal(Operations.INSERT, tableName, new String[colunmCount], true);
    }

    /**
     * 生成INSERT OR REPLACE语句。
     */
    public static <T> String createInsertOrReplace(String tableName, Converter<T> converter, T t) {
        return createInsertOrReplace(tableName, converter, t, false);
    }

    /**
     * 生成INSERT OR REPLACE语句，插入项目用"?"代替。
     * @param colunmCount colunmCount 要插入的列数.
     */
    public static String createBlankInsertOrReplace(String tableName, int colunmCount) {
        if(colunmCount <= 0) throw new IllegalArgumentException("colunmCount must > 0.");
        return createInsertInternal(Operations.INSERT_OR_REPLACE, tableName, new String[colunmCount], true);
    }

    /**
     * 生成INSERT OR REPLACE语句。
     */
    public static <T> String createInsertOrReplace(String tableName, Converter<T> converter, T t, boolean blank) {
        String document = converter.convertToDocument(t);
        String id = converter.bindID(t);
        return createInsertInternal(Operations.INSERT_OR_REPLACE, tableName, new String[]{id, document}, blank);
    }

    /**
     * 生成INSERT OR REPLACE语句。
     */
    public static String createInsertOrReplace(String tableName, String[] values) {
        return createInsertInternal(Operations.INSERT_OR_REPLACE, tableName, values, false);
    }

    /**
     * 生成INSERT OR IGNORE语句。
     */
    public static <T> String createInsertOrIgnore(String tableName, Converter<T> converter, T t) {
        return createInsertOrIgnore(tableName, converter, t, false);
    }

    /**
     * 生成INSERT OR IGNORE语句。
     */
    public static String createInsertOrIgnore(String tableName, String[] values) {
        return createInsertInternal(Operations.INSERT_OR_IGNORE, tableName, values, false);
    }

    /**
     * 生成INSERT OR IGNORE语句，插入项目用"?"代替。
     * @param colunmCount colunmCount 要插入的列数.
     */
    public static String createBlankInsertOrIgnore(String tableName, int colunmCount) {
        if(colunmCount <= 0) throw new IllegalArgumentException("colunmCount must > 0.");
        return createInsertInternal(Operations.INSERT_OR_IGNORE, tableName, new String[colunmCount], true);
    }

    /**
     * 生成INSERT OR IGNORE语句。
     */
    public static <T> String createInsertOrIgnore(String tableName, Converter<T> converter, T t, boolean blank) {
        String document = converter.convertToDocument(t);
        String id = converter.bindID(t);
        return createInsertInternal(Operations.INSERT_OR_IGNORE, tableName, new String[]{id, document}, blank);
    }

    /**
     * 生成SELECT语句查询所有。
     */
    public static String createSelectAll(String tableName) {
        return "select * from " + tableName;
    }


    /**
     * 生成根据id删除的DELETE语句。
     */
    public static String createBlankDeleteById(String tableName) {
        return "delete from " + tableName + " where _id = ?";
    }

    /**
     * 读取特定格式的指定索引范围的SQL数据（第一项为_id，第二项为value），读取完成后关闭cursor。
     * @param cursor 结果指针。
     * @param converter 文档模型-对象模型转换器。
     * @param filter 筛选器，筛选出满足特定条件都额数据。
     * @param result 结果集。
     */
    public static <T> void readDocumentsFromCursor(Cursor cursor,
                                                   Converter<T> converter,
                                                   Filter<T> filter,
                                                   List<T> result) {
        if(result == null) result = new ArrayList<>();
        if(cursor == null) return;
        if(filter == null) filter = new EmptyFilter<>();
        try {

            int index = 0;
            int collected = 0;
            while(cursor.moveToNext()) {
                String value = cursor.getString(1);//表结构： | _id | value |
                T t = converter.convertToObject(value);
                if(filter.doFilter(t, index, collected)){
                    result.add(t);
                    ++collected;
                }
                if(filter.isTerminated()) break;
                ++index;
            }

        } finally {
            cursor.close();
        }
    }

    /**
     * 检查指定数据库是否为空。
     * @param db 需要检查的数据库。
     * @return 是否为空。
     */
    public static boolean assertDbNull(SQLiteDatabase db) {
        return db == null;
    }

    private static String createInsertInternal(String operation, String tableName, String[] values, boolean blank) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(operation)
                .append(" into ")
                .append(tableName)
                .append(" values");
        for (int i = 0; i < values.length; i++) {
            if(blank) {
                sBuilder.append(i == 0 ? "(" : ", ")
                        .append("?");
            }else {
                sBuilder.append(i == 0 ? "(\'" : ", \'")
                        .append(values[i])
                        .append("\'");
            }
        }
        sBuilder.append(")");
        return sBuilder.toString();
    }
}
