package co.com.pragma.orderengine.infrastructure.entry_points.web;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.exception.PaymentFailedException;
import co.com.pragma.orderengine.domain.ports.in.ICheckoutOrderUseCase;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private IOrderRepositoryPort orderRepository;

    @Test
    void shouldReturn201WhenCheckoutSucceeds() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@example.com\",\"amount\":150.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("client@example.com"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"amount\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void shouldReturn402WhenPaymentFails() throws Exception {
        doThrow(new PaymentFailedException("Payment processing failed"))
                .when(checkoutOrderUseCase).execute(any(Order.class));

        mockMvc.perform(post("/api/v1/orders/checkout")
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
}
