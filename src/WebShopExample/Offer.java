package WebShopExample;

public class Offer {
    String[] items;
    int percentage;

    public void setOffer(String[] items, int percentage) {
        this.items = items;
        this.percentage = percentage;
    }
    
    public class MassOffer extends Offer {
        public void getPopularItems() {

        }
        
    }

    public class TargetedOffer extends MassOffer {
        public void setOffer(int percentage, int userID) {
            User user = database.getUser(userId);
            this.items = user.getFavourites();
            this.percentage = percentage;
        }
    }

    public class Test {
        public void test() {
            Offer offer = new Offer();
            MassOffer mass = new MassOffer();
            TargetedOffer target = new TargetedOffer();
            target.setOffer(15, 67); 
        }
        
    }

} 

