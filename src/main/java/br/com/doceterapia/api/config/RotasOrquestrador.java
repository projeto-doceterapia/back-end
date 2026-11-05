package br.com.doceterapia.api.config;

import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * The only routes the orchestrator calls on this API, and so the only ones its
 * service credential may reach. A new orchestrator call must be added here.
 */
public final class RotasOrquestrador {

    public record Rota(HttpMethod metodo, String caminho) {
    }

    public static final List<Rota> ROTAS = List.of(
            new Rota(HttpMethod.GET, "/insumos/catalogo"),
            new Rota(HttpMethod.POST, "/insumos/obter-ou-criar"),
            new Rota(HttpMethod.GET, "/notas-fiscais/{chave}/status"),
            new Rota(HttpMethod.POST, "/estoque/movimentos"),
            new Rota(HttpMethod.GET, "/notificacoes/agenda"),
            new Rota(HttpMethod.POST, "/notificacoes/{id}/confirmacao-envio"),
            new Rota(HttpMethod.GET, "/categorias-insumo"),
            new Rota(HttpMethod.POST, "/notificacoes"),
            new Rota(HttpMethod.GET, "/configuracao-notificacao")
    );

    private RotasOrquestrador() {
    }
}
