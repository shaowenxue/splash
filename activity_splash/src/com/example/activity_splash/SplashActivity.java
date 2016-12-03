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
			case ENTER_HOME://������ҳ��
				enterHome();
				break;
			case SHOW_UPDATE_DIALOG://��ʾ�����Ի���
				Log.i(TAG,"��ʾ�����Ի���");
				showUpdateDialog();
				break;
			case URL_ERRROR://URL����
				Toast.makeText(getApplicationContext(), "URL����", 0).show();
				enterHome();
				break;
			case NETWORK_ERROR://�������Ӵ���
				Toast.makeText(getApplicationContext(), "�������Ӵ���", 0).show();
				enterHome();
				break;
			case JSON_ERROR://JSON����
				Toast.makeText(getApplicationContext(), "JSON����", 0).show();
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
        tv_splash_version.setText("�汾��"+getAppVersion());
        //�����߳�
        checkVersion();
        AlphaAnimation aa = new AlphaAnimation(0.2f,1.0f);
        aa.setDuration(500);
        findViewById(R.id.rl_splash_root).startAnimation(aa);
    }
//������ҳ��
    private void enterHome() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
			startActivity(intent);
			finish(); 
		};
//�Ի���
	protected void showUpdateDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(false);//���õ������
		builder.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				enterHome();
			}
		});
		builder.setTitle("���°汾��");
		builder.setMessage(description);
		builder.setNegativeButton("��������", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//�ж��Ƿ���SD��
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					//���ش���
					FinalHttp http = new FinalHttp();
					//����mnt/sdcard/updata.apk
					http.download(apkurl, Environment.getExternalStorageDirectory().getAbsolutePath()+"/updata.apk", 
							new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									// TODO Auto-generated method stub
									super.onFailure(t, errorNo, strMsg);
									Toast.makeText(SplashActivity.this, "����ʧ��", 1).show();
									Log.i(TAG,"����ʧ��");
								}

								@Override
								public void onLoading(long count, long current) {
									// TODO Auto-generated method stub
									super.onLoading(count, current);
									int progress = (int)(current*100/count);
									tv_splash_updateinfo.setText("���ؽ���:"+progress+"%");
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
					Toast.makeText(getApplicationContext(), "SD��������", 0).show();
				}
			}
			
		});
		builder.setPositiveButton("�´���˵", new OnClickListener(){

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
				//��ʼ��ʱ��
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
							//�汾һ�� ������ҳ��
							msg.what = ENTER_HOME;
						}else{
							//��������һ�������Ի���
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
					//������ʱ��
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
     * �õ�Ӧ�ð汾��
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
