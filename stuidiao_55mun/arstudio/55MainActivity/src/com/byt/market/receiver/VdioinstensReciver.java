package com.byt.market.receiver;

import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.tools.LogCart;
import com.byt.market.util.Singinstents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class VdioinstensReciver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		LogCart.Log("啵啵啵----");
		if (arg1.getAction().equals("android.intent.action.PACKAGE_ADDED")) {   
            String packageName = arg1.getDataString();  
            if(packageName.equals("com.byt.market.palyer")){
            	 try{  
            		 LogCart.Log("啵啵啵----");
     		    	Singinstents.getInstents().getAppPackageName();
     		    	DownLoadVdioapkTools td = new DownLoadVdioapkTools(arg0);
//     		    	if(td.isRunningApp(arg0, "com.byt.market.palyer")){
//     		    		return;
//     		    	}
//     		        Intent intent = arg0.getPackageManager().
//     		        		getLaunchIntentForPackage(Singinstents.getInstents().getAppPackageName());  
//     		        intent.putExtra("url", Singinstents.getInstents().getVdiouri());
//     		        arg0.startActivity(intent);  
     		    	td.startAPP(Singinstents.getInstents().getVdiouri());
     		    }catch(Exception e){  
     		        Toast.makeText(arg0, "没有安装", Toast.LENGTH_LONG).show();  
     		        
     		    }  
            }
        }   
	}

}
