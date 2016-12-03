package com.chen.treeview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chen.treeview.listener.OnNodeItemClickListener;
import com.chen.treeview.model.NodeDataConverter;
import com.chen.treeview.adapter.TreeRecyclerAdapter;
import com.chen.treeview.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhaohua on 16/11/25.
 */
public class TreeRecyclerView extends FrameLayout {

    public final static int MODE_SINGLE_SELECT = 1;        //单选模式
    public final static int MODE_MULTI_SELECT = 2;         //多选模式
    public final static int MODE_DEPEND_PARENT = 3;        //多选，父节点选中，子节点全取消，选中子节点，则父节点选中取消
    public final static int MODE_CLICK_SELECT = 4;         //没有选中模式，直接点击跳转

    private RecyclerView mRecyclerView;
    private TreeRecyclerAdapter mAdapter;


    public TreeRecyclerView(Context context) {
        super(context);
        initView(context);
    }

    public TreeRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TreeRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void initView(Context context) {
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mRecyclerView, lp);
    }


    /**
     * 初始化树形折叠控件数据
     *
     *
     * T 类型 示例
     *
     * public static class TestModel {
     *          @NodeId
     *          public String id;                    //必填字段
     *          @NodeName
     *          public String name;                  //必填字段
     *          @NodeLabel
     *          public String label;
     *          @NodePid
     *          public String parentId;              //父节点id
     *          @NodeChild
     *          public List<TestModel> child;        //child用来表示层级关系, child为空，则表示叶子节点
     *          ...
     *          others
     *          ...
     * }
     *
     *
     */
    public <T> void setData(List<T> list, int mode) {

        ArrayList<Node<T>> nodeList = new ArrayList<>();

        try {
            nodeList = NodeDataConverter.convertToNodeList(list);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mAdapter = new TreeRecyclerAdapter<>(mRecyclerView.getContext());
        setMode(mode);
        mAdapter.addAllData(nodeList);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 设置点击事件, MODE_CLICK_SELECT模式下需要使用
     */
    public void setOnItemClickListener(OnNodeItemClickListener listener) {
        mAdapter.setOnItemClickListener(listener);
    }


    /**
     * 获取选中的内容
     *
     * @param <T>
     * @return
     */
    public <T> List<T> getSelectedItems() {
        return mAdapter.getSelectedItems();
    }


    /**
     * 设置折叠控件的选择模式
     *
     * @param mode
     */
    private void setMode(int mode) {
        if (mode != MODE_SINGLE_SELECT
                && mode != MODE_MULTI_SELECT
                && mode != MODE_DEPEND_PARENT
                && mode != MODE_CLICK_SELECT) {
            return;
        }
        mAdapter.setMode(mode);
    }


}
