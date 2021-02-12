package ie.bookeo.adapter.mediaExplorer;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * References
 *
 * - Android Material Design working with Tabs
 * - URL - https://www.androidhive.info/2015/09/android-material-design-working-with-tabs/
 * - Creator - androidhive
 *
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


}