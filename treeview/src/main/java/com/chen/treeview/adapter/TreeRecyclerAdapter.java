package com.chen.treeview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chen.treeview.R;
import com.chen.treeview.TreeRecyclerView;
import com.chen.treeview.listener.OnNodeCheckListener;
import com.chen.treeview.listener.OnNodeItemClickListener;
import com.chen.treeview.listener.OnNodeSwitchListener;
import com.chen.treeview.model.Node;
import com.chen.treeview.viewholder.TreeBaseViewHolder;
import com.chen.treeview.viewholder.TreeLeafViewHolder;
import com.chen.treeview.viewholder.TreeNodeViewHolder;

import java.util.ArrayList;
import java.util.List;


public class TreeRecyclerAdapter<T> extends RecyclerView.Adapter<TreeBaseViewHolder> {

    private Context mContext;
    private List<Node<T>> mVisibleNodes;
    private List<Node<T>> mRootNodes;
    private OnNodeItemClickListener mOnNodeItemClickListener;
    private int mSelectMode = TreeRecyclerView.MODE_SINGLE_SELECT;

    private OnNodeSwitchListener mOnNodeSwitchListener = new OnNodeSwitchListener() {

        @Override
        public void onExpand(Node node, int position) {
            expandNode(filterNodeById(node.getId(), mRootNodes));
            rearrangeVisibleNodes();
        }

        @Override
        public void onShrink(Node node, int position) {
            shrinkNode(filterNodeById(node.getId(), mRootNodes));
            rearrangeVisibleNodes();
        }
    };


    private OnNodeCheckListener mOnNodeCheckListener = new OnNodeCheckListener() {
        @Override
        public void onCheck(boolean isChecked, int position, Node node) {
            if (mOnNodeItemClickListener != null) {
                mOnNodeItemClickListener.onItemClick(node.getContent());
            }
            checkNode(filterNodeById(node.getId(), mRootNodes), isChecked);
        }
    };


    public TreeRecyclerAdapter(Context context) {
        mContext = context;
        mVisibleNodes = new ArrayList<>();
        mRootNodes = new ArrayList<>();
    }


    public void addAllData(List<Node<T>> nodes) {

        if (nodes != null && !nodes.isEmpty()) {
            mRootNodes.clear();
            mRootNodes.addAll(nodes);
        }

        for (Node<T> item : nodes) {
            filterVisibleNodes(item);
        }

        notifyDataSetChanged();
    }

    public void setMode(int mode) {
        mSelectMode = mode;
    }


    /**
     * 设置点击事件
     */
    public void setOnItemClickListener(OnNodeItemClickListener listener) {
        mOnNodeItemClickListener = listener;
    }

    /**
     * 返回选中
     *
     * @return
     */
    public List<T> getSelectedItems() {
        return null;
    }


    @Override
    public TreeBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case Node.TREE_NODE:
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.listitem_tree_node, parent, false);
                return new TreeNodeViewHolder<T>(view);
            case Node.TREE_LEAF:
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.listitem_tree_leaf, parent, false);
                return new TreeLeafViewHolder<T>(view);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(TreeBaseViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case Node.TREE_NODE:
                TreeNodeViewHolder<T> nodeViewHolder = (TreeNodeViewHolder<T>) holder;
                nodeViewHolder.bindView(mVisibleNodes.get(position),
                        position, mOnNodeSwitchListener, mOnNodeCheckListener);
                break;
            case Node.TREE_LEAF:
                TreeLeafViewHolder<T> leafViewHolder = (TreeLeafViewHolder<T>) holder;
                leafViewHolder.bindView(mVisibleNodes.get(position),
                        position, mOnNodeCheckListener);
                break;
            default:
                break;
        }
    }


    @Override
    public int getItemCount() {
        return mVisibleNodes.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mVisibleNodes.get(position).getType();
    }


    private void shrinkNode(Node<T> node) {
        if (node == null) {
            return;
        }
        node.setExpanded(false);

        if (node.getChildren() != null) {
            for (int i = 0; i < node.getChildren().size(); i++) {
                shrinkNode(node.getChildren().get(i));
            }
        }
    }


    private void expandNode(Node<T> node) {
        if (node == null) {
            return;
        }
        node.setExpanded(true);
    }


    private void checkNode(Node<T> node, boolean isChecked) {

        if (mSelectMode == TreeRecyclerView.MODE_SINGLE_SELECT) {
            //单选模式
            if (isChecked) {
                for (Node<T> item : mVisibleNodes) {
                    if (node.getId() == item.getId()) {
                        item.setChecked(true);
                    } else {
                        item.setChecked(false);
                    }
                }
                node.setChecked(isChecked);
            }
        } else if (mSelectMode == TreeRecyclerView.MODE_MULTI_SELECT) {
            //多选模式
            for (Node<T> item : mVisibleNodes) {
                if (node.getId() == item.getId()) {
                    item.setChecked(isChecked);
                }
            }
            node.setChecked(isChecked);
        } else if (mSelectMode == TreeRecyclerView.MODE_DEPEND_PARENT) {
            //TODO 有时间再搞
        }

        notifyDataSetChanged();
    }


    private Node<T> filterNodeById(final String id, final List<Node<T>> list) {

        for (Node<T> item : list) {

            if (item.getId().equals(id)) {
                return item;
            }

            if (item.getChildren() != null) {
                Node<T> result = filterNodeById(id, item.getChildren());
                if (result != null) {
                    return result;
                }
            }

        }
        return null;
    }


    private void rearrangeVisibleNodes() {

        if (mRootNodes == null || mRootNodes.size() <= 0) {
            return;
        }

        mVisibleNodes.clear();

        for (Node<T> node : mRootNodes) {
            filterVisibleNodes(node);
        }

        notifyDataSetChanged();

    }


    /**
     * 将展开节点归结到可视节点中, 递归总是从根节点开始
     *
     * @return
     */
    private void filterVisibleNodes(Node<T> node) {
        mVisibleNodes.add(node);
        if (node.isExpanded()) {
            if (node.getChildren() != null) {
                for (Node<T> item : node.getChildren()) {
                    filterVisibleNodes(item);
                }
            }
        }
    }


}
