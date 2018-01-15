package com.synaric.app.rxmodel.converter;

/**
 * 确立对象模型和最终存储结果之间的映射关系。
 * 对象模型指实际应用中需要存储到数据库的Java对象，文档模型指该对象在数据库中的存储形式。
 * 文档模型必须为String类型。
 * Created by Synaric on 2016/8/23 0023.
 */
public interface IConverter<T> {

    /**
     * 将文档模型转换为对象模型。
     * @param document 文档模型。
     * @return 对象模型。
     */
    T convertToObject(String document);

    /**
     * 将对象模型转换为文档模型。
     * @param t 对象模型。
     * @return 文档模型。
     */
    String convertToDocument(T t);

    /**
     * 确定文档模型的主键，这个主键作为文档模型的唯一标识。
     * @param t 对象模型。
     * @return ID值。
     */
    String bindID(T t);
}
