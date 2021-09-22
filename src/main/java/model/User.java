package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.BotState;
import model.enums.Language;
import model.template.AbsName;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends AbsName {
    private String phoneNumber;
    private String password;
    private Role role;
    private Long tgUserId;
    private Language language;
    private BotState botState;
    private Integer pageNum;
    private Integer lastPageNum;
    private Integer oldMessageId;
    private Integer productId;
    private Integer amount;
}
