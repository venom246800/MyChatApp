package com.hackervenom.mychatapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Gravity;

public class TabsAccessor extends FragmentPagerAdapter
{

    public TabsAccessor(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0 :
                Chats chats = new Chats();
                return chats;
            case 1 :
                Groups groups = new Groups();
                return groups;
            case 2 :
                Contacts contacts = new Contacts();
                return contacts;
            case 3 :
                Request request = new Request();
                return request;
            default :
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0 :
                return "Chats";
            case 1 :
                return "Groups";
            case 2 :
                return "Contacts";
            case 3 :
                return "Requests";
            default :
                return null;
        }
    }
}
