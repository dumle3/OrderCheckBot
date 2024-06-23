package dimats;

public class Order {
    int id;
    String name;
    String gild;
    int amount;
    int table;
    Long millis;

    Boolean isPosted;
    Boolean isServed;

    public Order(int id, String name, String gild, int amount, int table, Long millis, boolean isPosted, boolean isServed) {
        this.id = id;
        this.name = name;
        this.gild = gild;
        this.amount = amount;
        this.table = table;
        this.millis = millis;
        this.isPosted = isPosted;
        this.isServed = isServed;
    }
}
