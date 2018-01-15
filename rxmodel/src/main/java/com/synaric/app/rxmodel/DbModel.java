package com.synaric.app.rxmodel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.synaric.app.rxmodel.converter.Converter;
import com.synaric.app.rxmodel.converter.JsonConverter;
import com.synaric.app.rxmodel.filter.EmptyFilter;
import com.synaric.app.rxmodel.filter.Filter;
import com.synaric.app.rxmodel.utils.ReflectUtils;
import com.synaric.app.rxmodel.utils.RxUtils;
import com.synaric.app.rxmodel.utils.SqlUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import rx.Observable;
import rx.functions.Func1;

/**
 * 操作数据库的Model。
 * Created by Synaric on 2016/8/20 0020.
 */
@SuppressWarnings("unused")
public abstract class DbModel<T> {

    private RxModel rxModel;
    private String tableName;
    private Converter<T> converter;
    private ReadWriteLock lock;
    private final Class<T> clz;

    @SuppressWarnings("unchecked")
    public DbModel(RxModel rxModel, String table) {
        this.rxModel = rxModel;
        clz = (Class<T>) ReflectUtils.getActualClass(this.getClass(), 0);
        tableName = TextUtils.isEmpty(table) ? clz.getSimpleName() : table;
        converter = new JsonConverter<T>(clz) {
            @Override
            public String bindID(T t) {
                return DbModel.this.bindID(t);
            }
        };

        lock = rxModel.getGlobalLock();
        createTableIfNotExists(tableName);
    }

    @SuppressWarnings("unchecked")
    public DbModel(RxModel rxModel) {
        this(rxModel, null);
    }

    /**
     * 确定文档模型的主键，这个主键作为文档模型的唯一标识。
     * @param t 对象模型。
     * @return ID值。
     */
    public abstract String bindID(T t);

    /**
     * 插入一个对象模型。一类对象模型对应一个表，表名为类名，因此包名不同但类名相同的对象模型将会插入到同一个表中。
     * 如果对象模型对应的表没有创建，将会首先创建表，再插入数据。
     * 插入逻辑遵循SQL的INSERT逻辑。
     * @param t 要插入的对象模型。
     * @return 插入结果。
     */
    public Observable<Boolean> insert(final T t) {
        return RxUtils.makeModelObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return insertInternal(t, SqlUtils.Operations.INSERT);
            }
        });
    }

    /**
     * 插入一个对象模型。一类对象模型对应一个表，表名为类名，因此包名不同但类名相同的对象模型将会插入到同一个表中。
     * 如果对象模型对应的表没有创建，将会首先创建表，再插入数据。
     * 如果已经存在相同id的文档模型，将会覆盖。
     * 底层插入逻辑遵循SQL的INSERT OR REPLACE逻辑。
     * @param t 要插入的对象模型。
     * @return 插入结果。
     */
    public Observable<Boolean> save(final T t) {
        return RxUtils.makeModelObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return insertInternal(t, SqlUtils.Operations.INSERT_OR_REPLACE);
            }
        });
    }

    /**
     * 同步插入一个对象模型。一类对象模型对应一个表，表名为类名，因此包名不同但类名相同的对象模型将会插入到同一个表中。
     * 如果对象模型对应的表没有创建，将会首先创建表，再插入数据。
     * 如果已经存在相同id的文档模型，将会覆盖。
     * 底层插入逻辑遵循SQL的INSERT OR REPLACE逻辑。
     * @param t 要插入的对象模型。
     * @return 插入结果。
     */
    public boolean syncSave(final T t) {
        return insertInternal(t, SqlUtils.Operations.INSERT_OR_REPLACE);
    }

    /**
     * 插入多个对象模型。一类对象模型对应一个表，表名为类名。
     * 插入逻辑遵循SQL的INSERT逻辑。
     * @param collection 要插入的对象模型的集合。
     * @return 插入结果。
     */
    public Observable<Boolean> insertAll(final Collection<? extends T> collection) {
        return RxUtils.makeModelObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return insertAllInternal(collection, SqlUtils.Operations.INSERT);
            }
        });
    }

    /**
     * 插入多个对象模型。一类对象模型对应一个表，表名为类名。
     * 如果对象模型对应的表没有创建，将会首先创建表，再插入数据。
     * 如果已经存在相同id的文档模型，将会覆盖。
     * 底层插入逻辑遵循SQL的INSERT OR REPLACE逻辑。
     * @param collection 要插入的对象模型的集合。
     * @return 插入结果。
     */
    public Observable<Boolean> saveAll(final Collection<? extends T> collection) {
        return RxUtils.makeModelObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return insertAllInternal(collection, SqlUtils.Operations.INSERT_OR_REPLACE);
            }
        });
    }

    /**
     * 同步插入多个对象模型。一类对象模型对应一个表，表名为类名。
     * 如果对象模型对应的表没有创建，将会首先创建表，再插入数据。
     * 如果已经存在相同id的文档模型，将会覆盖。
     * 底层插入逻辑遵循SQL的INSERT OR REPLACE逻辑。
     * @param collection 要插入的对象模型的集合。
     * @return 插入结果。
     */
    public boolean syncSaveAll(Collection<? extends T> collection) {
        return insertAllInternal(collection, SqlUtils.Operations.INSERT_OR_REPLACE);
    }

    /**
     * 更新一个对象模型。
     * @param onUpdate 更新回调，可以在回调中更新对象并返回。如果filter的筛选结果为空集，则
     * {@link OnUpdate#update(Object)}中的传入值为null。
     */
    public Observable<Boolean> updateFirst(final OnUpdate<T> onUpdate, final Filter<T> filter) {
        return queryFirst(filter)
        .map(new Func1<T, Boolean>() {
            @Override
            public Boolean call(T t) {
                t = onUpdate.update(t);
                return syncSave(t);
            }
        });
    }

    /**
     * 查询所有满足条件的指定类型数据。如果数据不存在或者表尚未创建，则返回为空列表。
     * @param filter 筛选器
     * @return 满足条件的数据。
     */
    public Observable<List<T>> query(final Filter<T> filter) {
        return RxUtils.makeModelObservable(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return queryInternal(clz, filter);
            }
        });
    }

    /**
     * 同步查询所有满足条件的指定类型数据。如果数据不存在或者表尚未创建，则返回为空列表。
     * @param filter 筛选器
     * @return 满足条件的数据。
     */
    public List<T> syncQuery(final Filter<T> filter) {
        return queryInternal(clz, filter);
    }

    /**
     * 查询所有指定类型数据。如果数据不存在或者表尚未创建，则返回为空列表。
     * @return 所有指定类型的数据。
     */
    public Observable<List<T>> queryAll() {
        return RxUtils.makeModelObservable(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return queryInternal(clz, null);
            }
        });
    }

    /**
     * 查询指定类型数据满足条件的第一条数据。
     * @param filter 筛选器。
     * @return 满足条件的第一条数据。
     */
    public Observable<T> queryFirst(final Filter<T> filter) {
        return RxUtils.makeModelObservable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return queryOneInternal(clz, filter);
            }
        });
    }

    /**
     * 查询所有指定类型数据在表中的第一条数据。
     * @return 第一条查找到的数据。
     */
    public Observable<T> queryFirst() {
        return RxUtils.makeModelObservable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return queryOneInternal(clz);
            }
        });
    }

    /**
     * 同步查询所有指定类型数据在表中的第一条数据。
     */
    public T syncQueryFirst() {
        return queryOneInternal(clz);
    }

    /**
     * 同步查询指定类型数据满足条件的第一条数据。
     */
    public T syncQueryFirst(final Filter<T> filter) {
        return queryOneInternal(clz, filter);
    }

    /**
     * 删除满足条件的指定类型的数据。
     * 首先，会基于{@link DbModel#query(Filter)}进行一次查询，对所有查询到的数据,开启事务
     * 进行删除。
     * @param filter 筛选器
     * @return 删除文档数。
     */
    public Observable<Integer> delete(final Filter<T> filter) {
        return query(filter)
                .map(new Func1<List<T>, Integer>() {
                    @Override
                    public Integer call(List<T> ts) {
                        return deleteInternal(ts);
                    }
                });
    }

    /**
     * 同步删除满足条件的指定类型的数据。
     * 首先，会基于{@link DbModel#query(Filter)}进行一次查询，对所有查询到的数据,开启事务
     * 进行删除。
     * @param filter 筛选器
     * @return 删除文档数。
     */
    public int syncDelete(Filter<T> filter) {
        List<T> ts = syncQuery(filter);
        return deleteInternal(ts);
    }

    /**
     * 删除所有指定类型的数据。
     * 首先，会基于{@link DbModel#query(Filter)}进行一次查询，对所有查询到的数据,开启事务
     * 进行删除。
     * @return 删除文档数。
     */
    public Observable<Integer> deleteAll() {
        return queryAll()
                .map(new Func1<List<T>, Integer>() {
                    @Override
                    public Integer call(List<T> ts) {
                        return deleteInternal(ts);
                    }
                });
    }

    private boolean insertInternal(final T t, final String operation) {
        final SQLiteDatabase db = rxModel.getDataBase();
        if(SqlUtils.assertDbNull(db)) return false;
        return doSqlOperation(db, lock.writeLock(), false, true, false, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String sql = SqlUtils.ofInsert(operation, tableName, converter, t);
                if(TextUtils.isEmpty(sql)) return false;
                db.execSQL(sql);
                return true;
            }
        });
    }

    private boolean insertAllInternal(final Collection<? extends T> collection, final String operation) {
        final SQLiteDatabase db = rxModel.getDataBase();
        if(SqlUtils.assertDbNull(db)) return false;
        return doSqlOperation(db, lock.writeLock(), true, true, false, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String sql = SqlUtils.ofBlankInsert(operation, tableName, 2);
                SQLiteStatement stmt = db.compileStatement(sql);
                for (T t : collection) {
                    String document = converter.convertToDocument(t);
                    stmt.bindString(1, converter.bindID(t));
                    stmt.bindString(2, document);
                    stmt.execute();
                    stmt.clearBindings();
                }
                stmt.close();
                return true;
            }
        });
    }

    private List<T> queryInternal(Class<T> clz, final Filter<T> filter) {
        final SQLiteDatabase db = rxModel.getDataBase();
        final List<T> result = new ArrayList<>();
        if(SqlUtils.assertDbNull(db)) return result;
        final String tableName = clz.getSimpleName();
        return doSqlOperation(db, lock.readLock(), false, true, result, new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                String sql = SqlUtils.createSelectAll(tableName);
                Cursor cursor = db.rawQuery(sql, null);
                SqlUtils.readDocumentsFromCursor(cursor, converter, filter, result);
                return result;
            }
        });
    }

    private T queryOneInternal(Class<T> clz, final Filter<T> filter) {
        final SQLiteDatabase db = rxModel.getDataBase();
        if(SqlUtils.assertDbNull(db)) return null;
        final List<T> result = new ArrayList<>();
        final String tableName = clz.getSimpleName();
        return doSqlOperation(db, lock.readLock(), false, true, null, new Callable<T>() {
            @Override
            public T call() throws Exception {
                String sql = SqlUtils.createSelectAll(tableName);
                Cursor cursor = db.rawQuery(sql, null);
                filter.setLimit(1);
                SqlUtils.readDocumentsFromCursor(cursor, converter, filter, result);
                return result.isEmpty() ? null : result.get(0);
            }
        });
    }

    private T queryOneInternal(Class<T> clz) {
        return queryOneInternal(clz, new EmptyFilter<T>());
    }

    private int deleteInternal(final List<T> ts) {
        if (ts == null || ts.isEmpty()) return 0;
        final SQLiteDatabase db = rxModel.getDataBase();
        if(SqlUtils.assertDbNull(db)) return 0;
        return doSqlOperation(db, lock.writeLock(), true, true, 0, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                String sql = SqlUtils.createBlankDeleteById(tableName);
                SQLiteStatement stmt = db.compileStatement(sql);
                int affectedLines = 0;
                for(T t : ts) {
                    stmt.bindString(1, converter.bindID(t));
                    affectedLines += stmt.executeUpdateDelete();
                    stmt.clearBindings();
                }
                return affectedLines;
            }
        });
    }

    /**
     * 如果表不存在，则创建；否则什么也不做。
     */
    private void createTableIfNotExists(final String tableName) {
        final SQLiteDatabase db = rxModel.getDataBase();
        if(SqlUtils.assertDbNull(db)) return;
        doSqlOperation(db, lock.writeLock(), true, true, false, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String sql = SqlUtils.generateCreateTableSQL(tableName);
                db.execSQL(sql);
                return true;
            }
        });
    }

    /**
     * 执行一个SQL操作。
     * 首先检查指定数据库是否被锁（通过{@link SQLiteDatabase#isDbLockedByCurrentThread()}和RxModel中配置的全局锁）。
     * 如果数据库被锁，则开启事务并执行指定任务；否则直接执行指定任务。
     * 如果全局锁被锁，则需要等待直到获取获取全局锁再执行任务。
     * 无论操作结果，如果closeDbFinally为true，则db均会被关闭。
     * @param db 要操作的数据库。
     * @param lock 从RxModel获取的全局锁。
     * @param forceTransaction 是否跳过锁检查，强制使用事务。
     * @param closeDbFinally 是否完成操作后关闭数据库。
     * @param defaultValue 操作异常时，返回的默认值。
     * @param runnable SQL操作。
     * @return 是否执行成功
     */
    private <K> K doSqlOperation(SQLiteDatabase db,
                                 Lock lock,
                                 boolean forceTransaction,
                                 boolean closeDbFinally,
                                 K defaultValue,
                                 Callable<K> runnable) {
        if (!forceTransaction && db.isDbLockedByCurrentThread()) {
            try {
                lock.lock();
                return runnable.call();
            } catch (Exception e){
                e.printStackTrace();
            } finally {

                //由于每个线程只能获取一个SQLiteDatabase对象，因此不能关闭
                //if(closeDbFinally) db.close();
                lock.unlock();
            }
        } else {
            db.beginTransaction();
            try {
                //lock.lock();
                K result = runnable.call();
                db.setTransactionSuccessful();
                return result;
            } catch (Exception e){
                e.printStackTrace();
            }  finally {
                db.endTransaction();
                //if(closeDbFinally) db.close();
                //lock.unlock();
            }
        }

        //执行SQL异常，返回默认值
        return defaultValue;
    }

    public interface OnUpdate<K> {

        K update(K src);
    }
}
