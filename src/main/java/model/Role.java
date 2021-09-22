package model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.RoleName;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private Integer id;
    private RoleName roleName;
}
