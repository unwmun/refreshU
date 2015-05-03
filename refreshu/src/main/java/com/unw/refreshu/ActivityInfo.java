package com.unw.refreshu;

/**
 * Created by unw on 15. 4. 18..
 */
public class ActivityInfo {

    public static final String COLUMN_ACTIVITY_INFO_ACTIVITY_NAME = "class_name";
    public static final String COLUMN_ACTIVITY_INFO_PACKAGE_NAME = "package_name";

    private String activityName;
    private String packageName;

    public ActivityInfo(){}

    public ActivityInfo(String activityName, String packageName) {
        this.activityName = activityName;
        this.packageName = packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        return this.activityName.equals( ((ActivityInfo)o).activityName );
    }

    @Override
    public String toString() {
        return "FQCN : " + this.activityName;
    }
}
