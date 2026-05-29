package com.example.tastetestawdb.exception;

/**
 * Exceptie aruncata cand o resursa ceruta nu exista in baza de date.
 * Este tradusa de {@link GlobalExceptionHandler} intr-un raspuns HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " inexistent(a) pentru identificatorul: " + id);
    }
}
