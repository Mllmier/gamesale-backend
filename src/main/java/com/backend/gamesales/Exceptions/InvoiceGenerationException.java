package com.backend.gamesales.Exceptions;

public class InvoiceGenerationException extends RuntimeException {
    public InvoiceGenerationException(String message) {
      super(message);
    }
}
