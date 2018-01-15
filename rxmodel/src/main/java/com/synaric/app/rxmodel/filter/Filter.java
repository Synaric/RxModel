package com.synaric.app.rxmodel.filter;

/**
 * 抽象筛选器，定义了基本的筛选行为。
 * 可以通过{@link Filter#start}和{@link Filter#limit}设置筛选范围，但仅在查询语句中有效。
 * Created by Synaric on 2016/8/26 0026.
 */
@SuppressWarnings("unused")
public abstract class Filter<T> implements IFilter<T>{

    /**
     * 筛选开始的起始索引，从0开始。
     */
    private int start;

    /**
     * 最多筛选出的数据数。如果已经筛选出的数据量 = limit，则剩余数据被放弃。
     */
    private int limit;

    /**
     * 是否终结（即放弃所有剩余未筛选的数据）。
     */
    private boolean terminated;

    /**
     * 标记位，是否筛选出指定索引范围、指定数量的数据。
     */
    private boolean readAll;

    public Filter() {
        setDoFilterRange(0, -1);
    }

    public Filter(int start, int limit) {
        setDoFilterRange(start, limit);
    }

    /**
     * 筛选数据。如果数据索引index < {@link Filter#start}则该数据被放弃。如果collected > {@link Filter#limit}，则剩余
     * 所有数据都会被放弃，并且{@link Filter#isTerminated()}返回true。否则，通过{@link Filter#doIterativeFilter(Object)}筛选。
     * @param index 当前被筛选数据的索引。
     * @param collected 当前已经有多少数据被选入结果集。
     * @return 当前被筛选数据是否被选入结果集。
     */
    public final boolean doFilter(T t, int index, int collected) {
        if(!readAll) {
            if(index < start) return false;
            if(collected > limit) {
                terminated = true;
                return false;
            }
        }
        return doIterativeFilter(t);
    }

    /**
     * 对指定起始索引start开始筛选，筛选数据最多为limit.
     */
    public void setDoFilterRange(int start, int limit) {
        this.start = start;
        this.limit = limit;
        readAll = start <= 0 && limit <= 0;
    }

    @Override
    public boolean doIterativeFilter(T t) {
        return false;
    }

    public void setStart(int start) {
        setDoFilterRange(start, limit);
    }

    public void setLimit(int limit) {
        setDoFilterRange(start, limit);
    }

    public int getStart() {
        return start;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isTerminated() {
        return terminated;
    }
}
