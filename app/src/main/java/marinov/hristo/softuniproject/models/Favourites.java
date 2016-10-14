package marinov.hristo.softuniproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */

public class Favourites extends SugarRecord implements Parcelable {

    private User user;
    private String articleId;
    private String articleName;
    private String imageURI;

    public Favourites() {

    }

    public Favourites(User user, String articleId, String articleName, String imageURI) {
        this.user = user;
        this.setArticleId(articleId);
        this.setArticleName(articleName);
        this.setImageURI(imageURI);
    }

    public String getUserId() {
        if (user == null)
            return "Unknown";

        return user.getUsername();
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    protected Favourites(Parcel in) {
        user = (User) in.readValue(User.class.getClassLoader());
        articleId = in.readString();
        articleName = in.readString();
        imageURI = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeString(articleId);
        dest.writeString(articleName);
        dest.writeString(imageURI);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Favourites> CREATOR = new Parcelable.Creator<Favourites>() {
        @Override
        public Favourites createFromParcel(Parcel in) {
            return new Favourites(in);
        }

        @Override
        public Favourites[] newArray(int size) {
            return new Favourites[size];
        }
    };
}