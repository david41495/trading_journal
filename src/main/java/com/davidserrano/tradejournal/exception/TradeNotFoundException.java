package com.davidserrano.tradejournal.exception;

public class TradeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TradeNotFoundException(Long id) {
        super("Trade not found with ID: " + id);
    }
}