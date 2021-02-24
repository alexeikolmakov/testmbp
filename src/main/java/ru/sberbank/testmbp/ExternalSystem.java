package ru.sberbank.testmbp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ExternalSystem {

    static final Logger logger = LoggerFactory.getLogger(ExternalSystem.class);

    private final Map<Data, Action<Data>> hashData = new ConcurrentHashMap<>();

    public void send(Data data) {
        Action<Data> dataAction = hashData.compute(data, (key, value) -> {
            if (value == null) {
                value = new Action<>(true);
                value.dataQueue.add(key);
            } else {
                value.dataQueue.add(key);
            }
            return value;
        });

        dataAction.execute();
    }

    private final class Action<E extends Data> extends ReentrantLock {

        private final Queue<E> dataQueue = new ConcurrentLinkedQueue<>();

        public void execute() {
            this.lock();

            Data data = dataQueue.poll();

            ExternalSystemProcessor.receive(data);

            hashData.computeIfPresent(data, (k, v) -> {
                if (dataQueue.isEmpty()) {
                    return null;
                } else {
                    return v;
                }
            });

            this.unlock();
        }

        public Action(boolean fair) {
            super(fair);
        }
    }

}
