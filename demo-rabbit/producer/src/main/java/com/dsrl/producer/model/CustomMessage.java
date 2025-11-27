package com.dsrl.producer.model;

public record CustomMessage(
        String text,
        String sender,
        Long timestamp
) {}
