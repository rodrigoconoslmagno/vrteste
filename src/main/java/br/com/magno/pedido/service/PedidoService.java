package br.com.magno.pedido.service;

import br.com.magno.pedido.entidade.Pedido;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PedidoService {

    private final RabbitTemplate rabbitTemplate;
    private final Map<UUID, String> statusRepo = new ConcurrentHashMap<>();
    private final Queue filaPrincipal;
    private final Queue filaSucesso;
    private final Queue filaFalha;

    public PedidoService(RabbitTemplate rabbitTemplate, 
            Queue filaPrincipal, Queue filaSucesso, Queue filaFalha) {
		this.rabbitTemplate = rabbitTemplate;
		this.filaPrincipal = filaPrincipal;
		this.filaSucesso = filaSucesso;
		this.filaFalha = filaFalha;
	}

    public void enviarPedido(Pedido pedido) {
        statusRepo.put(pedido.getId(), "AGUARDANDO PROCESSO");
        rabbitTemplate.convertAndSend(filaPrincipal.getName(), pedido);
    }

    public void atualizarEPublicarStatus(UUID idPedido, String status, String mensagemErro) {
        statusRepo.put(idPedido, status);

        Map<String, Object> payload = new HashMap<>();
        payload.put("idPedido", idPedido);
        payload.put("status", status);
        if (mensagemErro != null) {
        	payload.put("mensagemErro", mensagemErro);
        }

        String nomeFila = status.equals("SUCESSO") ? filaSucesso.getName() : filaFalha.getName();
        
        rabbitTemplate.convertAndSend(nomeFila, payload);
    }

    public String buscarStatus(UUID id) {
        return statusRepo.getOrDefault(id, "NÃO ENCONTRADO");
    }
}