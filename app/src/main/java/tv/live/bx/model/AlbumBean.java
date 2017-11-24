package tv.live.bx.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Live on 2017/4/25.
 * Description:EditPhotoActivity中图片适配EditPhotoAdapter需要的实体类
 */

public class AlbumBean implements Serializable, Parcelable {
	private int id;            //图片ID
	private String url;        //图片地址
	private String path;        //本地地址
	private int status;        //图片状态：审核中；过审

	public AlbumBean() {

	}

	private AlbumBean(Parcel parcel) {
		id = parcel.readInt();
		url = parcel.readString();
		path = parcel.readString();
		status = parcel.readInt();
	}

	public AlbumBean(int id, String url, String localPath, int status) {
		this.id = id;
		this.url = url;
		this.path = localPath;
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(id);
		parcel.writeString(url);
		parcel.writeString(path);
		parcel.writeInt(status);
	}

	public static final Parcelable.Creator<AlbumBean> CREATOR = new Creator<AlbumBean>() {
		@Override
		public AlbumBean createFromParcel(Parcel parcel) {
			return new AlbumBean(parcel);
		}

		@Override
		public AlbumBean[] newArray(int i) {
			return new AlbumBean[i];
		}
	};
}
