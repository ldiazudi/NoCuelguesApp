package com.iessf.nocuelguesapp;

import java.time.LocalDate;

/**
 * TEMPORARY: loads the prototype examples so the list can be tested before the form exists.
 * Delete this class and its call in MainActivity once items can be added from FormActivity.
 */
final class SampleData {

    private SampleData() {
    }

    static void seedIfEmpty(ExpirationItemRepository repository) {
        if (!repository.getAll().isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        repository.save(new ExpirationItem("Seguro del auto", today.minusDays(2),
                Category.INSURANCE, Urgency.HIGH, "Llamar a la aseguradora para renovar"));
        repository.save(new ExpirationItem("Carnet de conducir", today.plusDays(3),
                Category.LICENSES, Urgency.HIGH, null));
        repository.save(new ExpirationItem("Amoxicilina", today.plusDays(3),
                Category.HEALTH, Urgency.MEDIUM, "Botiquín del baño"));
        repository.save(new ExpirationItem("Pasaporte", today.plusDays(18),
                Category.DOCUMENTS, Urgency.HIGH, "Pedir turno en Migraciones antes de viajar"));
        repository.save(new ExpirationItem("Spotify Premium", today.plusDays(45),
                Category.SUBSCRIPTIONS, Urgency.LOW, null));
        repository.save(new ExpirationItem("Garantía notebook", today.plusDays(210),
                Category.WARRANTIES, Urgency.LOW, null));
    }
}
