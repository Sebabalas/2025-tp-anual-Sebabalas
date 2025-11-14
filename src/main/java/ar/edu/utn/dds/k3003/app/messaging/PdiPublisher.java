package ar.edu.utn.dds.k3003.app.messaging;

import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PdiPublisher {

    private static final Logger log = LoggerFactory.getLogger(PdiPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PdiPublisher(RabbitTemplate rabbitTemplate,
                        @Value("${amqp.pdi.exchange}") String exchange,
                        @Value("${amqp.pdi.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(PdiDTONuevo dto) {
        log.info("[AMQP] Publicando PDI DTO hechoId={} desc={} lugar={}", dto.hechoId(), dto.descripcion(), dto.lugar());
        rabbitTemplate.convertAndSend(exchange, routingKey, dto);
    }
}

