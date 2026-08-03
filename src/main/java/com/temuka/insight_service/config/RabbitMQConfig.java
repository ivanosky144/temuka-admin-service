package com.temuka.insight_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.host:localhost}")
    private String rabbitMqHost;

    @Value("${rabbitmq.port:5672}")
    private int rabbitMqPort;

    @Value("${rabbitmq.username:guest}")
    private String rabbitMqUsername;

    @Value("${rabbitmq.password:guest}")
    private String rabbitMqPassword;

    @Value("${rabbitmq.queue.name:temuka_queue}")
    private String queueName;

    @Value("${rabbitmq.search.sync.queue.name:search.sync}")
    private String searchSyncQueueName;

    public static final String SEARCH_EXCHANGE = "temuka_search_exchange";
    public static final String SEARCH_SYNC_ROUTING_KEY = "search.sync";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitMqHost, rabbitMqPort);
        connectionFactory.setUsername(rabbitMqUsername);
        connectionFactory.setPassword(rabbitMqPassword);
        return connectionFactory;
    }

    // 1. Declare the Jackson Message Converter Bean
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public DirectExchange searchExchange() {
        return new DirectExchange(SEARCH_EXCHANGE, true, false);
    }

    @Bean
    public Queue appQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue searchSyncQueue() {
        return new Queue(searchSyncQueueName, true);
    }

    @Bean
    public Binding searchSyncBinding(Queue searchSyncQueue, DirectExchange searchExchange) {
        return BindingBuilder
                .bind(searchSyncQueue)
                .to(searchExchange)
                .with(SEARCH_SYNC_ROUTING_KEY);
    }

    // 2. CRITICAL: Inject jsonMessageConverter() into the ContainerFactory!
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);   
        factory.setMessageConverter(jsonMessageConverter()); // <--- THIS line wires Jackson to @RabbitListener
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}