package com.synaric.app.rxmodel.utils;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 反射相关工具类。
 * Created by Synaric on 2016/8/30 0030.
 */
public class ReflectUtils {


    /**
     * 获取指定类的泛型参数的确切类型。
     * <br/><br/>例子：
     * <pre>
     * List&lt;String&gt; lst = new ArrayList&lt;&gt;;
     * Class&lt;T&gt clz = (Class&lt;T&gt;) ReflectUtils.getActualClass(lst.getClass(), 0);
     * final String actualType = clz.getSimpleName();
     * </pre>
     * @param clz 需要获取确切类型的类。
     * @param index 需要获取确切类型的泛型的索引，从0开始。
     * @return 确切类型。
     */
    public static Class<?> getActualClass(Class<?> clz, int index) {
        Type genType = clz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

    /**
     * 获取指定类的泛型参数的确切类型(Gson实现)。
     * 在某些极端情况下，相比{@link #getActualClass(Class, int)}具有更好的效果。
     * 例如，泛型参数嵌套：
     * <pre>
     * Response&lt;List&lt;String&gt;&gt; response = new Response()&lt;&gt;;
     * Class&lt;T&gt clz = (Class&lt;T&gt;) ReflectUtils.getActualClass(response.getClass(), 0);
     * </pre>
     * 将会获得正确的类型List&lt;String&gt;。
     * @param clz 需要获取确切类型的类。
     * @return 确切类型。
     */
    public static Type getActualClass(Class<?> clz) {
        Type superclass = clz.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    /**
     * 获取指定对象的某个Field。
     */
    public static Field getDeclaredField(Object object, String fieldName) {
        Class<?> clz = object.getClass();
        for(; clz != Object.class; clz = clz.getSuperclass()) {
            try {

                return clz.getDeclaredField(fieldName);

            } catch (NoSuchFieldException e) {
                //不处理，继续在超类寻找
            }
        }

        return null;
    }

    /**
     * 获取指定对象某个Field的值。
     */
    public static Object getDeclaredFieldValue(Object object, String fieldName) {
        Field field = getDeclaredField(object, fieldName);
        if(field != null) {
            field.setAccessible(true);
            try {

                return field.get(object);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取指定对象的某个方法。
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... paramTypes) {
        Class<?> clz = object.getClass();
        for(; clz != Object.class; clz = clz.getSuperclass()) {
            try {

                Method method = clz.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;

            } catch (NoSuchMethodException e) {
                //不处理，继续在超类寻找
            }
        }

        return null;
    }

    /**
     * 获取指定对象的某个方法。
     */
    public static Method getMethod(Object object, String methodName, Class<?>... paramTypes) {
        Class<?> clz = object.getClass();
        for(; clz != Object.class; clz = clz.getSuperclass()) {
            try {

                Method method = clz.getMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;

            } catch (NoSuchMethodException e) {
                //不处理，继续在超类寻找
            }
        }

        return null;
    }

    /**
     * 执行方法。
     */
    public static Object invoke(Object object, Method method, Object... args) {
        if(method == null) return null;
        try {
            method.setAccessible(true);
            return method.invoke(object, args);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
