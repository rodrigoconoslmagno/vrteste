package br.com.magno.pedido.exception;

public class ExcecaoDeProcessamento extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ExcecaoDeProcessamento(String mensagem) {
        super(mensagem);
    }

    public ExcecaoDeProcessamento(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}