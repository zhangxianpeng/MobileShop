package com.idaniu.maga.shopping.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idaniu.maga.shopping.Constant;
import com.idaniu.maga.shopping.R;
import com.idaniu.maga.shopping.ShoppingApplication;
import com.idaniu.maga.shopping.activity.BannerActivity;
import com.idaniu.maga.shopping.adapter.HomeRecyclerAdapter;
import com.idaniu.maga.shopping.bean.BannerBean;
import com.idaniu.maga.shopping.bean.HomeBean;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 第一个fragment，展示主页
 * Created by yuanbao15 on 2017/10/8.
 */
public class Fragment1 extends Fragment{
    private static final String TAG = "HomeFragment";

    private ViewPager mViewPager;       //图片轮播里的ViewPager
    private BannerPagerAdapter mBannerPagerAdapter;
    private LinearLayout mIndicateLinearLayout;     //圆点指示器
    private TextView mBannerTitleTextView;      //图片轮播中每张图片的标题

    private List<BannerBean> mBannerList;
    private List<ImageView> mIndicateList = new ArrayList<>();   //圆点集合
    private List<ImageView> mBannerViewsList = new ArrayList<>();   //图片展示的集合

    private List<HomeBean> homeBeanList = new ArrayList<>();    //主页瀑布流的部分
    private HomeRecyclerAdapter mHomeRecyclerAdapter;
    private RecyclerView recyclerView;

/*    public static Fragment1 newInstance(String tabName){
        Fragment1 fragment = new Fragment1();
//        fragment.mTabName = tabName;
        return fragment;
    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag1, container, false);
        initView(v);
//        getLayoutInflater(savedInstanceState).inflate(R.layout.home_item,container);
// 没有这句报错，因为否则就不知道把哪一个布局放到瀑布流中。也不对，viewHolder里有对R.layout.home_item的引用
        return v;
    }

    private void initView(View v) {
        mViewPager = (ViewPager) v.findViewById(R.id.viewpager_hotnews);
        mBannerTitleTextView =(TextView) v.findViewById(R.id.tv_title_hotnews);
        mIndicateLinearLayout =(LinearLayout) v.findViewById(R.id.ll_hotnews_indicator);


        mBannerPagerAdapter = new BannerPagerAdapter();
        mViewPager.setAdapter(mBannerPagerAdapter);     //给mViewPager绑定这个适配器

        //图片轮播的viewpager在页面改变时候
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                for (int j = 0; j < mIndicateLinearLayout.getChildCount(); j++) { //选到哪页就跳转到哪一页
                    View view = mIndicateLinearLayout.getChildAt(j);
                    if (j == i) {
                        view.setSelected(true);
                        mIndicateList.get(j).setBackgroundResource(R.drawable.indicate_circle_red); //实现圆点的状态变化
                    } else {
                        view.setSelected(false);
                        mIndicateList.get(j).setBackgroundResource(R.drawable.indicate_circle_gray);
                    }
                }
                //标题内容也要随之相应的更改
                mBannerTitleTextView.setText(mBannerList.get(i).getName());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        loadBannerData();       //下载 图片轮播的图片资源


        loadHomeData();     //下载 主页瀑布流的数据

        //主页瀑布流部分初始化
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_home);
        //瀑布流效果
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);   //分成几列
        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));      //普通线性展示
//        mHomeRecyclerAdapter = new HomeRecyclerAdapter(getActivity(),homeBeanList);   //这是没有下拉刷新的，下面的有
        mHomeRecyclerAdapter = new HomeRecyclerAdapter(getContext(),homeBeanList, new HomeRecyclerAdapter.OnMoreListener() {
            @Override
            public void onMore() {      //没实现
                loadHomeData();
                Toast.makeText(ShoppingApplication.getInstance(), "you refresh this page!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(mHomeRecyclerAdapter);
//        Log.d(TAG, "frag1执行到这2");

    }

    //下载主页homebean数据，使用okhttp方式。
    private void loadHomeData() {
        OkHttpClient client = new OkHttpClient();       //创建网络实例
        //创建请求
        Request request = new Request.Builder()
                .url(Constant.HOME_URL + "?type=1")
                .build();
        //返回数据
        client.newCall(request).enqueue(new Callback() {     //书上使用的execute()方法，同步方式。老师的是enqueue(new Callback())这个是没有返回值的，异步方式。
            @Override
            public void onFailure(Call call, IOException e) {
                if(getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShoppingApplication.getInstance(), "load data failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                System.out.println(responseData);
                Gson gson = new Gson();     //用Gson解析Json数据
                Type type = new TypeToken<List<HomeBean>>(){}.getType();  //这句不知道是干嘛，获取类型？!!!!这里要改HomeBean，调试了好久才查出错误产生的并不是HomeBean对象

                //homeBeanList = gson.fromJson(responseData, type);发现这儿的homeBeanList有数据，但外部没有，所以不能这样写
                homeBeanList.clear();
                List<HomeBean> list = gson.fromJson(responseData, type);
                homeBeanList.addAll(list);
                /*//debug
                for (HomeBean homeBean:homeBeanList){
                    Log.d(TAG,homeBean.toString());
                }*/
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHomeRecyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    //下载图片资源，使用okhttp方式。
    private void loadBannerData() {
        OkHttpClient client = new OkHttpClient();       //创建网络实例
        //创建请求
        Request request = new Request.Builder()
                .url(Constant.HEAD_URL + "?type=1")         //接口文档中type为1的数据（广告图片）
                .build();
        //返回数据
        client.newCall(request).enqueue(new Callback() {     //书上使用的execute()方法，同步方式。老师的是enqueue(new Callback())这个是没有返回值的，异步方式。
            @Override
            public void onFailure(Call call, IOException e) {
                if(getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShoppingApplication.getInstance(), "load data failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
//                System.out.println(responseData);
                Gson gson = new Gson();     //用Gson解析Json数据
                Type type = new TypeToken<List<BannerBean>>(){}.getType();  //这句不知道是干嘛，获取类型？
                mBannerList = gson.fromJson(responseData, type);        //gson获取到的数据成为一个个bannerbean对象然后添加到mbannerlist列表中

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mBannerList != null && mBannerList.size()>=0){
                            mIndicateLinearLayout.removeAllViews(); //先清除再重新加载
                            mBannerViewsList.clear();
                            mIndicateLinearLayout.removeAllViews();

                            for (int j=0; j<mBannerList.size(); j++){
                                //图片处理
                                ImageView imageView = new ImageView(getActivity());
                                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT));  //宽高
                                imageView.setBackgroundResource(0);     //这句不懂？ 是撤掉背景？？
                                mBannerViewsList.add(imageView);

                                //圆点指示器处理
                                ImageView indicateView = new ImageView(getActivity());
                                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(18,18);   //20*20的像素大小
                                indicateView.setLayoutParams(lp);
                                indicateView.setBackgroundResource(0);
//                              indicateView.setImageResource(R.drawable.indicate_circle_gray);   //不通过这里展示，否则与onPageSelected()方法里的有重叠，效果不好
//                              indicateView.setPadding(4, 4, 4, 4);    //每个圆点图片的前后左右距离
                                mIndicateLinearLayout.addView(indicateView);
                                mIndicateList.add(indicateView);

                            }
                            mBannerPagerAdapter.notifyDataSetChanged();
                            mIndicateLinearLayout.postDelayed(bannerRunnable, 1000);

                        }
                    }
                });
            }
        });


    }

    //图片自动轮播。但圆点怎么更换图片呢？不在这儿，在那个onchangelistener()监听方法里
    private Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            //圆点指示器随着图片变化而变化
            int index = mViewPager.getCurrentItem();
            if(index == mBannerList.size()-1){  //循环重复
                index = -1;
            }else{
                index = index+1;
            }

            mViewPager.setCurrentItem(index,true);
            mIndicateLinearLayout.postDelayed(bannerRunnable,3000);     //每过3秒进行切换图片
        }
    };

    //自定义一个BannerPagerAdapter的适配器，给头图配置用
    private class BannerPagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            if(mBannerViewsList == null){
                return 0;
            }
            return mBannerViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        //后面两个方法是非必须重写的，然后借鉴老师的。需要！！这里对应着mBannerPagerAdapter.notifyDataSetChanged();
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mBannerViewsList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if(getActivity() == null || getActivity().isFinishing()) return null;
            ImageView view = mBannerViewsList.get(position);
            if(view.getParent() != null){
                ((ViewGroup)view.getParent()).removeView(view);
            }
            view.setImageResource(R.mipmap.ic_launcher);
            Picasso.with(getActivity()).load(mBannerList.get(position).getImgUrl()).into(view);
            container.addView(view);

            //yb为bannerView设置点击监听响应。是在这而不是在mViewPager
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"yb点击了首页"+mViewPager.getCurrentItem(),Toast.LENGTH_SHORT).show();
                    if(mViewPager.getCurrentItem() == 0){
                        Intent intent = new Intent();
                        intent.setClass(getContext(), BannerActivity.class);
                        startActivity(intent);
                        /*
                        //打开手机手机浏览器访问一个网址
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri data = Uri.parse("https://yuanbao15.github.io");
                        intent.setData(data);
                        startActivity(intent);*/
                    }
                }
            });

            return view;
        }
    }


}
