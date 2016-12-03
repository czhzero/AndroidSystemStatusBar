package com.chen.treeview.model;

import android.text.TextUtils;

import com.chen.treeview.model.Node;
import com.chen.treeview.model.NodeChild;
import com.chen.treeview.model.NodeId;
import com.chen.treeview.model.NodeLabel;
import com.chen.treeview.model.NodeName;
import com.chen.treeview.model.NodePid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhaohua on 16/12/3.
 */
public class NodeDataConverter {

    /**
     *
     * 列表转换
     *
     *
     * @param list
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> ArrayList<Node<T>> convertToNodeList(List<T> list) throws IllegalAccessException, IllegalArgumentException {


        ArrayList<Node<T>> nodeList = new ArrayList<>();

        if (list == null || list.size() <= 0) {
            return nodeList;
        }

        for (T t : list) {

            Node<T> node = data2Node(t);

            if (node != null) {
                nodeList.add(node);
            }

        }

        //设置层级和parent
        setLevelAndParent(nodeList);

        return nodeList;
    }


    /**
     * 根据pid 和 childlist 设置 parent 和 level
     * @param nodeList
     * @param <T>
     */
    public static <T> void setLevelAndParent(List<Node<T>> nodeList) {

        if (nodeList == null) {
            return;
        }

        for (Node<T> node : nodeList) {

            //根节点
            if (TextUtils.isEmpty(node.getpId())) {
                node.setParent(null);
                node.setLevel(0);
            }

            //递归设置
            if (node.getChildren() != null) {
                for (Node<T> child : node.getChildren()) {
                    child.setParent(node);
                    child.setLevel(node.getLevel() + 1);
                }
                setLevelAndParent(node.getChildren());
            }

        }
    }


    /**
     *
     * T 类型 示例
     *
     *    public static class TestModel {
                @NodeId
                public String id;       //必填字段
                @NodeName
                public String name;     //必填字段
                @NodeLabel
                public String label;
                @NodePid
                public String parentId;
                @NodeChild
                public List<TestModel> child;
           }
     *
     *
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private static <T> Node<T> data2Node(T t) throws IllegalAccessException, IllegalArgumentException {

        if (t == null) {
            return null;
        }

        String id = "";
        String pid = "";
        String name = "";
        String label = "";
        List<T> childs = null;

        Class clazz = t.getClass();
        //反射获取类中的字段
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {

            //id
            if (field.getAnnotation(NodeId.class) != null) {
                field.setAccessible(true);
                id = (String) field.get(t);
            }

            //pid
            if (field.getAnnotation(NodePid.class) != null) {
                field.setAccessible(true);
                pid = (String) field.get(t);
            }


            //name
            if (field.getAnnotation(NodeName.class) != null) {
                field.setAccessible(true);
                name = (String) field.get(t);
            }

            //lable
            if (field.getAnnotation(NodeLabel.class) != null) {
                field.setAccessible(true);
                label = (String) field.get(t);
            }

            //child
            if (field.getAnnotation(NodeChild.class) != null) {
                field.setAccessible(true);
                childs = (List<T>) field.get(t);
            }

        }

        //非法节点
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name)) {
            return null;
        }

        Node<T> node = new Node(id, pid, name);

        if (childs != null && childs.size() > 0) {
            ArrayList<Node<T>> nodeChilds = new ArrayList<>();
            for (T item : childs) {
                Node<T> itemNode = data2Node(item);
                if (itemNode != null) {
                    nodeChilds.add(itemNode);
                }
            }
            node.setChildren(nodeChilds);
            node.setType(Node.TREE_NODE);
        } else {
            node.setChildren(null);
            node.setType(Node.TREE_LEAF);
        }

        node.setLabel(label);
        node.setContent(t);

        return node;
    }








}
