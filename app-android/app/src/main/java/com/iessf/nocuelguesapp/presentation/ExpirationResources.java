package com.iessf.nocuelguesapp.presentation;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.domain.model.Category;
import com.iessf.nocuelguesapp.domain.model.Urgency;

/** Traduce valores del modelo a textos, iconos y fondos de Android. */
public final class ExpirationResources {
    private ExpirationResources() { }

    @StringRes
    public static int categoryLabel(Category category) {
        switch (category) {
            case DOCUMENTS: return R.string.category_documents;
            case LICENSES: return R.string.category_licenses;
            case INSURANCE: return R.string.category_insurance;
            case WARRANTIES: return R.string.category_warranties;
            case HEALTH: return R.string.category_health;
            case SUBSCRIPTIONS: return R.string.category_subscriptions;
            default: return R.string.category_other;
        }
    }

    @DrawableRes
    public static int categoryIcon(Category category) {
        switch (category) {
            case DOCUMENTS: return R.drawable.ic_category_documents;
            case LICENSES: return R.drawable.ic_category_licenses;
            case INSURANCE: return R.drawable.ic_category_insurance;
            case WARRANTIES: return R.drawable.ic_category_warranties;
            case HEALTH: return R.drawable.ic_category_health;
            case SUBSCRIPTIONS: return R.drawable.ic_category_subscriptions;
            default: return R.drawable.ic_category_other;
        }
    }

    @StringRes
    public static int urgencyLabel(Urgency urgency) {
        switch (urgency) {
            case HIGH: return R.string.urgency_high;
            case MEDIUM: return R.string.urgency_medium;
            default: return R.string.urgency_low;
        }
    }

    @DrawableRes
    public static int urgencyBackground(Urgency urgency) {
        switch (urgency) {
            case HIGH: return R.drawable.bg_badge_high;
            case MEDIUM: return R.drawable.bg_badge_medium;
            default: return R.drawable.bg_badge_low;
        }
    }
}
