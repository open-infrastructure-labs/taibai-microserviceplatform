package com.taibai.admin.syncproject;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class RedissonLocker implements Locker {

    private RedissonClient redissonClient;

    public RedissonLocker(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void lock(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        lock.lock();
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        lock.unlock();
    }

    @Override
    public void lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
    }

    @Override
    public void lock(String lockKey, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        lock.lock(leaseTime, unit);
    }

    @Override
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        try {
            return lock.tryLock(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        try {
            return lock.tryLock(waitTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        return lock.isLocked();
    }

    @Override
    public Iterable<String> list() {
        RKeys keys = redissonClient.getKeys();
        return keys.getKeysByPattern(LOCK_PREFIX + "*");
    }
}
