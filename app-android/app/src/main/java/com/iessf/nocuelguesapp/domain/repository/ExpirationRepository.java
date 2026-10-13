package com.iessf.nocuelguesapp.domain.repository;

import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import java.util.List;

/** Data contract, independent from Android and from the storage format. */
public interface ExpirationRepository {
    List<ExpirationItem> getAll();
    /** Returns null when no item has that id. */
    ExpirationItem findById(String id);
    void save(ExpirationItem item);
    void delete(String id);
}
