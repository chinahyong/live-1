package tv.live.bx.ui;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;

import com.efeizao.bx.R;
import tv.live.bx.adapters.SelectModuleGridAdapter;
import tv.live.bx.adapters.SelectModuleGridAdapter.ISelectGridItemOnClick;

/**
 * 板块选择对话框 Function: TODO ADD FUNCTION. <br/>
 * @author Administrator
 * @version
 * @since JDK 1.6
 */
@SuppressLint("ResourceAsColor")
public class SelectMoudleDialog {
	private Context context;
	private Dialog dialog;
	private Button mCancleBtn;
	private GridView mGridView;
	private SelectModuleGridAdapter adapter;
	private OnItemClickListener itemClickListener;
	private List<Map<String, String>> giftsData;

	private Display display;

	public SelectMoudleDialog(Context context, List<Map<String, String>> giftsData,
			OnItemClickListener itemClickListener) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
		this.itemClickListener = itemClickListener;
		this.giftsData = giftsData;
	}

	public SelectMoudleDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_moudle_layou, null);

		mCancleBtn = (Button) view.findViewById(R.id.cancel);
		mCancleBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		mGridView = (GridView) view.findViewById(R.id.gridView);
		// 设置Dialog最小宽度为屏幕宽度
		view.setMinimumWidth(display.getWidth());

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
		dialog.setContentView(view);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);

		adapter = new SelectModuleGridAdapter(context, giftsData, new ISelectGridItemOnClick() {

			@Override
			public void onClick(ViewGroup parent, View v, int position) {
				itemClickListener.onClick(position);
				dialog.dismiss();
			}
		});

		mGridView.setAdapter(adapter);
		return this;
	}

	/** 设置礼物名称 */
	// public SelectMoudleDialog setTitle(String title) {
	// giftName.setText(title);
	// return this;
	// }
	//
	/** 更新数据 */
	public SelectMoudleDialog setListData(List<Map<String, String>> giftsData) {
		this.giftsData = giftsData;
		if (adapter != null) {
			adapter.setData(giftsData);
		}
		return this;
	}

	public SelectMoudleDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public SelectMoudleDialog setCanceledOnTouchOutside(boolean cancel) {
		dialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	public void show() {
		dialog.show();
	}

	public interface OnItemClickListener {
		void onClick(int position);
	}
}
