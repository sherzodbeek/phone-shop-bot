package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.OrderStatus;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer id;
    private Date createdAt;
    private OrderStatus status;
    private Double totalSum;
    private User user;
    private String address;
    private Float lon;
    private Float lat;
}
