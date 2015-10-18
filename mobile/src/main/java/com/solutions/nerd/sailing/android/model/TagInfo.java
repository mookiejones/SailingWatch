package com.firstmate.android.model;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by cberman on 12/16/2014.
 */
public class TagInfo implements Parcelable {
    private String mDescription;
    private String mCategory;
    private Uri mPictureUri;
    private String mAddress1;
    private String mAddress2;
    private Location mLocation;
    private long mId = -1;

    public static final Creator<TagInfo> CREATOR = new Creator<TagInfo>() {
        @Override
        public TagInfo createFromParcel(Parcel parcel) {
            return new TagInfo(parcel);
        }

        @Override
        public TagInfo[] newArray(int size) {
            return new TagInfo[size];
        }

    };

    public TagInfo(long id) {
        mId = id;
    }

    private Context mContext;
    public TagInfo(Context context) {

        mContext = context;

    }

    public TagInfo(Parcel data) {
        setId(data.readLong());
        setDescription(data.readString());
        setCategory(data.readString());
        setPictureUri(data.readString());
        setAddress1(data.readString());
        setAddress2(data.readString());
        setLatitude(data.readDouble());
        setLongitude(data.readDouble());
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public Uri getPictureUri() {
        return mPictureUri;
    }

    public void setPictureUri(Uri pictureUri) {
        this.mPictureUri = pictureUri;
    }

    public void setPictureUri(String uri) {
        setPictureUri(Uri.parse(uri));
    }

    public String getAddress1() {
        return mAddress1;
    }

    public void setAddress1(String address1) {
        this.mAddress1 = address1;
    }

    public String getAddress2() {
        return mAddress2;
    }

    public void setAddress2(String address2) {
        this.mAddress2 = address2;
    }

    public void setLocation(Location location){
        mLocation = location;

    }
    public Location getLocation(){
        return mLocation;
    }

    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public LatLng getLatLng(){
        return new LatLng(this.mLocation.getLatitude(),this.mLocation.getLongitude());
    }
    public void setLatitude(double latitude) {
        this.mLocation.setLatitude(latitude);
    }

    public double getLongitude() {
        return this.mLocation.getLongitude();
    }

    public void setLongitude(double longitude) {


        this.mLocation.setLongitude(longitude);


    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(getId());
        parcel.writeString(getDescription());
        parcel.writeString(getCategory());
        if (getPictureUri() == null)
            parcel.writeString("");
        else
            parcel.writeString(getPictureUri().getPath());
        parcel.writeString(getAddress1());
        parcel.writeString(getAddress2());
        parcel.writeDouble(getLatitude());
        parcel.writeDouble(getLongitude());
    }





}