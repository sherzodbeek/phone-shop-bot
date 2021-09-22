package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basket {
    private Integer id;
    private int userId;
    private int productId;
    private int amount;
    private double totalSum;
}
