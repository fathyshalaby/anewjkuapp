package org.voidsink.anewjkuapp;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;

import java.text.DateFormat;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

/**
 * Created by paul on 06.09.2014.
 */
public class MenuCard extends MenuBaseCard {


    public MenuCard(final Context c, Mensa mensa, MensaDay day, MensaMenu menu) {
        super(c, mensa, day, menu);

        this.mMensa = mensa;
        this.mDay = day;
        this.mMenu = menu;

        if (mMenu != null && !mMenu.getName().isEmpty()) {
            // init header
            CardHeader header = new CardHeader(new ContextThemeWrapper(c, R.style.AppTheme));

//        header.setPopupMenu(R.menu.menu_card_popup_menu, new CardHeader.OnClickCardHeaderPopupMenuListener(){
//            @Override
//            public void onMenuItemClick(BaseCard card, MenuItem item) {
//                switch (item.getItemId()) {
//                    default: Toast.makeText(mContext, "Click on " + item.getTitle(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

            header.setTitle(mMenu.getName());
            addCardHeader(header);
        }
    }

    @Override
    protected int getInnerLayoutResId() {
        return R.layout.mensa_menu_item;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView name = (TextView) view.findViewById(R.id.mensa_menu_item_name);
        TextView soup = (TextView) view.findViewById(R.id.mensa_menu_item_soup);
        TextView meal = (TextView) view.findViewById(R.id.mensa_menu_item_meal);
        TextView price = (TextView) view.findViewById(R.id.mensa_menu_item_price);
        TextView oehBonus = (TextView) view.findViewById(R.id.mensa_menu_item_oeh_bonus);
        View chip = view.findViewById(R.id.mensa_menu_chip);

//
//        String menuName = mMenu.getName();
//        if (menuName != null && !menuName.isEmpty()) {
//            name.setText(menuName);
//            name.setVisibility(View.VISIBLE);
//        } else {
        name.setVisibility(View.GONE);
//        }
        String menuSoup = mMenu.getSoup();
        if (menuSoup != null && !menuSoup.isEmpty()) {
            soup.setText(menuSoup);
            soup.setVisibility(View.VISIBLE);
        } else {
            soup.setVisibility(View.GONE);
        }
        meal.setText(mMenu.getMeal());
        if (mMenu.getPrice() > 0) {
            price.setText(String.format("%.2f €",
                    mMenu.getPrice()));
            price.setVisibility(View.VISIBLE);

            if (mMenu.getOehBonus() > 0) {
                oehBonus.setText(String.format(
                        "inkl %.2f € ÖH Bonus", mMenu.getOehBonus()));
                oehBonus.setVisibility(View.VISIBLE);
            } else {
                oehBonus.setVisibility(View.GONE);
            }
        } else {
            price.setVisibility(View.GONE);
            oehBonus.setVisibility(View.GONE);
        }
        chip.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_LVA);
        chip.setVisibility(View.GONE);
    }

}
