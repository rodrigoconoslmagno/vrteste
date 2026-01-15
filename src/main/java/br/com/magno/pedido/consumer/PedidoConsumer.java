package br.com.magno.pedido.consumer;

import java.util.Random;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import br.com.magno.pedido.entidade.Pedido;
import br.com.magno.pedido.exception.ExcecaoDeProcessamento;
import br.com.magno.pedido.service.PedidoService;

@Component
public class PedidoConsumer {
	
	private final PedidoService pedidoService;

    public PedidoConsumer(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @RabbitListener(queues = "#{filaPrincipal.name}")
    public void processarPedido(Pedido pedido) throws InterruptedException {
        System.out.println("Iniciando processamento: {}" + pedido.getId());
        
        Thread.sleep(new Random().nextInt(2000) + 1000); 
        
        try {
            pedidoService.atualizarEPublicarStatus(pedido.getId(), "SUCESSO", null);
        } catch (ExcecaoDeProcessamento e) {
            pedidoService.atualizarEPublicarStatus(pedido.getId(), "FALHA", e.getMessage());
            throw e;
        }
    }

}
