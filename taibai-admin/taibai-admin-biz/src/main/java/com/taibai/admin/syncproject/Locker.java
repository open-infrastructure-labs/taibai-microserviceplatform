package com.taibai.admin.syncproject;

import java.util.concurrent.TimeUnit;

public interface Locker {

    final String LOCK_PREFIX = "rclock_";

    /**
     * 获取锁，如果锁不可用，则当前线程处于休眠状态，直到获得锁为止。
     * 
     * @param lockKey
     */
    void lock(String lockKey);

    /**
     * 释放锁
     * 
     * @param lockKey
     */
    void unlock(String lockKey);

    /**
     * 获取锁,如果锁不可用，则当前线程处于休眠状态，直到获得锁为止。如果获取到锁后，执行结束后解锁或达到超时时间后会自动释放锁
     * 
     * @param lockKey
     * @param leaseTime 上锁后自动释放锁时间 单位：秒
     */
    void lock(String lockKey, long leaseTime);

    /**
     * 带超时单位的锁
     * 
     * @param lockKey
     * @param leaseTime 上锁后自动释放锁时间
     * @param unit      时间单位
     */
    void lock(String lockKey, long leaseTime, TimeUnit unit);

    /**
     * 尝试获取锁，获取到立即返回true,未获取到立即返回false
     *
     * @param lockKey
     * @return
     */
    boolean tryLock(String lockKey);

    /**
     * 尝试获取锁，在等待时间内获取到锁则返回true,否则返回false。
     * 
     * @param lockKey
     * @param waitTime 最多等待时间 单位：秒
     * @return
     */
    boolean tryLock(String lockKey, long waitTime);

    /**
     * 尝试获取锁，在等待时间内获取到锁则返回true,否则返回false。
     * 
     * @param lockKey
     * @param waitTime 最多等待时间
     * @param unit     时间单位
     * @return
     */
    boolean tryLock(String lockKey, long waitTime, TimeUnit unit);

    /**
     * 尝试获取锁，在等待时间内获取到锁则返回true,否则返回false。如果获取到锁后，执行结束后解锁或达到超时时间后会自动释放锁
     * 
     * @param lockKey
     * @param waitTime  最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime);

    /**
     * 尝试获取锁，带时间单位
     * 
     * @param lockKey
     * @param waitTime  最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @param unit      时间单位
     * @return
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 锁是否被任意一个线程锁持有
     *
     * @param lockKey
     * @return
     */
    boolean isLocked(String lockKey);

    /**
     * 列出所有锁
     *
     * @return
     */
    Iterable<String> list();
}
