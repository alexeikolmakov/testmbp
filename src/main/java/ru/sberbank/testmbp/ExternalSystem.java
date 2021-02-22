package ru.sberbank.testmbp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

public class ExternalSystem {

    static final Logger logger = LoggerFactory.getLogger(ExternalSystem.class);

    private final ForkJoinPool pool;
    private final Map<Data, Action<Data>> hashData = new ConcurrentHashMap<>();

    public ExternalSystem(int nthreads) {
        this.pool = new ForkJoinPool(nthreads);
    }

    public void send(Data data) {
        Action<Data> dataAction = hashData.merge(data, new Action<>(null, data), (completer1, completer2) -> {
            Action<Data> dataAction1 = new Action<>(completer1, data);
            return dataAction1;
        });
        if (dataAction.getCompleter() == null) {
            pool.submit(dataAction);
        }
    }

    class Action<E extends Data> extends CountedCompleter<E> {

        private final E data;

        @Override
        public void compute() {
            CountedCompleter<?> c = getCompleter();
            if (c != null && c.getPendingCount() == 0) {
                c.setPendingCount(1);
                c.fork();
                c.join();
            }
            // TODO эмуляция отправки данных

            ExternalSystemProcessor.receive(data);

            if (compareAndSetPendingCount(1 , 0)) {
                quietlyComplete();
            } else {
                hashData.computeIfPresent(data, (k, v) -> {
                    if (v != this) {
                        this.addToPendingCount(1);
                        v.fork();
                        return v;
                    } else {
                        return null;
                    }
                });

            }
        }

        public Action(CountedCompleter<?> completer, E data) {
            super(completer);
            this.data = data;
        }
    }

}
