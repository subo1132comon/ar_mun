package com.byt.market.util;

import com.byt.market.Refreshable;

import java.util.ArrayList;
import java.util.List;

public class RefreshHelper {
    private List<Refreshable> refreshList = new ArrayList<Refreshable>();
    
    public static RefreshHelper helper;
    
    public static RefreshHelper getInstance(){
        if(helper == null){
            helper = new RefreshHelper();
        }
        return helper;
    }
    
    public void registRefreshable(Refreshable ref){
        if(!refreshList.contains(ref)){
            refreshList.add(ref);
        }
    }
    
    public void unregistRefreshable(Refreshable ref){
        if(refreshList.contains(ref)){
            refreshList.remove(ref);
        }
    }
    
    public void refresh(){
        for (Refreshable ref : refreshList) {
            ref.refresh();
        }
    }
}
