package com.byt.market.adapter;

import com.byt.market.ui.GuideFragmentFive;
import com.byt.market.ui.GuideFragmentfour;
import com.byt.market.ui.GuideFragmentone;
import com.byt.market.ui.GuideFragmentthree;
import com.byt.market.ui.GuideFragmenttwo;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
/**
 * Fragment������
 * FragmentPagerAdapter : �����ǵ�viewpager��fragmentǶ��ʹ��ʱʵ�õ�
 * </BR> </BR> By����ɬ </BR> ��ϵ���ߣ�QQ 534429149
 * */
public class GuideViewPagerAdapter extends FragmentPagerAdapter{

	private Context ctx;

	//FragmentManager fragment������ ,������
	public GuideViewPagerAdapter(FragmentManager fm,Context ctx) {
		super(fm);
		this.ctx = ctx;
	}
	//����һ��fragment
	//arg0 �������ڼ�ҳ
	@Override
	public Fragment getItem(int arg0) {
		Fragment mFragment = null;
		if(arg0 == 0){
			mFragment = new GuideFragmentone(ctx);
		}else if(arg0 == 1){
			mFragment = new GuideFragmenttwo(ctx);
		}else if(arg0 == 2){
			mFragment = new GuideFragmentfour(ctx);
		}else if(arg0 == 3){
			mFragment = new GuideFragmentthree(ctx);
		}else if(arg0 == 4){
			mFragment = new GuideFragmentFive(ctx);
		}
		return mFragment;
	}

	//������������
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 5;
	}

}
