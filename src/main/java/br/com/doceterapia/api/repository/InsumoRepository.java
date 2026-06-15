package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.Insumo;

public interface InsumoRepository extends JpaRepository<Insumo, Integer> {

    Boolean existsByCategoriaIdCategoriaInsumo(Integer idCategoriaInsumo);
}
