package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.template.AbsName;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attachment extends AbsName {
    private int size;
    private String type;
}
