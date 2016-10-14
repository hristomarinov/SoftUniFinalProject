package marinov.hristo.softuniproject.account;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.models.User;
import marinov.hristo.softuniproject.utils.AESHelper;
import marinov.hristo.softuniproject.utils.DeveloperKey;

public class RegisterFragment extends Fragment implements OnClickListener {

    boolean success = false;
    Editor editorLogin;
    SharedPreferences prefLogin;
    private EditText mUsername, mPassword, mConfirmPassword, mEmail, mFirstName, mLastName;

    public interface IRegister {
        public void onRegister(int action);
    }

    IRegister callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callback = (IRegister) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_register, container, false);

        mUsername = (EditText) view.findViewById(R.id.username);
        mPassword = (EditText) view.findViewById(R.id.password_reg);
        mConfirmPassword = (EditText) view.findViewById(R.id.confirm_password);
        mEmail = (EditText) view.findViewById(R.id.email);
        mFirstName = (EditText) view.findViewById(R.id.first_name);
        mLastName = (EditText) view.findViewById(R.id.last_name);

        Button mRegister = (Button) view.findViewById(R.id.register_reg);
        mRegister.setOnClickListener(this);

        prefLogin = getActivity().getSharedPreferences("activity_login", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        //Get parameters from the fields
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        String confirmPassword = mConfirmPassword.getText().toString();
        String email = mEmail.getText().toString();
        String first_name = mFirstName.getText().toString();
        String last_name = mLastName.getText().toString();


        if (username.equals("")) {
            mUsername.setError(getResources().getString(R.string.enter_username));
        }

        if (password.equals("")) {
            mPassword.setError(getResources().getString(R.string.enter_password));
        }

        if (confirmPassword.equals("")) {
            mConfirmPassword.setError(getResources().getString(R.string.enter_password_again));
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), R.string.pass_not_match, Toast.LENGTH_LONG).show();
            return;
        }

        if ((!username.equals(""))
                && (!password.equals(""))
                && (!confirmPassword.equals(""))) {
            new CreateUser(username, password, email, first_name, last_name).execute();
        }

    }

    class CreateUser extends AsyncTask<Void, String, String> {

        private long userId;
        private String username, password, email, first_name, last_name;
        private ProgressDialog dialogTask;

        public CreateUser(String username, String password, String email, String first_name, String last_name) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.first_name = first_name;
            this.last_name = last_name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    dialogTask = new ProgressDialog(getActivity());
                    dialogTask.setMessage(getResources().getString(R.string.register_wait));
                    dialogTask.setIndeterminate(true);
                    dialogTask.setProgressNumberFormat(null);
                    dialogTask.setProgressPercentFormat(null);
                    dialogTask.setCancelable(false);
                    dialogTask.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialogTask.show();
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            // Check if username already exist
            List<User> userList = User.findWithQuery(User.class, "Select * from user where username = ?", username);

            if (userList.size() == 0) {
                String encryptedPass;
                try {
                    encryptedPass = AESHelper.encrypt(DeveloperKey.PASSWORD_KEY, password);
                } catch (Exception e) {
                    encryptedPass = password;
                    e.printStackTrace();
                }

                User user = new User(username, encryptedPass, email, "imagePath", first_name, last_name);
                user.save();
                userId = user.getId();

                editorLogin.putLong("userId", userId);
                editorLogin.commit();
                success = true;
            } else {
                success = false;
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialogTask.dismiss();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            dialogTask.dismiss();
            if (success) {
                Toast.makeText(getActivity(), R.string.register_success, Toast.LENGTH_LONG).show();
                if (callback != null)
                    callback.onRegister(R.string.login);
            } else {
                Toast.makeText(getActivity(), R.string.user_exist, Toast.LENGTH_LONG).show();
            }
        }
    }

}
