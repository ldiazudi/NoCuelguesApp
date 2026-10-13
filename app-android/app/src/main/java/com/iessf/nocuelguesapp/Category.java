package com.iessf.nocuelguesapp;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Predefined category of an expiration item.
 */
public enum Category {
    DOCUMENTS(R.string.category_documents, R.drawable.ic_category_documents),
    LICENSES(R.string.category_licenses, R.drawable.ic_category_licenses),
    INSURANCE(R.string.category_insurance, R.drawable.ic_category_insurance),
    WARRANTIES(R.string.category_warranties, R.drawable.ic_category_warranties),
    HEALTH(R.string.category_health, R.drawable.ic_category_health),
    SUBSCRIPTIONS(R.string.category_subscriptions, R.drawable.ic_category_subscriptions),
    OTHER(R.string.category_other, R.drawable.ic_category_other);

    @StringRes
    private final int labelRes;
    @DrawableRes
    private final int iconRes;

    Category(@StringRes int labelRes, @DrawableRes int iconRes) {
        this.labelRes = labelRes;
        this.iconRes = iconRes;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }
}
