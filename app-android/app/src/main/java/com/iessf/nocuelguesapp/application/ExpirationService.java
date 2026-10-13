package com.iessf.nocuelguesapp.application;

import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.domain.repository.ExpirationRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Use cases shared by the main screen and the form. */
public final class ExpirationService {
    private static final int WEEK_DAYS = 7;
    private final ExpirationRepository repository;

    public ExpirationService(ExpirationRepository repository) {
        this.repository = repository;
    }

    public List<ExpirationItem> getAll() {
        List<ExpirationItem> items = new ArrayList<>(repository.getAll());
        items.sort(ExpirationItem.DISPLAY_ORDER);
        return items;
    }

    public ExpirationItem findById(String id) {
        return repository.findById(id);
    }

    public void save(ExpirationItem item) {
        repository.save(item);
    }

    public void delete(String id) {
        repository.delete(id);
    }

    public Summary summarize(List<ExpirationItem> items, LocalDate today) {
        int overdue = 0;
        int thisWeek = 0;
        for (ExpirationItem item : items) {
            int days = item.getDaysUntilExpiration(today);
            if (days < 0) {
                overdue++;
            } else if (days <= WEEK_DAYS) {
                thisWeek++;
            }
        }
        return new Summary(overdue, thisWeek, items.size());
    }

    public static final class Summary {
        private final int overdue;
        private final int thisWeek;
        private final int total;

        private Summary(int overdue, int thisWeek, int total) {
            this.overdue = overdue;
            this.thisWeek = thisWeek;
            this.total = total;
        }

        public int getOverdue() {
            return overdue;
        }

        public int getThisWeek() {
            return thisWeek;
        }

        public int getTotal() {
            return total;
        }
    }
}
