package com.iessf.nocuelguesapp;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Urgency level of an expiration item.
 * Declaration order matters: it is the priority used to sort items due on the same day.
 */
public enum Urgency {
    HIGH(R.string.urgency_high, R.drawable.bg_badge_high),
    MEDIUM(R.string.urgency_medium, R.drawable.bg_badge_medium),
    LOW(R.string.urgency_low, R.drawable.bg_badge_low);

    @StringRes
    private final int labelRes;
    @DrawableRes
    private final int badgeBackgroundRes;

    Urgency(@StringRes int labelRes, @DrawableRes int badgeBackgroundRes) {
        this.labelRes = labelRes;
        this.badgeBackgroundRes = badgeBackgroundRes;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    @DrawableRes
    public int getBadgeBackgroundRes() {
        return badgeBackgroundRes;
    }
}
