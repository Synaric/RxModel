package com.synaric.app.rxmodel.filter;

/**
 * 用于对数据集合进行筛选的接口。
 * Created by Synaric on 2016/8/25 0025.
 */
public interface IFilter<T> {

    /**
     * 判断指定数据对象是否满足特定条件。
     * @param t 数据对象。
     * @return 是否保留到结果集。
     */
    boolean doIterativeFilter(T t);
}
