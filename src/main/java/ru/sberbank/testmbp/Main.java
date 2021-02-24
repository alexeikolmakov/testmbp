package ru.sberbank.testmbp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {

        // случайные коды системы от 1 до 10
        final IntStream intStream = new Random().ints(100, 1,10);

        int threads = 8;

        // внешняя система с 8 потоками
        final ExternalSystem externalSystem = new ExternalSystem(threads);

        // вызов внеше1 симстемы в 8 потоков
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);

        AtomicInteger atomic = new AtomicInteger();

        for (int i : intStream.toArray()) {
            executorService.execute(() -> {
                logger.info(String.join(" ", "Data", "code:" + i, "поток "+ Thread.currentThread().getName()));
                externalSystem.send(new Data(i+"", "данные: " + atomic.incrementAndGet()));
            });
        }

        Thread.sleep(40000);

        atomic.set(0);

        // проверка повторного запуска
        for (int i = 0; i<10 ; i++) {
            int finalI = i;
            executorService.execute(() -> {
                logger.info(String.join(" ", "Data", "code:" + finalI, "поток "+ Thread.currentThread().getName()));
                externalSystem.send(new Data(finalI +"", "данные: " + atomic.incrementAndGet()));
            });
        }

        Thread.sleep(6000);

        executorService.shutdown();
    }
}