package model;

import java.util.List;

public class DashboardStats {

    private int totalStudents;
    private int maleCount;
    private int femaleCount;
    private int totalDepartments;
    private List<Student> recentStudents;

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getMaleCount() {
        return maleCount;
    }

    public void setMaleCount(int maleCount) {
        this.maleCount = maleCount;
    }

    public int getFemaleCount() {
        return femaleCount;
    }

    public void setFemaleCount(int femaleCount) {
        this.femaleCount = femaleCount;
    }

    public int getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(int totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public List<Student> getRecentStudents() {
        return recentStudents;
    }

    public void setRecentStudents(List<Student> recentStudents) {
        this.recentStudents = recentStudents;
    }
}
