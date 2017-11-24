package tv.live.bx.imageloader.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.target.ViewTarget;
import com.efeizao.bx.R;

/**
 * Created by Administrator on 2017/5/23.
 */

public class MyGlideModule implements GlideModule {
	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		ViewTarget.setTagId(R.id.glide_tag_id);
	}

	@Override
	public void registerComponents(Context context, Glide glide) {

	}
}
