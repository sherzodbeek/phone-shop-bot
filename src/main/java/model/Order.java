package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.OrderStatus;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Integer id;
    private Date createdAt;
    private OrderStatus status;
    private double totalSum;
    private User user;
    private String address;
    private double lon;
    private double lat;
}
