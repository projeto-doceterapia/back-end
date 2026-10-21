package br.com.doceterapia.api.service;

import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.UnidadeIncompativelException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes para ConversaoUnidadeService")
class ConversaoUnidadeServiceTest {

    private final ConversaoUnidadeService service = new ConversaoUnidadeService();

    @Test
    @DisplayName("Deve manter a quantidade quando a unidade é a mesma (KG para KG)")
    void mesmaUnidadeKg() {
        BigDecimal resultado = service.converter(BigDecimal.valueOf(10), UnidadeMedida.KG, UnidadeMedida.KG);
        assertEquals(0, BigDecimal.valueOf(10).compareTo(resultado));
    }

    @Test
    @DisplayName("Deve converter G para KG dividindo por 1000")
    void gramasParaKg() {
        BigDecimal resultado = service.converter(BigDecimal.valueOf(500), UnidadeMedida.G, UnidadeMedida.KG);
        assertEquals(0, new BigDecimal("0.5").compareTo(resultado));
    }

    @Test
    @DisplayName("Deve converter KG para G multiplicando por 1000")
    void kgParaGramas() {
        BigDecimal resultado = service.converter(BigDecimal.valueOf(2), UnidadeMedida.KG, UnidadeMedida.G);
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(resultado));
    }

    @Test
    @DisplayName("Deve converter ML para L dividindo por 1000")
    void mlParaLitro() {
        BigDecimal resultado = service.converter(BigDecimal.valueOf(1500), UnidadeMedida.ML, UnidadeMedida.L);
        assertEquals(0, new BigDecimal("1.5").compareTo(resultado));
    }

    @Test
    @DisplayName("Deve converter L para ML multiplicando por 1000")
    void litroParaMl() {
        BigDecimal resultado = service.converter(BigDecimal.valueOf(3), UnidadeMedida.L, UnidadeMedida.ML);
        assertEquals(0, BigDecimal.valueOf(3000).compareTo(resultado));
    }

    @Test
    @DisplayName("Deve manter a quantidade para UNIDADE com UNIDADE")
    void unidadeParaUnidade() {
        BigDecimal resultado = service.converter(BigDecimal.valueOf(7), UnidadeMedida.UNIDADE, UnidadeMedida.UNIDADE);
        assertEquals(0, BigDecimal.valueOf(7).compareTo(resultado));
    }

    @Test
    @DisplayName("Deve manter a quantidade para L com L e ML com ML")
    void mesmaUnidadeVolume() {
        assertEquals(0, BigDecimal.valueOf(4).compareTo(
                service.converter(BigDecimal.valueOf(4), UnidadeMedida.L, UnidadeMedida.L)));
        assertEquals(0, BigDecimal.valueOf(250).compareTo(
                service.converter(BigDecimal.valueOf(250), UnidadeMedida.ML, UnidadeMedida.ML)));
    }

    @Test
    @DisplayName("Deve manter a quantidade para G com G")
    void mesmaUnidadeGramas() {
        assertEquals(0, BigDecimal.valueOf(800).compareTo(
                service.converter(BigDecimal.valueOf(800), UnidadeMedida.G, UnidadeMedida.G)));
    }

    @Test
    @DisplayName("Deve rejeitar massa com volume (KG para L)")
    void massaComVolume() {
        assertThrows(UnidadeIncompativelException.class,
                () -> service.converter(BigDecimal.valueOf(1), UnidadeMedida.KG, UnidadeMedida.L));
    }

    @Test
    @DisplayName("Deve rejeitar volume com massa (ML para KG)")
    void volumeComMassa() {
        assertThrows(UnidadeIncompativelException.class,
                () -> service.converter(BigDecimal.valueOf(100), UnidadeMedida.ML, UnidadeMedida.KG));
    }

    @Test
    @DisplayName("Deve rejeitar contagem com massa (UNIDADE para KG)")
    void contagemComMassa() {
        assertThrows(UnidadeIncompativelException.class,
                () -> service.converter(BigDecimal.valueOf(5), UnidadeMedida.UNIDADE, UnidadeMedida.KG));
    }

    @Test
    @DisplayName("Deve rejeitar massa com contagem (G para UNIDADE)")
    void massaComContagem() {
        assertThrows(UnidadeIncompativelException.class,
                () -> service.converter(BigDecimal.valueOf(5), UnidadeMedida.G, UnidadeMedida.UNIDADE));
    }

    @Test
    @DisplayName("saoCompativeis deve refletir as grandezas")
    void compatibilidade() {
        assertTrue(service.saoCompativeis(UnidadeMedida.KG, UnidadeMedida.G));
        assertTrue(service.saoCompativeis(UnidadeMedida.L, UnidadeMedida.ML));
        assertTrue(service.saoCompativeis(UnidadeMedida.UNIDADE, UnidadeMedida.UNIDADE));
    }
}
