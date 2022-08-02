package WebShopExample;
public class Shop {
    public void {purchase} sendOrderConfirmation(int orderNumber, String email) {
        sendEmail(email, "Your order with number " + orderNumber + " for a total of: " + Order.getTotal() +
        "has been processed");
        sendOffer();
    }
    
    public void {marketing} sendDirectOffer() {
        if (User.getZip() >= 2000 && User.getZip() <= 3000) {
            sendEmail(User.getEmail(), "Special offer just for you"); 
        } else {
           sendEmail(User.getEmail(), "Really great offer that you wont miss!"); 
        }   
    }

    public String sendEmail(String email, String text) {
        return "Send an email to the user containing " + text;
    }

    public void {purchase} processOrder(int orderNumber, String email) {
        sendOrderConfirmation(orderNumber, email);
        getPayment();
        shipping();
        sendOffer();
    }

    public void {marketing} sendDirectOffer(String email) {
        sendEmail(email, "Offer only for you");
    }

    public void {purchase} processOrder (Order order, User user) {
        getPayment(order, user);
        shipping(order, user);
        sendDirectOffer(user.getEmail());
    }

    public void {shipping} shipping() {
        System.out.println("Send order " + Order.getNumber() + " to adress:" + User.getAdress());
    }

    public String {payment} getPayment() {
        int order = Order.getNumber();
        int credit = User.getCreditCard();
        return "Order " + order + " has been payed with card: " + credit;        
    }

    
}
