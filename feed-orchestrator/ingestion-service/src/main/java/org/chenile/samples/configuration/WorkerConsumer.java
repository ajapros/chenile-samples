package org.chenile.samples.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.orchestrator.process.model.Process;
import com.rabbitmq.client.Channel;
import org.chenile.orchestrator.process.model.WorkerType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;


public class WorkerConsumer {

    @Autowired
    private ConfigurableApplicationContext context;


    @Autowired
    private WorkerStarterDelegator workerStarterDelegator;


    private boolean processed = false;


    @RabbitListener(queues = RabbitMQConfig.QUEUE, ackMode = "MANUAL")
    public void listen(String message, Channel channel, Message rawMessage) {

       if (processed) return;

        System.out.println("Received: " + message);

        // Simulate some task
        try {
            Thread.sleep(1000);
             ObjectMapper objectMapper = new ObjectMapper();

            Process process = objectMapper.readValue(message, Process.class);

            Map<String,Object> map = rawMessage.getMessageProperties().getHeaders();

            Map<String, String> stringMap = new HashMap<>();

             WorkerType workerType = null;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                stringMap.put(entry.getKey(), entry.getValue() == null ? "null" : entry.getValue().toString());

                if(entry.getKey().equalsIgnoreCase("worker")){
                    workerType= WorkerType.valueOf(entry.getValue().toString());
                }

            }


            workerStarterDelegator.start(process, stringMap, workerType);
            channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception ignored) {}

        System.out.println("Task complete. Exiting...");

        System.exit(1);
    }
}
