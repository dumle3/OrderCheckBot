package dimats;

public class Order {
    int id;
    String name;
    String gild;
    int amount;
    int table;
    String whenGot;

    Boolean isPosted;
    Boolean isServed;

    public Order(int id, String name, String gild, int amount, int table, String whenGot, boolean isPosted, boolean isServed) {
        this.id = id;
        this.name = name;
        this.gild = gild;
        this.amount = amount;
        this.table = table;
        this.whenGot = whenGot;
        this.isPosted = isPosted;
        this.isServed = isServed;
    }

    public void setServed(Boolean served) {
        isServed = served;
    }
}
