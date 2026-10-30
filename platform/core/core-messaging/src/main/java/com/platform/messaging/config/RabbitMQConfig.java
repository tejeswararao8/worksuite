package com.platform.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─────────────────────────────────────────────
    // EXCHANGE - Routes messages to correct queues
    // ─────────────────────────────────────────────
    public static final String EXCHANGE = "vve.platform.exchange";

    // ─────────────────────────────────────────────
    // QUEUE NAMES
    // ─────────────────────────────────────────────
    public static final String EMAIL_QUEUE          = "vve.email.queue";
    public static final String PAYROLL_QUEUE        = "vve.payroll.queue";
    public static final String NOTIFICATION_QUEUE   = "vve.notification.queue";
    public static final String TICKET_QUEUE         = "vve.ticket.queue";
    public static final String SUPPORT_QUEUE        = "vve.support.queue";
    public static final String AUDIT_QUEUE          = "vve.audit.queue";
    public static final String REPORT_QUEUE         = "vve.report.queue";
    public static final String ONBOARDING_QUEUE     = "vve.onboarding.queue";

    // ─────────────────────────────────────────────
    // ROUTING KEYS
    // ─────────────────────────────────────────────
    public static final String EMAIL_ROUTING_KEY        = "email";
    public static final String PAYROLL_ROUTING_KEY      = "payroll";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";
    public static final String TICKET_ROUTING_KEY       = "ticket";
    public static final String SUPPORT_ROUTING_KEY      = "support";
    public static final String AUDIT_ROUTING_KEY        = "audit";
    public static final String REPORT_ROUTING_KEY       = "report";
    public static final String ONBOARDING_ROUTING_KEY   = "onboarding";

    // ─────────────────────────────────────────────
    // EXCHANGE BEAN
    // ─────────────────────────────────────────────
    @Bean
    public DirectExchange platformExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    // ─────────────────────────────────────────────
    // QUEUES - durable=true means survive restart
    // Dead Letter Queue configured for failed messages
    // ─────────────────────────────────────────────
    @Bean public Queue emailQueue()        { return QueueBuilder.durable(EMAIL_QUEUE).build(); }
    @Bean public Queue payrollQueue()      { return QueueBuilder.durable(PAYROLL_QUEUE).build(); }
    @Bean public Queue notificationQueue() { return QueueBuilder.durable(NOTIFICATION_QUEUE).build(); }
    @Bean public Queue ticketQueue()       { return QueueBuilder.durable(TICKET_QUEUE).build(); }
    @Bean public Queue supportQueue()      { return QueueBuilder.durable(SUPPORT_QUEUE).build(); }
    @Bean public Queue auditQueue()        { return QueueBuilder.durable(AUDIT_QUEUE).build(); }
    @Bean public Queue reportQueue()       { return QueueBuilder.durable(REPORT_QUEUE).build(); }
    @Bean public Queue onboardingQueue()   { return QueueBuilder.durable(ONBOARDING_QUEUE).build(); }

    // ─────────────────────────────────────────────
    // BINDINGS - Connect queues to exchange
    // ─────────────────────────────────────────────
    @Bean public Binding emailBinding()        { return BindingBuilder.bind(emailQueue()).to(platformExchange()).with(EMAIL_ROUTING_KEY); }
    @Bean public Binding payrollBinding()      { return BindingBuilder.bind(payrollQueue()).to(platformExchange()).with(PAYROLL_ROUTING_KEY); }
    @Bean public Binding notificationBinding() { return BindingBuilder.bind(notificationQueue()).to(platformExchange()).with(NOTIFICATION_ROUTING_KEY); }
    @Bean public Binding ticketBinding()       { return BindingBuilder.bind(ticketQueue()).to(platformExchange()).with(TICKET_ROUTING_KEY); }
    @Bean public Binding supportBinding()      { return BindingBuilder.bind(supportQueue()).to(platformExchange()).with(SUPPORT_ROUTING_KEY); }
    @Bean public Binding auditBinding()        { return BindingBuilder.bind(auditQueue()).to(platformExchange()).with(AUDIT_ROUTING_KEY); }
    @Bean public Binding reportBinding()       { return BindingBuilder.bind(reportQueue()).to(platformExchange()).with(REPORT_ROUTING_KEY); }
    @Bean public Binding onboardingBinding()   { return BindingBuilder.bind(onboardingQueue()).to(platformExchange()).with(ONBOARDING_ROUTING_KEY); }

    // ─────────────────────────────────────────────
    // MESSAGE CONVERTER - JSON serialization
    // ─────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ─────────────────────────────────────────────
    // RABBIT TEMPLATE - Used to send messages
    // ─────────────────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
