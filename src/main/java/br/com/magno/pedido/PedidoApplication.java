package br.com.magno.pedido;

import java.awt.EventQueue;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import br.com.magno.pedido.view.PedidoForm;

@SpringBootApplication
public class PedidoApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PedidoApplication.class)
                .headless(false)
                .run(args);

        // Abre a tela Swing
        EventQueue.invokeLater(() -> {
            PedidoForm form = new PedidoForm();
            form.setVisible(true);
        });
	}

}
