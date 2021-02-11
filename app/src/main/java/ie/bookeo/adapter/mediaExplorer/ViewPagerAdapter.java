package ie.bookeo.adapter.mediaExplorer;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(int position, Fragment fragment, String title) {
        mFragmentList.add(position, fragment);
        mFragmentTitleList.add(position, title);
    }

    public void dropFragment(int postion) {
        mFragmentList.remove(postion);
        mFragmentTitleList.remove(postion);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        String title = null;
//        if (position == 0)
//        {
//            title = "Device";
//        }
//        else if (position == 1)
//        {
//            title = "Bookeo";
//        }
//        else if (position == 2)
//        {
//            title = "Images";
//        }
//        else if (position == 3)
//        {
//            title = "Drive";
//        }
//        return title;
//    }

}