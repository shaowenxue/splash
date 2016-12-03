package com.example.activity_splash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.ithemia.mobilesafe.utils.StreamTools;


public class SplashActivity extends Activity {
	protected static final int ENTER_HOME = 0;


	protected static final int SHOW_UPDATE_DIALOG = 1;
	
	
	protected static final int URL_ERRROR = 2;


	protected static final int NETWORK_ERROR = 3;


	protected static final int JSON_ERROR = 4;


	protected static final String TAG ="SplashActivity";


	private TextView tv_splash_version;
	private TextView tv_splash_updateinfo;
	
	private String description;
	private String apkurl;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case ENTER_HOME://进入主页面
				enterHome();
				break;
			case SHOW_UPDATE_DIALOG://显示升级对话框
				Log.i(TAG,"显示升级对话框");
				showUpdateDialog();
				break;
			case URL_ERRROR://URL错误
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();
				enterHome();
				break;
			case NETWORK_ERROR://网络连接错误
				Toast.makeText(getApplicationContext(), "网络连接错误", 0).show();
				enterHome();
				break;
			case JSON_ERROR://JSON错误
				Toast.makeText(getApplicationContext(), "JSON错误", 0).show();
				enterHome();
				break;
			}
		}		
	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        tv_splash_version = (TextView)findViewById(R.id.tv_splash_version);
        tv_splash_updateinfo = (TextView)findViewById(R.id.tv_splash_updateinfo);
        tv_splash_version.setText("版本号"+getAppVersion());
        //在子线程
        checkVersion();
        AlphaAnimation aa = new AlphaAnimation(0.2f,1.0f);
        aa.setDuration(500);
        findViewById(R.id.rl_splash_root).startAnimation(aa);
    }
//进入主页面
    private void enterHome() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
			startActivity(intent);
			finish(); 
		};
//对话框
	protected void showUpdateDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(false);//不让点击返回
		builder.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				enterHome();
			}
		});
		builder.setTitle("有新版本了");
		builder.setMessage(description);
		builder.setNegativeButton("立刻升级", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//判断是否有SD卡
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					//下载代码
					FinalHttp http = new FinalHttp();
					//下载mnt/sdcard/updata.apk
					http.download(apkurl, Environment.getExternalStorageDirectory().getAbsolutePath()+"/updata.apk", 
							new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									// TODO Auto-generated method stub
									super.onFailure(t, errorNo, strMsg);
									Toast.makeText(SplashActivity.this, "下载失败", 1).show();
									Log.i(TAG,"下载失败");
								}

								@Override
								public void onLoading(long count, long current) {
									// TODO Auto-generated method stub
									super.onLoading(count, current);
									int progress = (int)(current*100/count);
									tv_splash_updateinfo.setText("下载进度:"+progress+"%");
								}

								@Override
								public void onSuccess(File t) {
									// TODO Auto-generated method stub
									super.onSuccess(t);
									installAPK(t);
								}

								private void installAPK(File t) {
									// TODO Auto-generated method stub
									Intent intent = new Intent();
									intent.setAction("android.intent.action.VIEW");
									intent.addCategory("android.intent.category.DEFAULT");
									intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");
									startActivity(intent);
								}
								
							});
				}else{
					enterHome();
					Toast.makeText(getApplicationContext(), "SD卡不存在", 0).show();
				}
			}
			
		});
		builder.setPositiveButton("下次再说", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				enterHome();
				dialog.dismiss();
			}
			
		});
		builder.show();
	}

	private void checkVersion() {
		// TODO Auto-generated method stub
		new Thread(){
			public void run() {
				Message msg = Message.obtain();
				//开始的时间
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serviceurl));
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					
					int code = conn.getResponseCode();
					if(code == 200){
						InputStream is = conn.getInputStream();
						String result = StreamTools.readFromStream(is);
						JSONObject json = new JSONObject(result);
						String version =(String)json.get("version");
						description =(String)json.get("description");
						apkurl =(String)json.get("apkurl");
						
						if(getAppVersion().equals(version)){
							//版本一致 进入主页面
							msg.what = ENTER_HOME;
						}else{
							//升级弹出一个升级对话框
							msg.what = SHOW_UPDATE_DIALOG;
						}
					}
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg.what = URL_ERRROR;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg.what = NETWORK_ERROR;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg.what = JSON_ERROR;
				}finally{
					//结束的时间
					long endTime = System.currentTimeMillis();
					long dtime = endTime - startTime;
					if(dtime<2000){
						try {
							Thread.sleep(2000-dtime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}
			};
		}.start();
	}

	/**
     * 得到应用版本号
     */
    public String getAppVersion(){
    	PackageManager pm = getPackageManager();
    	try {
			PackageInfo info = pm.getPackageInfo("com.example.activity_splash", 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return "";
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
