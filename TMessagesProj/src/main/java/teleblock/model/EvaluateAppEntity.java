package teleblock.model;

public class EvaluateAppEntity {

    private long firstOpenAppDate;//第一次打开app日期
    private boolean evaluatedApp;//是否评价过app
    private long currentDate;//当天日期
    private boolean dayFirstTime;//当天是否第一次打开弹窗

    public long getFirstOpenAppDate() {
        return firstOpenAppDate;
    }

    public void setFirstOpenAppDate(long firstOpenAppDate) {
        this.firstOpenAppDate = firstOpenAppDate;
    }

    public boolean isEvaluatedApp() {
        return evaluatedApp;
    }

    public void setEvaluatedApp(boolean evaluatedApp) {
        this.evaluatedApp = evaluatedApp;
    }

    public long getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(long currentDate) {
        this.currentDate = currentDate;
    }

    public boolean isDayFirstTime() {
        return dayFirstTime;
    }

    public void setDayFirstTime(boolean dayFirstTime) {
        this.dayFirstTime = dayFirstTime;
    }
}