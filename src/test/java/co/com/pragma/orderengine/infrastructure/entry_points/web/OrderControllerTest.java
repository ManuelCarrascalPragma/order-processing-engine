package co.com.pragma.orderengine.infrastructure.entry_points.web;

import co.com.pragma.orderengine.domain.model.IdempotencyRecord;
import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.domain.model.exception.OrderNotFoundException;
import co.com.pragma.orderengine.domain.model.exception.PaymentFailedException;
import co.com.pragma.orderengine.domain.model.exception.VersionConflictException;
import co.com.pragma.orderengine.domain.ports.in.ICheckoutOrderUseCase;
import co.com.pragma.orderengine.domain.ports.in.IReplaceOrderUseCase;
import co.com.pragma.orderengine.domain.ports.in.IUpdateOrderStatusUseCase;
import co.com.pragma.orderengine.domain.ports.out.IIdempotencyStorePort;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.infrastructure.entry_points.web.advice.GlobalExceptionHandler;
import co.com.pragma.orderengine.infrastructure.entry_points.web.mapper.IOrderWebMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({IOrderWebMapperImpl.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICheckoutOrderUseCase checkoutOrderUseCase;

    @MockBean
    private IUpdateOrderStatusUseCase updateOrderStatusUseCase;

    @MockBean
    private IReplaceOrderUseCase replaceOrderUseCase;

    @MockBean
    private IOrderRepositoryPort orderRepository;

    @MockBean
    private IIdempotencyStorePort idempotencyStore;


    @Test
    void shouldReturn201WhenCheckoutSucceeds() throws Exception {
        Order paid = new Order("ord-1", "client@example.com", Money.ofUsd(150.0),
                OrderStatus.PAID, "tx-1", 0L);
        when(idempotencyStore.findByKey("key-1")).thenReturn(Optional.empty());
        when(checkoutOrderUseCase.execute(any(Order.class))).thenReturn(paid);

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@example.com\",\"amount\":150.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("client@example.com"))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldReplayStoredResponseWhenIdempotencyKeyExists() throws Exception {
        String storedBody = "{\"id\":\"ord-1\",\"customerEmail\":\"client@example.com\","
                + "\"amount\":150.0,\"currency\":\"USD\",\"status\":\"PAID\","
                + "\"transactionId\":\"tx-1\",\"version\":0}";
        when(idempotencyStore.findByKey("key-dup"))
                .thenReturn(Optional.of(new IdempotencyRecord("key-dup", 201, storedBody)));

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Idempotency-Key", "key-dup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@example.com\",\"amount\":150.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ord-1"));
    }

    @Test
    void shouldReturn400WhenIdempotencyKeyHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@example.com\",\"amount\":150.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Idempotency-Key", "key-x")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"amount\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void shouldReturn402WhenPaymentFails() throws Exception {
        when(idempotencyStore.findByKey("key-p")).thenReturn(Optional.empty());
        doThrow(new PaymentFailedException("Payment processing failed"))
                .when(checkoutOrderUseCase).execute(any(Order.class));

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Idempotency-Key", "key-p")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@example.com\",\"amount\":150.0}"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.title").value("Payment Failed"));
    }


    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/missing"))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldReturn200WhenPatchStatusSucceeds() throws Exception {
        Order updated = new Order("ord-1", "client@example.com", Money.ofUsd(150.0),
                OrderStatus.CANCELLED, null, 2L);
        when(updateOrderStatusUseCase.execute(eq("ord-1"), eq(OrderStatus.CANCELLED), eq(1L)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/orders/ord-1/status")
                        .header("If-Match", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void shouldReturn412WhenVersionDoesNotMatch() throws Exception {
        when(updateOrderStatusUseCase.execute(any(), any(), anyLong()))
                .thenThrow(new VersionConflictException("Version mismatch"));

        mockMvc.perform(patch("/api/v1/orders/ord-1/status")
                        .header("If-Match", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.title").value("Version Conflict"));
    }

    @Test
    void shouldReturn400WhenIfMatchHeaderMissing() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/ord-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturn200WhenPutReplacesOrder() throws Exception {
        Order replaced = new Order("ord-1", "new@example.com", Money.ofUsd(200.0),
                OrderStatus.PAID, "tx-new", 3L);
        when(replaceOrderUseCase.execute(any(Order.class))).thenReturn(replaced);

        mockMvc.perform(put("/api/v1/orders/ord-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"amount\":200.0,"
                                + "\"status\":\"PAID\",\"transactionId\":\"tx-new\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerEmail").value("new@example.com"))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldReturn404WhenPutTargetDoesNotExist() throws Exception {
        when(replaceOrderUseCase.execute(any(Order.class)))
                .thenThrow(new OrderNotFoundException("Order not found: ord-1"));

        mockMvc.perform(put("/api/v1/orders/ord-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"amount\":200.0,"
                                + "\"status\":\"PAID\",\"transactionId\":\"tx-new\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order Not Found"));
    }
}
