package com.synaric.app.rxmodel.filter;

/**
 * 不筛选任何数据的筛选器。
 * Created by Synaric on 2016/8/25 0025.
 */
public class EmptyFilter<T> extends Filter<T> {

    public EmptyFilter() {
        super();
    }

    @Override
    public boolean doIterativeFilter(T t) {
        return true;
    }
}
