package com.example.jc.timedtodolist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class Task {
    private TableRow task;
    private TextView taskNum;
    private EditText taskDesc;
    private CheckBox taskCheckBox;


    private Context context;
    private TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,5.0f);
    private TableRow.LayoutParams editTextParams = new TableRow.LayoutParams(0,TableLayout.LayoutParams.WRAP_CONTENT,4.0f);
    private TableRow.LayoutParams checkBoxParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);
    private TableRow.LayoutParams textViewParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);


    private SharedPreferences settings;

    private static int totalNumberOfTask = 0;
    public Task(){
        task = null;
        taskNum = null;
        taskDesc = null;
        taskCheckBox = null;
    }
    protected Task(Context context){
        this.context = context;
        settings = context.getSharedPreferences("Settings2",MODE_PRIVATE);
        task = new TableRow(context);
        taskNum = new TextView(context);
        taskDesc = new EditText(context);
        taskCheckBox = new CheckBox(context);
    }

    protected void addNewTask(){
        //if(settings.getBoolean())
        totalNumberOfTask++;
        assignTaskNumber(totalNumberOfTask);
        assignTaskDescription("");
        assignTaskCheckBox(false);

        task.setPadding(4,8,4,32);

        task.addView(taskNum);
        task.addView(taskDesc);
        task.addView(taskCheckBox);

        task.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                removeIndividualTask();
                return false;
            }
        });

    }
    public void assignDefaultTaskValues(){
        totalNumberOfTask++;
        String s = totalNumberOfTask + " )";
        taskNum.setText(s);
        taskDesc.setHint("Leave blank if not needed!");
        taskCheckBox.setEnabled(false);

    }

    private void removeIndividualTask(){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setMessage("Are you sure you want to remove the task? (May mess up numbering)")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Yes, I'm Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //TODO: Change ToDoList so it extends table layout
                       ViewGroup parentView = ToDoList.getTableLayout();
                       parentView.removeView(task);
                       ToDoList.getListOfTasks().remove(Task.this);
                       ToDoList.redoNumbering();
                       totalNumberOfTask--;
                    }
                })
                .create();
        alert.show();
    }
    protected void assignTaskNumber(int currentTaskNumber){
        String s = currentTaskNumber + " )";
        taskNum.setText(s);
        taskNum.setLayoutParams(textViewParams);
        taskNum.setTextSize(18);
        taskNum.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }
    protected void assignTaskDescription(String description){
        taskDesc.setText(description);
        taskDesc.setHint("Leave blank if not needed!");
        taskDesc.requestFocus();
        taskDesc.setTextSize(18);
        taskDesc.setLayoutParams(editTextParams);
    }
    protected void assignTaskCheckBox(boolean isChecked){
        taskCheckBox.setChecked(isChecked);
        taskCheckBox.setEnabled(false);
        taskCheckBox.setLayoutParams(checkBoxParams);
    }
    public void setTaskVisibility(){
        task.setVisibility(View.GONE);
        totalNumberOfTask--;
    }



    public TextView getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(TextView taskNum) {
        this.taskNum = taskNum;
    }

    public EditText getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(EditText taskDesc) {
        this.taskDesc = taskDesc;
    }

    public CheckBox getTaskCheckBox() {
        return taskCheckBox;
    }

    public void setTaskCheckBox(CheckBox taskCheckBox) {
        this.taskCheckBox = taskCheckBox;
    }
    public TableRow getTask() {
        return task;
    }

    public void setTask(TableRow task) {
        this.task = task;
    }

    public static int getTotalNumberOfTask() {
        return totalNumberOfTask;
    }

    public static void setTotalNumberOfTask(int totalNumberOfTask) {
        Task.totalNumberOfTask = totalNumberOfTask;
    }
}
