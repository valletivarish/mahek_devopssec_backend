package com.eventmanager.eventrsvp.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void resourceNotFoundExceptionWithMessageConstructor() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Event not found");
        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void resourceNotFoundExceptionWithIdConstructor() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Event", 42L);
        assertEquals("Event not found with ID: 42", ex.getMessage());
    }

    @Test
    void resourceNotFoundExceptionIsRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void badRequestExceptionWithMessage() {
        BadRequestException ex = new BadRequestException("Duplicate email");
        assertEquals("Duplicate email", ex.getMessage());
    }

    @Test
    void badRequestExceptionIsRuntimeException() {
        BadRequestException ex = new BadRequestException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void resourceNotFoundExceptionWithZeroId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Category", 0L);
        assertEquals("Category not found with ID: 0", ex.getMessage());
    }

    @Test
    void badRequestExceptionWithEmptyMessage() {
        BadRequestException ex = new BadRequestException("");
        assertEquals("", ex.getMessage());
    }
}
