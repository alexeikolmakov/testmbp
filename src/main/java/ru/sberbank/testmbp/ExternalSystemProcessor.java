package ru.sberbank.testmbp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalSystemProcessor {

    static final Logger logger = LoggerFactory.getLogger(ExternalSystemProcessor.class);

    private static final Set<Data> dataSet = ConcurrentHashMap.newKeySet();

    public static void receive(Data data) {
        if (dataSet.contains(data)) {
           logger.error("wrong execution");
        }

        // пауза от 10 до 2000 мс
        int timems = getRandomNumberUsingNextInt(10, 2000);
        try {
            Thread.sleep(timems);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(String.join(" ", "Thread: " + Thread.currentThread().getName(),
                        "time: " + timems, "code: " + data.getCode(), "data: " + data.getData()));


    }


    public static int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
