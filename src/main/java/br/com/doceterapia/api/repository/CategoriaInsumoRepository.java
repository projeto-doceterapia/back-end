package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.CategoriaInsumo;

public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Integer> {

    Boolean existsByNome(String nome);

    Boolean existsByNomeAndIdCategoriaInsumoNot(String nome, Integer idCategoriaInsumo);
}
