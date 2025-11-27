package com.dsrl.consumer.model;

public record CustomMessage(
        String text,
        String sender,
        Long timestamp
) {}
