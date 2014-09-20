package org.voidsink.anewjkuapp.fragment;

import android.graphics.Color;

import org.voidsink.anewjkuapp.MensaDayTabItem;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MensaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        final int indicatorColor = getResources().getColor(android.R.color.white);
        final int dividerColor = getResources().getColor(R.color.primary);

        if (PreferenceWrapper.getGroupMenuByDay(getContext())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1); // correct date for loop
            // add days until next friday
            do {
                // increment day
                cal.add(Calendar.DATE, 1);
                // do not add weekend (no menu)
                if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    mTabs.add(new MensaDayTabItem(cal.getTime(), CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
                }
            } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY);
        } else {
            mTabs.add(new SlidingTabItem("Classic", MensaClassicFragment.class, indicatorColor, dividerColor));
            mTabs.add(new SlidingTabItem("Choice", MensaChoiceFragment.class, indicatorColor, dividerColor));
            mTabs.add(new SlidingTabItem("KHG", MensaKHGFragment.class, indicatorColor, dividerColor));
            mTabs.add(new SlidingTabItem("Raab", MensaRaabFragment.class, indicatorColor, dividerColor));
        }
    }
}
