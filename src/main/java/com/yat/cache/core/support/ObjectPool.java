package com.yat.cache.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * ClassName ObjectPool
 * <p>Description 对象池类，用于管理对象的创建、销毁和复用</p>
 *
 * @author Yat
 * Date 2024/9/25 上午10:04
 * version 1.0
 */
public class ObjectPool<T> {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ObjectPool.class);

    /**
     * 池队列，用于存放对象
     */
    private final ArrayBlockingQueue<T> queue;
    /**
     * 对象池的大小
     */
    private final int size;

    /**
     * 对象工厂，用于创建和重置对象
     */
    private final ObjectFactory<T> factory;

    /**
     * 初始化对象池
     *
     * @param size    池的大小
     * @param factory 对象工厂，用于创建和重置对象
     */
    public ObjectPool(int size, ObjectFactory<T> factory) {
        this.size = size;
        this.factory = factory;
        queue = new ArrayBlockingQueue<>(size);

        // 初始化池中的对象
        for (int i = 0; i < size; i++) {
            queue.add(factory.create());
        }

        // 记录对象池初始化日志
        logger.debug("Init the object pool with size {}", size);
    }


    /**
     * 借出对象
     *
     * @return 借出的对象
     */
    public T borrowObject() {
        T t = queue.poll();

        // 如果池中没有可用对象，则创建新对象
        if (t == null) {
            logger.debug("The pool is not enough, create a new object");
            return factory.create();
        }
        return t;
    }

    /**
     * 归还对象到池中
     *
     * @param obj 需要归还的对象
     */
    public void returnObject(T obj) {
        // 对空对象直接返回
        if (obj == null) {
            return;
        }

        // 重置对象状态
        factory.reset(obj);

        // 将对象放回池中
        queue.offer(obj);
    }

    /**
     * 对象工厂接口，用于创建和重置对象
     *
     * @param <T> 工厂生产对象的类型
     */
    public interface ObjectFactory<T> {

        /**
         * 创建新对象
         *
         * @return 新创建的对象
         */
        T create();

        /**
         * 重置对象状态，以便再次使用
         *
         * @param obj 需要重置的对象
         */
        void reset(T obj);
    }
}
