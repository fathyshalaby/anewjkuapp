/*******************************************************************************
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/

package org.voidsink.anewjkuapp.fragment;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.update.ImportCalendarTask;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment2 extends BaseFragment implements WeekView.MonthChangeListener,
        WeekView.EventClickListener, WeekView.EventLongPressListener, DateTimeInterpreter {

    private static final String TAG = CalendarFragment2.class.getSimpleName();
    private static final SimpleDateFormat COLUMN_TITLE = new SimpleDateFormat("EEE dd.MM.");
    private ContentObserver mCalendarObserver;
    private WeekView mWeekView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_2, container,
                false);

        mWeekView = (WeekView) view.findViewById(R.id.weekView);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);
        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);
        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);
        // Set formatter for Date/Time
        mWeekView.setDateTimeInterpreter(this);

        // goto now
        mWeekView.goToToday();
        Calendar cal = Calendar.getInstance();
        mWeekView.goToHour(cal.get(Calendar.HOUR_OF_DAY));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.calendar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_calendar:
                Intent mUpdateService = new Intent(getActivity(), UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_CAL, true);
                getActivity().startService(mUpdateService);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mCalendarObserver = new CalendarContentObserver(new Handler());

        Account account = AppUtils.getAccount(getContext());

        String lvaCalId = CalendarUtils.getCalIDByName(getContext(), account, CalendarUtils.ARG_CALENDAR_COURSE, true);
        String examCalId = CalendarUtils.getCalIDByName(getContext(), account, CalendarUtils.ARG_CALENDAR_EXAM, true);

        if (lvaCalId == null || examCalId == null) {
            // listen to all changes
            getActivity().getContentResolver().registerContentObserver(
                    CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                            .appendPath("#").build(), false, mCalendarObserver);
        } else {
            getActivity().getContentResolver().registerContentObserver(
                    CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                            .appendPath(lvaCalId).build(), false, mCalendarObserver);
            getActivity().getContentResolver().registerContentObserver(
                    CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                            .appendPath(examCalId).build(), false, mCalendarObserver);
        }

    }

    @Override
    public void onStop() {
        getActivity().getContentResolver().unregisterContentObserver(
                mCalendarObserver);

        super.onStop();
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_CALENDAR;
    }

    @Override
    public void onEventClick(WeekViewEvent weekViewEvent, RectF rectF) {

    }

    @Override
    public void onEventLongPress(WeekViewEvent weekViewEvent, RectF rectF) {
        // show context menu

    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();

        Account mAccount = AppUtils.getAccount(getContext());
        if (mAccount == null) {
            return events;
        }

        final String calIDLva = CalendarUtils.getCalIDByName(getContext(),
                mAccount, CalendarUtils.ARG_CALENDAR_COURSE, true);
        final String calIDExam = CalendarUtils.getCalIDByName(getContext(),
                mAccount, CalendarUtils.ARG_CALENDAR_EXAM, true);

        if (calIDLva == null || calIDExam == null) {
            Log.w(TAG, "no events loaded, calendars not found");
            return events;
        }

        // fetch calendar colors
        final Map<String, Integer> mColors = new HashMap<>();
        final ContentResolver cr = getContext().getContentResolver();

        Cursor c = cr.query(CalendarContractWrapper.Calendars.CONTENT_URI(),
                new String[]{
                        CalendarContractWrapper.Calendars._ID(),
                        CalendarContractWrapper.Calendars.CALENDAR_COLOR()},
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                mColors.put(c.getString(0), c.getInt(1));
            }
            c.close();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, newYear);
        cal.set(Calendar.MONTH, newMonth - 1); // month index starts at 0 for jan
        cal.set(Calendar.DAY_OF_MONTH, 1);

        long now = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);

        long then = cal.getTimeInMillis();

        c = cr.query(
                CalendarContractWrapper.Events.CONTENT_URI(),
                ImportCalendarTask.EVENT_PROJECTION,
                "("
                        + CalendarContractWrapper.Events
                        .CALENDAR_ID()
                        + " = ? or "
                        + CalendarContractWrapper.Events
                        .CALENDAR_ID() + " = ? ) and "
                        + CalendarContractWrapper.Events.DTEND()
                        + " >= ? and "
                        + CalendarContractWrapper.Events.DTSTART()
                        + " <= ? and "
                        + CalendarContractWrapper.Events.DELETED()
                        + " != 1",
                new String[]{calIDExam, calIDLva,
                        Long.toString(now), Long.toString(then)},
                CalendarContractWrapper.Events.DTSTART() + " ASC");

        if (c == null) {
            return events;
        }

        try {
            while (c.moveToNext()) {
                Calendar startTime = Calendar.getInstance();
                startTime.setTimeInMillis(c.getLong(ImportCalendarTask.COLUMN_EVENT_DTSTART));
                Calendar endTime = Calendar.getInstance();
                endTime.setTimeInMillis(c.getLong(ImportCalendarTask.COLUMN_EVENT_DTEND));

                WeekViewEvent event = new WeekViewEvent(c.getLong(ImportCalendarTask.COLUMN_EVENT_ID), c.getString(ImportCalendarTask.COLUMN_EVENT_TITLE), startTime, endTime);

                // get accent color from theme
                TypedArray themeArray = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
                int color = themeArray.getColor(0, getContext().getResources().getColor(R.color.default_accent));
                themeArray.recycle();

                final String key = c.getString(ImportCalendarTask.COLUMN_EVENT_CAL_ID);
                if (mColors.containsKey(key)) {
                    color = mColors.get(key);
                }
                event.setColor(color);

                events.add(event);
            }
            c.close();
        } catch (Exception e) {
            Analytics.sendException(getContext(), e, false);
            events.clear();
        }

        return events;
    }

    @Override
    public String interpretDate(Calendar calendar) {
        return COLUMN_TITLE.format(calendar.getTime());
    }

    @Override
    public String interpretTime(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat timeFormat;

        boolean is24h = DateFormat.is24HourFormat(getContext());
        if (is24h) {
            timeFormat = new SimpleDateFormat("HH:mm");
        } else {
            timeFormat = new SimpleDateFormat("hh a");
        }

        return timeFormat.format(calendar.getTime());
    }

    private class CalendarContentObserver extends ContentObserver {

        public CalendarContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            mWeekView.notifyDatasetChanged();
        }
    }
}
