package com.taibai.admin.syncproject;

import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁工具类
 */
public final class LockManager {

    private static Locker locker;

    /**
     * 设置工具类使用的locker
     * @param locker
     */
    public static void setLocker(Locker locker) {
        LockManager.locker = locker;
    }

    /**
     * 获取锁
     * @param lockKey
     */
    public static void lock(String lockKey) {
        locker.lock(lockKey);
    }

    /**
     * 释放锁
     * @param lockKey
     */
    public static void unlock(String lockKey) {
        locker.unlock(lockKey);
    }

    /**
     * 获取锁，超时释放
     * @param lockKey
     * @param leaseTime（seconds）
     */
    public static void lock(String lockKey, long leaseTime) {
        locker.lock(lockKey, leaseTime);
    }

    /**
     * 获取锁，超时释放，指定时间单位
     * @param lockKey
     * @param leaseTime
     * @param unit
     */
    public static void lock(String lockKey, int leaseTime, TimeUnit unit) {
        locker.lock(lockKey, leaseTime, unit);
    }

    /**
     * 尝试获取锁，获取到立即返回true,获取失败立即返回false
     * @param lockKey
     * @return
     */
    public static boolean tryLock(String lockKey) {
        return locker.tryLock(lockKey);
    }

    /**
     * 尝试获取锁，在给定的waitTime时间内尝试，获取到返回true,获取失败立即返回false
     * @param lockKey
     * @param waitTime（seconds）
     * @return
     */
    public static boolean tryLock(String lockKey, long waitTime) {
        return locker.tryLock(lockKey, waitTime);
    }

    /**
     * 尝试获取锁，在给定的waitTime时间内尝试，获取到返回true,获取失败立即返回false
     * @param lockKey
     * @param waitTime
     * @param unit
     * @return
     */
    public static boolean tryLock(String lockKey, long waitTime, TimeUnit unit) {
        return locker.tryLock(lockKey, waitTime, unit);
    }

    /**
     * 尝试获取锁，在给定的waitTime时间内尝试，获取到返回true,获取失败返回false,获取到后再给定的leaseTime时间超时释放
     * @param lockKey
     * @param waitTime（seconds）
     * @param leaseTime（seconds）
     * @return
     * @throws InterruptedException
     */
    public static boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        return locker.tryLock(lockKey, waitTime, leaseTime);
    }

    /**
     * 尝试获取锁，带时间单位
     * @param lockKey
     * @param waitTime
     * @param leaseTime
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public static boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        return locker.tryLock(lockKey, waitTime, leaseTime, unit);
    }

    /**
     * 锁释放被任意一个线程持有
     * @param lockKey
     * @return
     */
    public static boolean isLocked(String lockKey) {
        return locker.isLocked(lockKey);
    }

    /**
     * 获取锁列表
     * @return
     */
    public static Iterable<String> list() {
        return locker.list();
    }
}
