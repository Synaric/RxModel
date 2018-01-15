package com.synaric.app.rxmodel.converter;

/**
 * 对象模型-文档模型转换器。
 * 子类必须实现它们的相互转换。
 * Created by Synaric on 2016/8/31 0031.
 */
public abstract class Converter<T> implements IConverter<T>{

    private Class<T> actualClass;

    public Converter(Class<T> clz) {
        this.actualClass = clz;
    }

    /**
     * 将文档模型转换为对象模型。
     * @param document 文档模型。
     * @return 对象模型。
     */
    public abstract T convertToObject(String document);

    /**
     * 将对象模型转换为文档模型。
     * @param t 对象模型。
     * @return 文档模型。
     */
    public abstract String convertToDocument(T t);

    /**
     * 确定文档模型的主键，这个主键作为文档模型的唯一标识。
     * @param t 对象模型。
     * @return ID值。
     */
    public abstract String bindID(T t);

    /**
     * 获取泛型的确切类型。
     * @return 确切类型。
     */
    public Class<T> getActualClass() {
        return actualClass;
    }
}
