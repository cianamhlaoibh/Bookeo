package ie.bookeo.adapter.login;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ie.bookeo.view.login.LoginFragment;
import ie.bookeo.view.login.RegisterFragment;
/*
 *
 *https://www.youtube.com/watch?v=ayKMfVt2Sg4
 *
 */
public class LoginTabAdapter extends FragmentPagerAdapter {

    private Context context;
    int totalTabs;

    public LoginTabAdapter(@NonNull FragmentManager fm, Context context, int totalTabs) {
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                LoginFragment loginFragment = new LoginFragment();
                return loginFragment;
            case 1:
                RegisterFragment registerFragment = new RegisterFragment();
                return registerFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
        {
            title = "Login";
        }
        else if (position == 1)
        {
            title = "Register";
        }
        return title;
    }
}
