package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event.BookReturnedToSupplierEvent;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event.BudzetUpdateFailedEvent;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event.BudzetUpdatedEvent;

import java.util.Map;

// Deklarisem redove koje ovaj servis slusa

@Configuration
public class RabbitMQConfig {

    public static final String CHOREOGRAPHY_EXCHANGE = "saga.choreography.exchange";

    // objavljujem ovde
    public static final String BOOK_RETURNED_TO_SUPPLIER_KEY = "book.returned.supplier";

    // slusam ovde
    public static final String BUDGET_UPDATED_QUEUE = "budget.updated.queue";
    public static final String BUDGET_UPDATED_KEY   = "budget.updated";

    public static final String BUDGET_UPDATE_FAILED_QUEUE = "budget.update.failed.queue";
    public static final String BUDGET_UPDATE_FAILED_KEY   = "budget.update.failed";


    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        typeMapper.setIdClassMapping(Map.of(
                "bookReturnedToSupplierEvent", BookReturnedToSupplierEvent.class,
                "budgetUpdateFailedEvent", BudzetUpdateFailedEvent.class,
                "budgetUpdatedEvent", BudzetUpdatedEvent.class
        ));
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    @Bean
    public TopicExchange choreographyExchange() {
        return new TopicExchange(CHOREOGRAPHY_EXCHANGE);
    }

    @Bean public Queue budgetUpdatedQueue()      { return QueueBuilder.durable(BUDGET_UPDATED_QUEUE).build(); }
    @Bean public Queue budgetUpdateFailedQueue() { return QueueBuilder.durable(BUDGET_UPDATE_FAILED_QUEUE).build(); }

    @Bean public Binding budgetUpdatedBinding()      { return BindingBuilder.bind(budgetUpdatedQueue()).to(choreographyExchange()).with(BUDGET_UPDATED_KEY); }
    @Bean public Binding budgetUpdateFailedBinding() { return BindingBuilder.bind(budgetUpdateFailedQueue()).to(choreographyExchange()).with(BUDGET_UPDATE_FAILED_KEY); }

}
