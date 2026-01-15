package br.com.magno.pedido.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	
	@Value("${rabbitmq.localhost}")
	private String localHost;
	@Value("${rabbitmq.username}")
	private String userName;
	@Value("${rabbitmq.password}")
	private String passWord;
	@Value("${rabbitmq.filaentrada}")
	private String filaEntrada;
	@Value("${rabbitmq.filadlq}")
    private String filaDlq;
	@Value("${rabbitmq.exchangedlq}")
    private String exchangeDlq;
	@Value("${rabbitmq.filasucesso}")
	private String filaSucesso;
	@Value("${rabbitmq.filafalha}")
	private String filaFalha;
	
	@Bean
    public ConnectionFactory connectionFactory() {
		String host = localHost.replace("https://", "")
                .replace("http://", "")
                .replace("amqp://", "")
                .split("/")[0];
        
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(passWord);
        connectionFactory.setVirtualHost(userName); 
        connectionFactory.setPort(5672);
        
        return connectionFactory;
    }
	
	@Bean
    public Queue filaPrincipal() {
		return QueueBuilder.durable(filaEntrada)
					.withArgument("x-dead-letter-exchange", "") // DLQ padrão
					.withArgument("x-dead-letter-routing-key", filaDlq)
					.build();
    }

    @Bean
    public Queue filaDLQ() {
        return QueueBuilder.durable(filaDlq).build();
    }

    @Bean
    public DirectExchange exchangeDLQ() {
        return new DirectExchange(exchangeDlq);
    }

    @Bean
    public Binding bindingDLQ() {
        return BindingBuilder.bind(filaDLQ()).to(exchangeDLQ()).with(filaDlq);
    }

    @Bean
    public Binding binding(Queue filaPrincipal, DirectExchange exchange) {
        return BindingBuilder.bind(filaPrincipal).to(exchange).with("rodrigo-magno");
    }
    
    @Bean
    public Queue filaSucesso() {
        return new Queue(filaSucesso);
    }

    @Bean
    public Queue filaFalha() {
        return new Queue(filaFalha);
    }
    
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
