package ie.bookeo.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ie.bookeo.activity.BookeoFolderFragment;
import ie.bookeo.activity.DeviceImagesFragement;
import ie.bookeo.activity.FolderViewFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    long id;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0)
        {
            fragment = new FolderViewFragment();
        }
        else if (position == 1)
        {
            fragment = new BookeoFolderFragment();
        }
        else if (position == 2)
        {
            fragment = new DeviceImagesFragement();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
        {
            title = "Device";
        }
        else if (position == 1)
        {
            title = "Bookeo";
        }
        else if (position == 2)
        {
            title = "Images";
        }
        return title;
    }

}