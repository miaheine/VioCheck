package WebShopExample;
import java.util.ArrayList;

public class Order {
    private static int number;
    private static int total;
    private static User user;
    private ArrayList<String> products;

    public Order(int number, User user) {
        Order.number = number;
        Order.user = user;
        products = new ArrayList<>();
    }

    public void addProduct(String product, int price) {
        products.add(product);
        total += price;
    }

    public static User {purchase} getUser() {
        return user;
    }

    public static int {purchase} getTotal() {
        return total;
    }

    public static int {purchase} getNumber() {
        return number;
    }
}
