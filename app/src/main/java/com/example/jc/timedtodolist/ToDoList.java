package com.example.jc.timedtodolist;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.jc.timedtodolist.MainActivity.TAG;

public class ToDoList {
    Context context;
    private static TableLayout tableLayout;
    private static List<Task> listOfTasks = new ArrayList<Task>();
    private final int MAX_LIST_SIZE = 20;
    SharedPreferences sharedPreferences, settings;
    public ToDoList(Context context){
        this.context = context;
        settings = context.getSharedPreferences("Settings",MODE_PRIVATE);
    }

    protected void addTask(Task newTask){
        if(listOfTasks.size()<MAX_LIST_SIZE) {
            tableLayout.addView(newTask.getTask());
            listOfTasks.add(newTask);
        }
        else
            Toast.makeText(context,"Can not have more than " + MAX_LIST_SIZE + " tasks", Toast.LENGTH_LONG).show();
    }

    protected static List<Task> getListOfTasks() {
        return listOfTasks;
    }

    protected void confirmList(){
        Iterator iterator = listOfTasks.iterator();
        Task currentTask;
        while(iterator.hasNext()){
            currentTask = (Task) iterator.next();
            currentTask.getTaskCheckBox().setEnabled(true);
            if(!settings.getBoolean("editTask",false))
                currentTask.getTaskDesc().setKeyListener(null);
            currentTask.getTaskDesc().setFocusable(settings.getBoolean("editTask",true));
           if((currentTask.getTaskDesc().getText().toString().equals(""))){
               removeEmptyTasks(currentTask, iterator);

            }
        }
        redoNumbering();
    }
    protected static void redoNumbering(){
        Iterator iterator = listOfTasks.iterator();
        int num = 1;
        String tmp;
        Task currentTask;
        while(iterator.hasNext()){
            currentTask = (Task) iterator.next();
            tmp = num + " )";
            currentTask.getTaskNum().setText(tmp);
            num++;
        }
    }
    public void setTableLayout(TableLayout tableLayout){
        this.tableLayout = tableLayout;
    }
    public static TableLayout getTableLayout() {
        return tableLayout;
    }

    private void removeEmptyTasks(Task currentTask, Iterator iterator){
        iterator.remove();
        tableLayout.removeView(currentTask.getTask());
        Task.setTotalNumberOfTask(Task.getTotalNumberOfTask()-1);
    }

    public int adjustTaskCounter(Task currentTask, int ct){
        String s = ct+ " )";
        currentTask.getTaskNum().setText(s);
        Task.setTotalNumberOfTask(ct++);
        return ct;
    }
    protected void resetList(){
        tableLayout.removeAllViews();
        Task.setTotalNumberOfTask(0);
        listOfTasks.clear();
    }
    protected boolean isToDoListEmpty(){
        Log.d(TAG, "isToDoListEmpty: size of list" + listOfTasks.size());
        return listOfTasks.isEmpty();
    }
}
