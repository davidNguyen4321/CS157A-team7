package servlet.engagement;

public class ToggleResponse {
    private boolean status;
    private int count;

    public ToggleResponse(boolean status, int count) {
        this.status = status;
        this.count = count;
    }

    public boolean isStatus() { return status; }
    public int getCount() { return count; }
}