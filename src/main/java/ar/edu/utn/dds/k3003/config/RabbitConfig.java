package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(prefix = "amqp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitConfig {

    @Value("${amqp.uri:}")
    private String amqpUri;

    /**
     * ConnectionFactory basado en URI, similar a la configuración de tp-dds-fuentes.
     * Permite usar un CLOUDAMQP_URL/amqp.uri con credenciales y vhost embebidos.
     */
    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        if (amqpUri != null && !amqpUri.isBlank()) {
            factory.setUri(amqpUri);
        }
        return factory;
    }

    @Bean
    public Queue pdiQueue(@Value("${amqp.pdi.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange pdiExchange(@Value("${amqp.pdi.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding pdiBinding(Queue pdiQueue,
                              DirectExchange pdiExchange,
                              @Value("${amqp.pdi.routing-key}") String routingKey) {
        return BindingBuilder.bind(pdiQueue).to(pdiExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(1);
        return factory;
    }
}

