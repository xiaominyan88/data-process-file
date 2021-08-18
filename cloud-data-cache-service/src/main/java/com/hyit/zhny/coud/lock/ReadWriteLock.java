package com.hyit.zhny.coud.lock;

import java.util.concurrent.locks.Lock;

public interface ReadWriteLock {

    Lock readLock();

    Lock writeLock();
}
