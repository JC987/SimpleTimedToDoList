package com.example.jc.timedtodolist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class Task {
    private TableRow task;
    private TextView taskNum;
    private EditText taskDesc;
    private CheckBox taskCheckBox;


    private Context context;
    private TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,5.0f);
    private TableRow.LayoutParams editTextParams = new TableRow.LayoutParams(0,TableLayout.LayoutParams.WRAP_CONTENT,4.0f);
    private TableRow.LayoutParams checkBoxParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);
    private TableRow.LayoutParams textViewParams= new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.5f);


    private static int totalNumberOfTask = 0;
     Task(){
        task = null;
        taskNum = null;
        taskDesc = null;
        taskCheckBox = null;
    }
     Task(Context context){
        this.context = context;
        task = new TableRow(context);
        taskNum = new TextView(context);
        taskDesc = new EditText(context);
        taskCheckBox = new CheckBox(context);
    }


    void addNewTask(){
        totalNumberOfTask++;
        assignTaskNumber(totalNumberOfTask);
        assignTaskDescription("");
        assignTaskCheckBox(false);

        task.setPadding(4,8,4,32);
        taskNum.setTextColor(Color.BLACK);

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
     void assignDefaultTaskValues(){
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
    private void assignTaskNumber(int currentTaskNumber){
        String s = currentTaskNumber + " )";
        taskNum.setText(s);
        taskNum.setLayoutParams(textViewParams);
        taskNum.setTextSize(18);
        taskNum.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }
    private void assignTaskDescription(String description){
        taskDesc.setText(description);
        taskDesc.setHint("Leave blank if not needed!");
        taskDesc.requestFocus();
        taskDesc.setTextSize(18);
        taskDesc.setLayoutParams(editTextParams);
    }
    private void assignTaskCheckBox(boolean isChecked){
        taskCheckBox.setChecked(isChecked);
        taskCheckBox.setEnabled(false);
        taskCheckBox.setLayoutParams(checkBoxParams);
    }



    TextView getTaskNum() {
        return taskNum;
    }

    void setTaskNum(TextView taskNum) {
        this.taskNum = taskNum;
    }

    EditText getTaskDesc() {
        return taskDesc;
    }

    void setTaskDesc(EditText taskDesc) {
        this.taskDesc = taskDesc;
    }

    CheckBox getTaskCheckBox() {
        return taskCheckBox;
    }

    void setTaskCheckBox(CheckBox taskCheckBox) {
        this.taskCheckBox = taskCheckBox;
    }
    TableRow getTask() {
        return task;
    }

    void setTask(TableRow task) {
        this.task = task;
    }

    static int getTotalNumberOfTask() {
        return totalNumberOfTask;
    }

    static void setTotalNumberOfTask(int totalNumberOfTask) {
        Task.totalNumberOfTask = totalNumberOfTask;
    }
}
