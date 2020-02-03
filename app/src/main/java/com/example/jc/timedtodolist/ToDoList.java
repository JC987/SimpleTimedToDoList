package com.example.jc.timedtodolist;

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

class ToDoList {
    Context context;
    private static TableLayout tableLayout;
    private static List<Task> listOfTasks = new ArrayList<Task>();
    private int maxListSize;
    private ArrayList<String> completed = new ArrayList<>();

    private ArrayList<String> failed =  new ArrayList<>();
    private SharedPreferences settings;
    ToDoList(Context context){
        this.context = context;
        //TODO: Add a setting to change max list size
        settings = context.getSharedPreferences("Settings",MODE_PRIVATE);
        maxListSize = Integer.parseInt(settings.getString("maxNumberOfTask","20"));
    }

    void addTask(Task newTask){
        if(listOfTasks.size()<maxListSize) {
            tableLayout.addView(newTask.getTask());
            listOfTasks.add(newTask);
        }
        else
            Toast.makeText(context,"Can not have more than " + maxListSize + " tasks", Toast.LENGTH_LONG).show();
    }

    static List<Task> getListOfTasks() {
        return listOfTasks;
    }

    void confirmList(){
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
    static void redoNumbering(){
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


    void resetList(){
        saveList();
        tableLayout.removeAllViews();
        Task.setTotalNumberOfTask(0);
        listOfTasks.clear();
    }

    public ArrayList<String> getCompleted() {
        return completed;
    }


    public ArrayList<String> getFailed() {
        return failed;
    }

    private void saveList(){
        Iterator iterator = listOfTasks.iterator();
        Task currentTask;
        while(iterator.hasNext()){
            currentTask = (Task) iterator.next();
            if(currentTask.getTaskCheckBox().isChecked()) {
                //add to completed
                completed.add(currentTask.getTaskDesc().getText().toString());
            }
            else {
                //add to failed
                failed.add(currentTask.getTaskDesc().getText().toString());
            }

        }
    }
    boolean isToDoListEmpty(){
        Log.d(TAG, "isToDoListEmpty: size of list" + listOfTasks.size());
        return listOfTasks.isEmpty();
    }
}
