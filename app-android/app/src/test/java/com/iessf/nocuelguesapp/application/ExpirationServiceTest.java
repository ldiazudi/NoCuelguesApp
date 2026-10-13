package com.iessf.nocuelguesapp.application;

import com.iessf.nocuelguesapp.domain.model.Category;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.domain.model.Urgency;
import com.iessf.nocuelguesapp.domain.repository.ExpirationRepository;
import org.junit.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class ExpirationServiceTest {
    private final LocalDate today = LocalDate.of(2026, 10, 12);

    private ExpirationItem item(String name, int days, Urgency urgency) {
        return new ExpirationItem(name, today.plusDays(days), Category.DOCUMENTS, urgency, null);
    }

    @Test
    public void orderUsesDateThenUrgencyThenNameWithoutChangingRepositoryList() {
        ExpirationItem later = item("Later", 20, Urgency.HIGH);
        ExpirationItem low = item("Low", 3, Urgency.LOW);
        ExpirationItem highZ = item("Zeta", 3, Urgency.HIGH);
        ExpirationItem highA = item("alfa", 3, Urgency.HIGH);
        ExpirationItem expired = item("Expired", -1, Urgency.LOW);
        MemoryRepository repo = new MemoryRepository(later, low, highZ, highA, expired);
        List<ExpirationItem> sorted = new ExpirationService(repo).getAll();
        assertEquals(Arrays.asList(expired, highA, highZ, low, later), sorted);
        assertSame(later, repo.items.get(0));
    }

    @Test
    public void summaryIncludesTodayAndDaySevenButExcludesExpiredAndDayEight() {
        List<ExpirationItem> items = Arrays.asList(
                item("Expired", -1, Urgency.HIGH),
                item("Today", 0, Urgency.LOW),
                item("Day seven", 7, Urgency.MEDIUM),
                item("Day eight", 8, Urgency.HIGH));
        ExpirationService.Summary summary = new ExpirationService(new MemoryRepository())
                .summarize(items, today);
        assertEquals(1, summary.getOverdue());
        assertEquals(2, summary.getThisWeek());
        assertEquals(4, summary.getTotal());
    }

    @Test
    public void emptyListHasZeroCounts() {
        ExpirationService service = new ExpirationService(new MemoryRepository());
        assertTrue(service.getAll().isEmpty());
        ExpirationService.Summary summary = service.summarize(Collections.emptyList(), today);
        assertEquals(0, summary.getOverdue());
        assertEquals(0, summary.getThisWeek());
        assertEquals(0, summary.getTotal());
    }

    private static final class MemoryRepository implements ExpirationRepository {
        private final List<ExpirationItem> items;
        MemoryRepository(ExpirationItem... initial) {
            items = new ArrayList<>(Arrays.asList(initial));
        }
        public List<ExpirationItem> getAll() { return items; }
        public ExpirationItem findById(String id) {
            for (ExpirationItem item : items) if (item.getId().equals(id)) return item;
            return null;
        }
        public void save(ExpirationItem item) {
            delete(item.getId());
            items.add(item);
        }
        public void delete(String id) { items.removeIf(item -> item.getId().equals(id)); }
    }
}
