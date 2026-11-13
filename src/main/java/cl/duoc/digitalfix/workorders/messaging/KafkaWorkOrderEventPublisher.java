
package cl.duoc.digitalfix.workorders.messaging;

import cl.duoc.digitalfix.workorders.dto.OrdenEvento;
import cl.duoc.digitalfix.workorders.model.WorkOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@ConditionalOnProperty(
        name = "app.kafka.enabled",
        havingValue = "true"
)
public class KafkaWorkOrderEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaWorkOrderEventPublisher.class
            );

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String topico;

    public KafkaWorkOrderEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            @Value("${app.kafka.topics.workorders}") String topico) {

        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.topico = topico;
    }

    public void publicar(WorkOrder orden) {

        Long ordenId = orden.getId();
        String estado = orden.getEstado().name();

        try {

            OrdenEvento evento = new OrdenEvento(
                    UUID.randomUUID().toString(),
                    ordenId,
                    orden.getClienteId(),
                    estado,
                    orden.getTecnicoId(),
                    Instant.now().toString()
            );

            String json = jsonMapper.writeValueAsString(evento);
            String clave = String.valueOf(ordenId);

            kafkaTemplate.send(topico, clave, json)
                    .whenComplete((resultado, error) -> {

                        if (error != null) {

                            log.error(
                                    "Error publicando orden {} en Kafka",
                                    ordenId,
                                    error
                            );

                        } else {

                            log.info(
                                    "Kafka: orden={}, estado={}, particion={}, offset={}",
                                    ordenId,
                                    estado,
                                    resultado.getRecordMetadata().partition(),
                                    resultado.getRecordMetadata().offset()
                            );
                        }
                    });

        } catch (Exception e) {

            log.error(
                    "No fue posible preparar el evento Kafka de la orden {}",
                    ordenId,
                    e
            );
        }
    }
}
