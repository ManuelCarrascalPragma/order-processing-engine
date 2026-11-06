package co.com.pragma.orderengine.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdempotencyRecordTest {

    @Test
    void shouldHoldKeyStatusAndBody() {
        IdempotencyRecord record = new IdempotencyRecord("key-1", 201, "{\"id\":\"ord-1\"}");

        assertEquals("key-1", record.key());
        assertEquals(201, record.statusCode());
        assertEquals("{\"id\":\"ord-1\"}", record.responseBody());
    }

    @Test
    void shouldRejectNullKey() {
        assertThrows(NullPointerException.class,
                () -> new IdempotencyRecord(null, 201, "{}"));
    }

    @Test
    void shouldRejectNullBody() {
        assertThrows(NullPointerException.class,
                () -> new IdempotencyRecord("key-1", 201, null));
    }
}
