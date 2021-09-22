import bot.BotService;
import bot.PhoneShopBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(new PhoneShopBot());
//        try {
//            BotService.addProduct();
//        } catch (ClassNotFoundException | IOException | SQLException e) {
//            e.printStackTrace();
//        }


    }
}
