package com.dsrl.producer.api;

import com.dsrl.producer.config.RabbitProps;
import com.dsrl.producer.model.CustomMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PublishController {

    private final RabbitTemplate rabbit;
    private final RabbitProps rabbitProps;

    public PublishController(RabbitTemplate rabbit, RabbitProps rabbitProps) {
        this.rabbit = rabbit;
        this.rabbitProps = rabbitProps;
    }

    enum Target { cons1, cons2, both }

    @PostMapping("/queue/{target}")
    public ResponseEntity<?> queue(@PathVariable Target target, @RequestBody CustomMessage m) {
        switch (target) {
            case cons1 -> rabbit.convertAndSend(rabbitProps.queueCons1, m);
            case cons2 -> rabbit.convertAndSend(rabbitProps.queueCons2, m);
            case both -> { rabbit.convertAndSend(rabbitProps.queueCons1, m); rabbit.convertAndSend(rabbitProps.queueCons2, m); }
        }
        return ResponseEntity.accepted().body("queue→" + target);
    }

    @PostMapping("/default/{target}")
    public ResponseEntity<?> def(@PathVariable Target target, @RequestBody CustomMessage m) {
        switch (target) {
            case cons1 -> rabbit.convertAndSend("", rabbitProps.queueCons1, m);
            case cons2 -> rabbit.convertAndSend("", rabbitProps.queueCons2, m);
            case both -> { rabbit.convertAndSend("", rabbitProps.queueCons1, m); rabbit.convertAndSend("", rabbitProps.queueCons2, m); }
        }
        return ResponseEntity.accepted().body("default→" + target);
    }

    @PostMapping("/direct/{target}")
    public ResponseEntity<?> direct(@PathVariable Target target, @RequestBody CustomMessage m) {
        switch (target) {
            case cons1 -> rabbit.convertAndSend(rabbitProps.directEx, rabbitProps.directKeyCons1, m);
            case cons2 -> rabbit.convertAndSend(rabbitProps.directEx, rabbitProps.directKeyCons2, m);
            case both -> {
                rabbit.convertAndSend(rabbitProps.directEx, rabbitProps.directKeyCons1, m);
                rabbit.convertAndSend(rabbitProps.directEx, rabbitProps.directKeyCons2, m);
            }
        }
        return ResponseEntity.accepted().body("direct→" + target);
    }

    @PostMapping("/topic/{target}")
    public ResponseEntity<?> topic(@PathVariable Target target,
                                   @RequestParam(value = "key", required = false) String key,
                                   @RequestBody CustomMessage m) {
        String k1 = key != null ? key : rabbitProps.topicKeyCons1;
        String k2 = key != null ? key : rabbitProps.topicKeyCons2;

        switch (target) {
            case cons1 -> rabbit.convertAndSend(rabbitProps.topicEx, k1, m);
            case cons2 -> rabbit.convertAndSend(rabbitProps.topicEx, k2, m);
            case both -> {
                rabbit.convertAndSend(rabbitProps.topicEx, k1, m);
                rabbit.convertAndSend(rabbitProps.topicEx, k2, m);
            }
        }
        return ResponseEntity.accepted().body("topic→" + target);
    }

    @PostMapping("/fanout/{target}")
    public ResponseEntity<?> fanout(@PathVariable Target target, @RequestBody CustomMessage m) {
        rabbit.convertAndSend(rabbitProps.fanoutEx, "", m);
        return ResponseEntity.accepted().body("fanout broadcast");
    }

    @GetMapping("/health")
    public String health() { return "OK"; }
}
