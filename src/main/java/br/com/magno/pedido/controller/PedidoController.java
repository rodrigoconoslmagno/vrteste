package br.com.magno.pedido.controller;

import br.com.magno.pedido.entidade.Pedido;
import br.com.magno.pedido.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<?> criarPedido(@RequestBody Pedido pedido) {
        if (pedido.getProduto() == null || pedido.getProduto().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: O nome do produto é obrigatório.");
        }

        if (pedido.getQuantidade() <= 0) {
            return ResponseEntity.badRequest().body("Erro: A quantidade deve ser maior que zero.");
        }

        pedidoService.enviarPedido(pedido);

        Map<String, Object> response = new HashMap<>();
        response.put("id", pedido.getId());
        response.put("mensagem", "Pedido recebido e em processamento.");
        
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> consultarStatus(@PathVariable UUID id) {
        String status = pedidoService.buscarStatus(id);
        return ResponseEntity.ok(status);
    }
}