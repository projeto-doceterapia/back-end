package br.com.doceterapia.api.exception;

public class NotificacaoTesteCooldownException extends RuntimeException {

    private final long segundosRestantes;

    public NotificacaoTesteCooldownException(long segundosRestantes) {
        super("Aguarde para enviar outra notificação de teste.");
        this.segundosRestantes = segundosRestantes;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }
}
