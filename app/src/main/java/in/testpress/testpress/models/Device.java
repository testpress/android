package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable {
    protected String registrationId;
    protected String deviceId;

    public Device() {}

    public Device(Parcel parcel) {
        registrationId = parcel.readString();
        deviceId = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(registrationId);
        parcel.writeString(deviceId);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Device createFromParcel(Parcel parcel) {
            return new Device(parcel);
        }

        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    public String getRegistrationId() { return registrationId; }

    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
