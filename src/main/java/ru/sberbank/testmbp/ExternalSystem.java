package ru.sberbank.testmbp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ExternalSystem {

    static final Logger logger = LoggerFactory.getLogger(ExternalSystem.class);

    private final Map<Data, Action<Data>> hashData = new ConcurrentHashMap<>();

    private int threads;

    public ExternalSystem(int threads) {
        this.threads = threads;
    }

    public void send(Data data) {
        Action<Data> dataAction = hashData.compute(data, (key, value) -> {
            try {
                if (value == null) {
                    value = new Action<>(threads, true);
                }
                if (!value.offer(data, 2100, TimeUnit.MILLISECONDS)) {
                    logger.error("something going wrong with external processor");
                    throw new RuntimeException("something going wrong with external processor");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.warn("execution interrupted");
            }
            return value;
        });

        dataAction.execute();
    }

    private final class Action<E extends Data> extends ArrayBlockingQueue<E> {

        private final ReentrantLock lock = new ReentrantLock(true);

        public Action(int capacity, boolean fair) {
            super(capacity, fair);
        }

        public void execute() {
            lock.lock();

            Data data = poll();

            ExternalSystemProcessor.receive(data);

            hashData.computeIfPresent(data, (k, v) -> {
                if (isEmpty()) {
                    return null;
                } else {
                    return v;
                }
            });

            lock.unlock();
        }

    }

}
