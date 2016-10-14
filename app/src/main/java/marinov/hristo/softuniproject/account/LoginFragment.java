package marinov.hristo.softuniproject.account;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.models.User;
import marinov.hristo.softuniproject.utils.AESHelper;
import marinov.hristo.softuniproject.utils.DeveloperKey;

public class LoginFragment extends Fragment implements OnClickListener {

    Editor editorLogin;
    SharedPreferences prefLogin;
    private TextInputEditText mUser, mPassword;

    public interface ILogin {
        public void onLogin(int action);
    }

    ILogin callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callback = (ILogin) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_login, container, false);

        mUser = (TextInputEditText) view.findViewById(R.id.username);
        mPassword = (TextInputEditText) view.findViewById(R.id.password);

        Button mSubmit = (Button) view.findViewById(R.id.login);
        Button mRegister = (Button) view.findViewById(R.id.register);

        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);

        prefLogin = getActivity().getSharedPreferences("activity_login", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();

        return view;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.login:
                int usernameLength = mUser.getText().length();
                int passwordLength = mPassword.getText().length();
                String getName = mUser.getText().toString();
                String encryptedPass;
                try {
                    encryptedPass = AESHelper.encrypt(DeveloperKey.PASSWORD_KEY, mPassword.getText().toString());
                } catch (Exception e) {
                    encryptedPass = mPassword.getText().toString();
                    e.printStackTrace();
                }

                if (usernameLength == 0) {
                    mUser.setError(getResources().getString(R.string.enter_username));
                }

                if (passwordLength == 0) {
                    mPassword.setError(getResources().getString(R.string.enter_password));
                }

                if (usernameLength > 0 && passwordLength > 0) {
                    new AttemptLogin(getName, encryptedPass).execute();
                }
                break;
            case R.id.register:
                if (callback != null)
                    callback.onLogin(R.string.register);
                break;
            default:
                break;
        }
    }

    private class AttemptLogin extends AsyncTask<String, Integer, Void> {

        private long userId;
        private List<User> user;
        private String username, password;
        private ProgressDialog dialogTask;

        public AttemptLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    dialogTask = new ProgressDialog(getActivity());
                    dialogTask.setMessage(getResources().getString(R.string.login_wait));
                    dialogTask.setIndeterminate(true);
                    dialogTask.setProgressNumberFormat(null);
                    dialogTask.setProgressPercentFormat(null);
                    dialogTask.setCancelable(false);
                    dialogTask.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialogTask.show();
                }
            });
        }

        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(String... args) {

            user = User.findWithQuery(User.class, "Select * from user where username = ?", username);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialogTask.dismiss();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialogTask.dismiss();

            if (user.size() == 0) {
                Toast.makeText(getActivity(), R.string.username_not_found, Toast.LENGTH_LONG).show();
            } else {
                if (password.equals(user.get(0).getPassword())) {
                    userId = user.get(0).getId();
                    editorLogin.putLong("userId", userId);
                    editorLogin.commit();

                    Toast.makeText(getActivity(), R.string.login_success, Toast.LENGTH_LONG).show();

                    if (callback != null)
                        callback.onLogin(R.string.profile);
                } else {
                    Toast.makeText(getActivity(), R.string.wrong_pass, Toast.LENGTH_LONG).show();
                }
            }

            hideKeyboard();
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
