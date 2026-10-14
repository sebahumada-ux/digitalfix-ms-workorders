package cl.duoc.digitalfix.workorders.messaging;

import cl.duoc.digitalfix.workorders.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WorkOrderEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(WorkOrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public WorkOrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarEmail(Long workOrderId, String estado) {

        String mensaje = crearMensaje(
                "EMAIL",
                workOrderId,
                estado
        );

        publicar(
                RabbitMqConfig.DIRECT_EXCHANGE,
                RabbitMqConfig.EMAIL_ROUTING_KEY,
                mensaje
        );
    }

    public void enviarDespacho(Long workOrderId, String estado) {

        String mensaje = crearMensaje(
                "DISPATCH",
                workOrderId,
                estado
        );

        publicar(
                RabbitMqConfig.DIRECT_EXCHANGE,
                RabbitMqConfig.DISPATCH_ROUTING_KEY,
                mensaje
        );
    }

    public void generarReporte(Long workOrderId, String estado) {

        String mensaje = crearMensaje(
                "REPORT",
                workOrderId,
                estado
        );

        publicar(
                RabbitMqConfig.DIRECT_EXCHANGE,
                RabbitMqConfig.REPORT_ROUTING_KEY,
                mensaje
        );
    }

    private String crearMensaje(
            String tipo,
            Long workOrderId,
            String estado) {

        return """
                {
                    "tipo": "%s",
                    "workOrderId": %d,
                    "estado": "%s",
                    "timestamp": "%s"
                }
                """.formatted(
                tipo,
                workOrderId,
                estado,
                LocalDateTime.now()
        );
    }

    private void publicar(
            String exchange,
            String routingKey,
            String mensaje) {

        try {

            rabbitTemplate.convertAndSend(
                    exchange,
                    routingKey,
                    mensaje
            );

            log.info(
                    "Evento RabbitMQ enviado. Exchange={}, routingKey={}",
                    exchange,
                    routingKey
            );

        } catch (AmqpException e) {

            log.error(
                    "No fue posible enviar evento RabbitMQ. Exchange={}, routingKey={}",
                    exchange,
                    routingKey,
                    e
            );
        }
    }
}