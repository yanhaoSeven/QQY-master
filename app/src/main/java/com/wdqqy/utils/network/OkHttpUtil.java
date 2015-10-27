package com.wdqqy.utils.network;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
	private static final OkHttpClient mOkHttpClient = new OkHttpClient();
	public static final int ERROR_HTTP_POST = 0;
	public static final int ERROR_HTTP_EXCEPTION = 1;
	public static final int ERROR_PARAMS_NULL = 2;

	static {
		mOkHttpClient.setConnectTimeout(1000, TimeUnit.SECONDS);
		mOkHttpClient.setWriteTimeout(100, TimeUnit.SECONDS);
		mOkHttpClient.setReadTimeout(300, TimeUnit.SECONDS);
	}

	public static Response execute(Request request) throws IOException {
		return mOkHttpClient.newCall(request).execute();
	}

	/**
	 * 开启异步线程访问网络,不需要开启线程
	 * 
	 * @param request
	 * @param responseCallback
	 */
	public static void enqueue(Request request, Callback responseCallback) {
		mOkHttpClient.newCall(request).enqueue(responseCallback);
	}

	public static ResponseBody getBody(Request request) throws IOException {
		return mOkHttpClient.newCall(request).execute().body();
	}

	public static void postExecute(String url, Map<String, Object> kv, Map<String, String> head, final PostHttp callBackPostHttp) {
		RequestBody formBody = null;
		FormEncodingBuilder formencoding = new FormEncodingBuilder();
		if (null == kv || kv.size() <= 0) {
			callBackPostHttp.getFailure(ERROR_PARAMS_NULL, "");
			return;
		} else {
			for (Map.Entry<String, Object> map : kv.entrySet()) {
				formencoding.add(map.getKey(), "" + map.getValue());
			}
			formBody = formencoding.build();
		}

		Builder builder = new Request.Builder().url(url).post(formBody);
		if (null == head || head.size() <= 0) {
			Log.i("TAG", "不做处理");
		} else {
			for (Map.Entry<String, String> map : head.entrySet()) {
				builder.addHeader(map.getKey(), map.getValue());
			}
		}
		Request request = builder.build();
		OkHttpUtil.enqueue(request, new Callback() {

			@Override
			public void onResponse(Response arg0) throws IOException {
				// TODO Auto-generated method stub
				if (arg0.code() == 200) {
					callBackPostHttp.getSuccess();
				} else {
					callBackPostHttp.getFailure(ERROR_HTTP_POST, "");
				}
			}

			@Override
			public void onFailure(Request arg0, IOException e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
				callBackPostHttp.getFailure(ERROR_HTTP_EXCEPTION, e.getMessage());
			}
		});
	}

	public static void postHttp(final String url, final Map<String, Object> kv, final Map<String, String> head, final PostHttp callBackPostHttp) {
		postExecute(url, kv, head, callBackPostHttp);
	}

	public interface PostHttp {
		void getSuccess();// 成功

		void getFailure(int tag, String errorMsg);// 失败,tag可以判断是怎么失败，0为请求不是200，1为异常
	}

}
