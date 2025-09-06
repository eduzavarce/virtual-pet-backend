package dev.eduzavarce.pets.shared.core.infrastructure;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:domain-events}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-prefix:events}")
    private String routingPrefix;

    @Value("${app.rabbitmq.queues.user-created-log:user-created-log.q}")
    private String userCreatedLogQueueName;

    @Bean
    public TopicExchange domainEventsExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue userCreatedLogQueue() {
        return QueueBuilder.durable(userCreatedLogQueueName).build();
    }

    @Bean
    public Binding userCreatedLogBinding(TopicExchange domainEventsExchange, Queue userCreatedLogQueue) {
        // Match events.<any-aggregate-id>.user.created
        String pattern = String.format("%s.%s", routingPrefix, "#.user.created");
        return BindingBuilder.bind(userCreatedLogQueue).to(domainEventsExchange).with(pattern);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setExchange(exchangeName);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
