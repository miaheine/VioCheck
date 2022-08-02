package WebShopExample;
public class User {
    private static int creditCard;
    private static String name;
    private static String email;
    private static String adress;
    private static int zip;
    public boolean marketingAllowed;

    public User(String name, String email, boolean marketingAllowed) {
        this.name = name;
        this.email = email;
        this.marketingAllowed = marketingAllowed;
    }

    public static String {purchase} getAdress() {
        return adress;
    }

    public static int {purchase} getZip() {
        return zip;
    }

    public static int {purchase} getCreditCard() {
        return creditCard;
    }

    public static String {email} getEmail() {
        return email;
    }

    public void unsubscribe() {
        this.marketingAllowed = false;
    }

    public void {marketing} subscribe() {
        this.marketingAllowed = true;
    }
}
