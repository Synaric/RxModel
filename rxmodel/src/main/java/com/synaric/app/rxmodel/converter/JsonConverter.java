package com.synaric.app.rxmodel.converter;

import com.google.gson.Gson;

/**
 * 将对象模型以Json格式存储。
 * Created by Synaric on 2016/8/23 0023.
 */
public abstract class JsonConverter<T> extends Converter<T> {

    private Gson gson = new Gson();

    public JsonConverter(Class<T> clz) {
        super(clz);
    }

    @Override
    public T convertToObject(String document) {
        return gson.fromJson(document, getActualClass());
    }

    @Override
    public String convertToDocument(T t) {
        return gson.toJson(t);
    }
}
