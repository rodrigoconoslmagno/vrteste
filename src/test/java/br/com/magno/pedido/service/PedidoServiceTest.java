package br.com.magno.pedido.service;

import br.com.magno.pedido.entidade.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class PedidoServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Queue filaPrincipal;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedidoExemplo;

    @BeforeEach
    void setUp() {
   
        pedidoExemplo = new Pedido();;
        
        pedidoExemplo.setId(UUID.randomUUID());
        pedidoExemplo.setProduto("Teclado Mecânico");
        pedidoExemplo.setQuantidade(2); 
        pedidoExemplo.setDataCriacao(LocalDateTime.now());
        
        lenient().when(filaPrincipal.getName()).thenReturn("pedidos.entrada.rodrigo-magno");
    }

    @Test
    @DisplayName("Deve publicar o pedido na fila correta e atualizar status para AGUARDANDO PROCESSO")
    void deveEnviarPedidoParaFilaComSucesso() {
        pedidoService.enviarPedido(pedidoExemplo);

        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("pedidos.entrada.rodrigo-magno"),
            any(Pedido.class)
        );
    }
}