package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes de persistência para ProdutoInsumoRepository")
class ProdutoInsumoRepositoryDataJpaTest {

    @Autowired
    private ProdutoInsumoRepository repository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaProdutoRepository categoriaProdutoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    private Produto novoProduto(CategoriaProduto categoria, String nome) {
        Produto produto = new Produto();
        produto.setCategoriaProduto(categoria);
        produto.setNome(nome);
        produto.setStatus(StatusAtivo.ATIVO);
        return produtoRepository.save(produto);
    }

    private Insumo novoInsumo(CategoriaInsumo categoria, String nome) {
        Insumo insumo = new Insumo();
        insumo.setCategoria(categoria);
        insumo.setNome(nome);
        insumo.setQuantidadeAtual(BigDecimal.ZERO);
        insumo.setQuantidadeMinima(BigDecimal.ZERO);
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setStatus(StatusAtivo.ATIVO);
        insumo.setCustoUnitario(new BigDecimal("6.5000"));
        return insumoRepository.save(insumo);
    }

    private void ligar(Produto produto, Insumo insumo) {
        ProdutoInsumo linha = new ProdutoInsumo();
        linha.setProduto(produto);
        linha.setInsumo(insumo);
        linha.setQuantidadeUtilizada(new BigDecimal("60.000"));
        linha.setUnidade(UnidadeMedida.G);
        repository.save(linha);
    }

    @Test
    @DisplayName("Deve carregar as fichas de vários produtos com o insumo numa consulta")
    void carregaFichasDeVariosProdutosComInsumo() {
        CategoriaProduto categoriaProduto = new CategoriaProduto();
        categoriaProduto.setNome("Doces");
        categoriaProduto = categoriaProdutoRepository.save(categoriaProduto);
        CategoriaInsumo categoriaInsumo = new CategoriaInsumo();
        categoriaInsumo.setNome("Farináceos");
        categoriaInsumo = categoriaInsumoRepository.save(categoriaInsumo);

        Produto bolo = novoProduto(categoriaProduto, "Bolo de pote");
        Produto brigadeiro = novoProduto(categoriaProduto, "Brigadeiro");
        Produto semFicha = novoProduto(categoriaProduto, "Torta");
        Insumo farinha = novoInsumo(categoriaInsumo, "Farinha de trigo");
        Insumo acucar = novoInsumo(categoriaInsumo, "Açúcar");
        ligar(bolo, farinha);
        ligar(bolo, acucar);
        ligar(brigadeiro, acucar);
        repository.flush();

        List<ProdutoInsumo> fichas = repository.findComInsumoByProdutoIds(
                List.of(bolo.getIdProduto(), brigadeiro.getIdProduto(), semFicha.getIdProduto()));

        assertThat(fichas).hasSize(3);
        assertThat(fichas).extracting(linha -> linha.getInsumo().getCustoUnitario())
                .allSatisfy(custo -> assertThat(custo).isEqualByComparingTo("6.5000"));
        assertThat(fichas).extracting(linha -> linha.getProduto().getIdProduto())
                .containsOnly(bolo.getIdProduto(), brigadeiro.getIdProduto());
    }
}
