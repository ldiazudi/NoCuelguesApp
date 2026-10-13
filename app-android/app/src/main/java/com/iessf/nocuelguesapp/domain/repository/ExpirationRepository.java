package com.iessf.nocuelguesapp.domain.repository;

import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import java.util.List;

/** Contrato de datos, independiente de Android y del formato de almacenamiento. */
public interface ExpirationRepository {
    List<ExpirationItem> getAll();
    /** Devuelve null si ningún ítem tiene ese identificador. */
    ExpirationItem findById(String id);
    void save(ExpirationItem item);
    void delete(String id);
}
