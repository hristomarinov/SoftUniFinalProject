package marinov.hristo.softuniproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

import java.util.List;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class User extends SugarRecord implements Parcelable {

    private String username;
    private String password;
    private String email;
    private String imagePath;
    private String firstName;
    private String lastName;

    public User() {

    }

    public User(String username, String password, String email, String imagePath, String firstName, String lastName) {
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.setImagePath(imagePath);
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    public List<Favourites> getFavourites() {

        return Favourites.find(Favourites.class, "user =?", String.valueOf(getId()));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
        email = in.readString();
        imagePath = in.readString();
        firstName = in.readString();
        lastName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(email);
        dest.writeString(imagePath);
        dest.writeString(firstName);
        dest.writeString(lastName);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}