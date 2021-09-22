package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Integer id;
    private String nameUz;
    private String nameRu;
    private String descriptionUz;
    private String descriptionRu;
    private double incomePrice;
    private double salePrice;
    private Attachment attachment;
}
