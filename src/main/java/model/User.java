package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.BotState;
import model.enums.Language;
import model.template.AbsName;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbsName {
    private String phoneNumber;
    private String password;
    private Role role;
    private long tgUserId;
    private Language language;
    private BotState botState;
}
