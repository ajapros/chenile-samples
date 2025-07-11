package org.chenile.samples.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.model.WorkerType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class MasterAmqProducer {

    private final RabbitTemplate rabbitTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MasterAmqProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Process payload, Map<String,String> headers, WorkerType workerType) throws JsonProcessingException {

        MessageProperties messageProperties = new MessageProperties();
        if (headers != null) {
            headers.forEach(messageProperties::setHeader);
        }
        messageProperties.setHeader("worker",workerType.name());

        // Convert body string to byte array
        byte[] bodyBytes = objectMapper.writeValueAsBytes(payload);

        // Create message with body and properties
        Message message = new Message(bodyBytes, messageProperties);

        rabbitTemplate.send(RabbitMQConfig.QUEUE, message);
        System.out.println("Sent: " + message);
    }

}