package marinov.hristo.softuniproject.account;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import marinov.hristo.softuniproject.R;

public class AccountActivity extends AppCompatActivity implements LoginFragment.ILogin, RegisterFragment.IRegister, ProfileFragment.IProfile {

    long userId;
    Editor editorLogin;
    SharedPreferences prefLogin;
    LoginFragment loginFragment;
    RegisterFragment registerFragment;
    ProfileFragment profileFragment;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        fragmentManager = getSupportFragmentManager();

        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();
        profileFragment = new ProfileFragment();

        prefLogin = getSharedPreferences("activity_login", MODE_PRIVATE);
        editorLogin = prefLogin.edit();

        userId = prefLogin.getLong("userId", -1);

        if (userId == -1) {
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, loginFragment).commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, profileFragment).commit();
        }
    }

    public void changeFragment(int action) {
        switch (action) {
            case R.string.login:
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, loginFragment).commit();

                break;
            case R.string.register:
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, registerFragment).commit();

                break;
            case R.string.profile:
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, profileFragment).commit();

                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLogin(int action) {
        changeFragment(action);
    }

    @Override
    public void onRegister(int action) {
        changeFragment(action);
    }

    @Override
    public void onProfile(int action) {
        changeFragment(action);
    }

    @Override
    public void onBackPressed() {
        if (loginFragment.isVisible() || profileFragment.isVisible()) {
            super.onBackPressed();
        } else {
            changeFragment(R.string.login);
        }
    }
}
