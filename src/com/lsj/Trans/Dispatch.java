package com.lsj.Trans;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.lsj.Trans.Params.HttpParams;

public abstract class Dispatch {
	protected static Map<String, String> ClassMap = new HashMap<>();			//类名映射，由子类完成
	protected Map<String, String> langMap = new HashMap<>();					//语言映射，由子类完成
	protected String base;														//分发器地址
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private static Map<String, Dispatch> Instances = new HashMap<>();			//实例映射

	abstract public String Trans(String from, String targ, String query) throws Exception;
	
	static public Dispatch Instance(String name) throws Exception{
		if(ClassMap.size() == 0){	//加载类
			Class.forName("com.lsj.Trans.BaiduDispatch");
			Class.forName("com.lsj.Trans.GoogleDispatch");
			Class.forName("com.lsj.Trans.JinshanDispatch");
			Class.forName("com.lsj.Trans.YoudaoDispatch");
		}
		//取出类
		String ClassName = ClassMap.get(name);
		if(ClassName == null){	//不存在对应的类, 无法实例化.
			return null;
		}
		
		if( Instances.get(ClassName) == null){
			Dispatch dispatch = (Dispatch)Class.forName(ClassName).newInstance();
			Instances.put(ClassName, dispatch);
		}
		return Instances.get(ClassName);
	}
	
	protected String execute(HttpParams params) throws Exception{
		
		HttpUriRequest request = params.RequestCreateByUrl(base);		//根据不同的参数情况，创建不同的request(get或post)
		CloseableHttpResponse response = httpClient.execute(request);
		
		return InputStream2JsonString(response.getEntity().getContent());	//将输入流全部读取出来转换为标准json字符串，在该函数会关闭输入流
	}
	
	private String InputStream2JsonString(InputStream is) throws Exception{
        Scanner scanner = new Scanner(is);
        String jsonString = scanner.useDelimiter("\\A").next();
        
        String buf = jsonString.replaceAll(",,", ",\"NULL\",");
		while(!jsonString.equals(buf)){
			jsonString = buf;
			buf = jsonString.replaceAll(",,", ",\"NULL\",");
		}
		
		scanner.close();
        is.close();
		return jsonString;
	}
}
