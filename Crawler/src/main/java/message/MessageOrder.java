package message;

import java.util.List;
import java.util.Objects;

/**
 * Created by Kris on 10-3-2015.
 */
public class MessageOrder extends Message {
    private List<String> orderList;

    /**
     * Create a Message
     *
     * @param order all the orders this crawler should do
     */
    public MessageOrder(List<String> order) {
        super(MessageType.ORDER);
        this.orderList = order;
    }

    public List<String> getOrderList() {
        return orderList;
    }
}
