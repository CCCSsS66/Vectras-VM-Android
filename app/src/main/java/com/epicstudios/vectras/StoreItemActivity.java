package com.epicstudios.vectras;

import android.app.Dialog;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.PowerManager;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.epicstudios.vectras.utils.FileUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.epicstudios.vectras.R;
import com.epicstudios.vectras.utils.UIUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.os.PowerManager;

public class StoreItemActivity extends AppCompatActivity {
	public StoreItemActivity activity;
	public String TAG = "StoreItemActivity";
	public static String icon, name, size, desc, descStr, prvMain, prv1, prv2, link;
	public TextView itemName, itemSize, itemDesc;
	public Button dBtn;
	public ImageView itemIcon, itemPrvMain, itemPrv1, itemPrv2;

	boolean isImageFitToScreen;

	@Override
	protected void onCreate(Bundle bundle) {
		activity = this;
		super.onCreate(bundle);
		setContentView(R.layout.activity_store_item);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		toolbar.setTitle(getString(R.string.app_name));
		itemIcon = findViewById(R.id.ivIcon);
		itemName = findViewById(R.id.textName);
		itemSize = findViewById(R.id.textSize);
		dBtn = findViewById(R.id.btn_download);
		itemDesc = findViewById(R.id.descTxt);
		itemPrvMain = findViewById(R.id.ivPrvMain);
		itemPrv1 = findViewById(R.id.ivPrv1);
		itemPrv2 = findViewById(R.id.ivPrv2);

		dBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				startDownload();
			}
		});
		itemName.setText(name);
		itemSize.setText(size);

		Glide.with(this).load(icon).into(itemIcon);
		Glide.with(this).load(prvMain).into(itemPrvMain);
		Glide.with(this).load(prv1).into(itemPrv1);
		Glide.with(this).load(prv2).into(itemPrv2);
		new Thread(new Runnable() {

			public void run() {

				BufferedReader reader = null;
				final StringBuilder builder = new StringBuilder();

				try {
					// Create a URL for the desired page
					URL url = new URL(desc); //My text file location
					//First open the connection
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(60000); // timing out in a minute

					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

					//t=(TextView)findViewById(R.id.TextView1); // ideally do this in onCreate()
					String str;
					while ((str = in.readLine()) != null) {
						builder.append(str);
					}
					in.close();
				} catch (Exception e) {
					itemDesc.setText("no internet connection");
					UIUtils.toastLong(StoreItemActivity.this, "check your internet connection");
					Log.d("VECTRAS", e.toString());
				}

				//since we are in background thread, to post results we have to go back to ui thread. do the following for that

				StoreItemActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						descStr = builder.toString(); // My TextFile has 3 lines
						itemDesc.setText(Html.fromHtml(descStr));
					}
				});

			}
		}).start();
		itemPrvMain.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePrvActivity.linkIv = prvMain;
				startActivity(new Intent(activity, ImagePrvActivity.class));
			}
		});
		itemPrv1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePrvActivity.linkIv = prv1;
				startActivity(new Intent(activity, ImagePrvActivity.class));
			}
		});
		itemPrv2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePrvActivity.linkIv = prv2;
				startActivity(new Intent(activity, ImagePrvActivity.class));
			}
		});
	}

	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;

	private void startDownload() {
		String url = link;
		new DownloadFileAsync().execute(url);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this, R.style.MainDialogTheme);
			mProgressDialog.setMessage("Downloading file..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;

			try {
				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d(TAG, "Lenght of file: " + lenghtOfFile);
				String fileName = URLUtil.guessFileName(link,null,null);
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(Config.sharedFolder+fileName);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
			}
			return null;

		}

		protected void onProgressUpdate(String... progress) {
			Log.d(TAG, progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			AlertDialog ad;
			ad = new AlertDialog.Builder(activity, R.style.MainDialogTheme).create();
			ad.setTitle("Downloaded Successfully!");
			String fileName = URLUtil.guessFileName(link,null,null);
			ad.setMessage("Downloaded to path: "+Config.sharedFolder+fileName+" boot vectras to check your downloads in QEMU VFAT partition.");
			ad.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			ad.show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}