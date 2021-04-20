public class Page {
    private String ID;
    private int value;

    public Page(String ID, int value) {
        this.ID = ID;
        this.value = value;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setValue(int value) {
        this.value = value;
    }


    public String getID() {
        return ID;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return ID + " " + value;
    }
}
