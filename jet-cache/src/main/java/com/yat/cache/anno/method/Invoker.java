package com.yat.cache.anno.method;


/**
 * 定义了一个Invoker接口，用于执行特定的操作
 * 此接口的主要目的是提供一种通用的调用机制，可以被各种具体的 调用逻辑实现
 *
 * @author Yat
 * Date 2024/8/22 22:00
 * version 1.0
 */
public interface Invoker {

    /**
     * 执行调用逻辑
     *
     * @return 调用操作的结果
     * @throws Throwable 如果调用过程中发生错误，可以抛出Throwable及其子类的异常
     */
    Object invoke() throws Throwable;
}

